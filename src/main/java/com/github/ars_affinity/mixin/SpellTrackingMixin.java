package com.github.ars_affinity.mixin;


import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.event.SchoolAffinityPointAllocatedEvent;
import com.github.ars_affinity.perk.PointCalculationHelper;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
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
import java.util.List;

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
            for (var ignored : spell.recipe()) {
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
        
        // Calculate percentage increases for each school and apply them
        float distributedCost = manaCost / schools.size();
        boolean hasChanges = false;
        
        for (SpellSchool school : schools) {
            float currentPercentage = affinityData.getSchoolPercentage(school);
            int totalPointsAcrossAllSchools = affinityData.getTotalPointsAcrossAllSchools();
            
            // Calculate percentage increase for this school
            float percentageIncrease = PointCalculationHelper.calculatePercentageIncrease(
                distributedCost, currentPercentage, totalPointsAcrossAllSchools);
            
            if (percentageIncrease > 0.0f) {
                // Add progress to the school and get points awarded
                int pointsAwarded = affinityData.addSchoolProgress(school, percentageIncrease);
                
                if (pointsAwarded > 0) {
                    // Fire point allocation event
                    SchoolAffinityPointAllocatedEvent event = new SchoolAffinityPointAllocatedEvent(
                        player, 
                        school, 
                        pointsAwarded, 
                        affinityData.getSchoolPoints(school)
                    );
                    NeoForge.EVENT_BUS.post(event);
                }
                
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
    

} 