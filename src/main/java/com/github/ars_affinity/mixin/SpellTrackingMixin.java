package com.github.ars_affinity.mixin;


import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.event.SchoolAffinityPointAllocatedEvent;
import com.github.ars_affinity.perk.PointCalculationHelper;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
import com.github.ars_affinity.util.CuriosHelper;
import com.github.ars_affinity.util.GlyphBlacklistHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mixin to track spell resolution for affinity.
 * Intercepts spell casts to track mana usage and glyph usage per school.
 * 
 * This mixin targets the main SpellResolver class to intercept ALL spell resolutions.
 */
@Mixin(value = SpellResolver.class, remap = false)
public abstract class SpellTrackingMixin {

    @Shadow(remap = false)
    public SpellContext spellContext;

    @Shadow(remap = false)
    public abstract SpellStats getCastStats();

    @Inject(method = "onResolveEffect", at = @At(value = "HEAD"))
    private void trackSpellResolution(Level world, HitResult result, CallbackInfo ci) {
        if (world.isClientSide()) {
            return;
        }
        
        try {
            SpellContext spellContext = this.spellContext;
            if (spellContext == null) {
                return;
            }
            
            var caster = spellContext.getCaster();
            if (!(caster instanceof PlayerCaster)) {
                return;
            }
            
            Spell spell = spellContext.getSpell();
            if (spell == null) {
                return;
            }

            if (spell.recipe() == null) {
                return;
            }
            
            boolean hasElements = false;
            for (var part : spell.recipe()) {
                hasElements = true;
                break;
            }
            if (!hasElements) {
                return;
            }

            SpellStats spellStats = this.getCastStats();
            trackSpellUsage((PlayerCaster) caster, spell, spellStats, world);
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error tracking spell resolution for mastery: {}", e.getMessage());
        }
    }

    private void trackSpellUsage(PlayerCaster caster, Spell spell, SpellStats stats, Level world) {
        try {
            var recipe = spell.recipe();

            if (recipe == null) {
                return;
            }

            List<AbstractAugment> currentAugments = new ArrayList<>();
            AbstractSpellPart currentBase = null;

            for (AbstractSpellPart glyph : recipe) {
                switch (glyph) {
                    case null -> {
                    }
                    case AbstractCastMethod ignored -> {
                    }
                    case AbstractAugment augment ->
                            currentAugments.add(augment);
                    default -> {
                        if (currentBase != null) {
                            trackSpellSegment(caster, currentBase, new ArrayList<>(currentAugments), stats, world);
                            currentAugments.clear();
                        }
                        currentBase = glyph;
                    }
                }

            }
            if (currentBase != null) {
                trackSpellSegment(caster, currentBase, currentAugments, stats, world);
            }

        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error accessing spell recipe: {}", e.getMessage());
        }
    }

    private void trackSpellSegment(PlayerCaster caster, AbstractSpellPart glyph, List<AbstractAugment> augments, SpellStats stats, Level world) {
        Player player = caster.player;
        if (player == null) {
            return;
        }

        trackSchoolProgress(player, glyph, augments);
    }

    private void trackSchoolProgress(Player player, AbstractSpellPart glyph, List<AbstractAugment> augments) {
        if (CuriosHelper.hasActiveAnchorCharm(player)) {
            ArsAffinity.LOGGER.info("Player {} has active Anchor Charm - preventing affinity changes", player.getName().getString());
            CuriosHelper.consumeAnchorCharmCharge(player);
            return;
        }
        
        // Check if the glyph is blacklisted
        if (GlyphBlacklistHelper.isGlyphBlacklisted(glyph)) {
            ArsAffinity.LOGGER.info("Glyph {} is blacklisted - skipping affinity progress tracking", 
                glyph.getRegistryName() != null ? glyph.getRegistryName().toString() : "unknown");
            return;
        }
        
        float manaCost = glyph.getCastingCost();

        List<SpellSchool> schools = glyph.spellSchools;
        if (schools == null || schools.isEmpty()) {
            return;
        }
        
        // Get player's current affinity data
        PlayerAffinityData affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData == null) {
            ArsAffinity.LOGGER.warn("Could not get affinity data for player: {}", player.getName().getString());
            return;
        }
        
        // Calculate point changes for each school and combine them
        float distributedCost = manaCost / schools.size();
        Map<SpellSchool, Integer> combinedChanges = new HashMap<>();
        
        for (SpellSchool school : schools) {
            Map<SpellSchool, Integer> changes = calculatePointChanges(school, distributedCost, affinityData);
            // Combine changes from all schools
            for (Map.Entry<SpellSchool, Integer> entry : changes.entrySet()) {
                combinedChanges.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        
        // Apply all point changes at once and fire events
        boolean hasChanges = false;
        for (Map.Entry<SpellSchool, Integer> entry : combinedChanges.entrySet()) {
            if (entry.getValue() != 0) {
                affinityData.addSchoolPoints(entry.getKey(), entry.getValue());
                int newPoints = affinityData.getSchoolPoints(entry.getKey());
                
                // Fire point allocation event
                SchoolAffinityPointAllocatedEvent event = new SchoolAffinityPointAllocatedEvent(
                    player, 
                    entry.getKey(), 
                    entry.getValue(), 
                    newPoints
                );
                NeoForge.EVENT_BUS.post(event);
                
                hasChanges = true;
            }
        }
        
        if (hasChanges) {
            StringBuilder affinityLog = new StringBuilder();
            affinityLog.append("Affinity Points: ");
            for (SpellSchool affinitySchool : SchoolRelationshipHelper.ALL_SCHOOLS) {
                int points = affinityData.getSchoolPoints(affinitySchool);
                affinityLog.append(String.format("%s: %d pts", 
                    affinitySchool.getTextComponent().getString(), 
                    points));
                if (affinitySchool != SchoolRelationshipHelper.ALL_SCHOOLS[SchoolRelationshipHelper.ALL_SCHOOLS.length - 1]) {
                    affinityLog.append(", ");
                }
            }
            
            ArsAffinity.LOGGER.info("Affinity Points: {}", affinityLog.toString());
            
            // Save the changes
            PlayerAffinityDataHelper.savePlayerData(player);
        }
    }
    
    /**
     * Calculate point changes for a school based on mana usage.
     * This replaces the old percentage-based affinity calculation.
     * Now only increases points in the school being used - no penalties for other schools.
     */
    private Map<SpellSchool, Integer> calculatePointChanges(SpellSchool castSchool, float mana, PlayerAffinityData affinityData) {
        Map<SpellSchool, Integer> changes = new HashMap<>();
        
        // Calculate points gained for the primary school only
        int currentPoints = affinityData.getSchoolPoints(castSchool);
        int maxPoints = com.github.ars_affinity.perk.PerkTreeManager.getMaxPointsForSchool(castSchool);
        
        // Check if already at 100% (max points) - no further points should be gained
        if (currentPoints >= maxPoints) {
            ArsAffinity.LOGGER.debug("Player already at max points ({}/{}) for {} school - no further points gained", 
                currentPoints, maxPoints, castSchool.getId());
            changes.put(castSchool, 0);
            return changes;
        }
        
        int pointsGained = PointCalculationHelper.calculatePointsGained(mana, currentPoints);
        
        // Ensure we don't exceed the maximum points
        int newTotalPoints = currentPoints + pointsGained;
        if (newTotalPoints > maxPoints) {
            pointsGained = maxPoints - currentPoints;
            ArsAffinity.LOGGER.debug("Capped point gain to prevent exceeding max points: {} -> {} (max: {})", 
                pointsGained + currentPoints, maxPoints, maxPoints);
        }
        
        changes.put(castSchool, pointsGained);
        
        // No penalties for other schools - each school is independent
        return changes;
    }

} 