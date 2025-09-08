package com.github.ars_affinity.mixin;


import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
import com.github.ars_affinity.util.CuriosHelper;
import com.github.ars_affinity.util.GlyphBlacklistHelper;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
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
            for (AbstractSpellPart part : spell.recipe()) {
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
        
        // Calculate affinity changes for each school and combine them
        float distributedCost = manaCost / schools.size();
        Map<SpellSchool, Float> combinedChanges = new HashMap<>();
        
        for (SpellSchool school : schools) {
            Map<SpellSchool, Float> changes = SchoolRelationshipHelper.calculateAffinityChanges(school, distributedCost);
            // Combine changes from all schools
            for (Map.Entry<SpellSchool, Float> entry : changes.entrySet()) {
                combinedChanges.merge(entry.getKey(), entry.getValue(), Float::sum);
            }
        }
        
        // Apply all changes at once to avoid multiple tier change events
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.applyAffinityChanges(player, combinedChanges);
        
        if (progress != null) {
            StringBuilder affinityLog = new StringBuilder();
            affinityLog.append("Affinity Progress: ");
            for (SpellSchool affinitySchool : SchoolRelationshipHelper.ALL_SCHOOLS) {
                float affinity = progress.getAffinity(affinitySchool);
                affinityLog.append(String.format("%s: %.1f%%", 
                    affinitySchool.getTextComponent().getString(), 
                    affinity * 100.0f));
                if (affinitySchool != SchoolRelationshipHelper.ALL_SCHOOLS[SchoolRelationshipHelper.ALL_SCHOOLS.length - 1]) {
                    affinityLog.append(", ");
                }
            }
            
            ArsAffinity.LOGGER.info("Affinity Progress: {}", affinityLog.toString());
            
            // The capability system handles saving automatically
            // No need to manually save here
        }
    }

} 