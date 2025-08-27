package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.function.Consumer;

/**
 * Helper class for efficient O(1) perk lookups and operations.
 * This replaces the old O(n) system that required looping through schools and tiers.
 */
public class AffinityPerkHelper {

    /**
     * Get the active perk for a specific perk type.
     * This provides O(1) access to the highest tier perk of the given type.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The active perk, or null if no perk of this type is active
     */
    public static AffinityPerk getActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.perk : null;
    }
    
    /**
     * Check if a specific perk type is currently active.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to check
     * @return True if the perk is active, false otherwise
     */
    public static boolean hasActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.hasActivePerk(perkType);
    }
    
    /**
     * Get the active perk data for a specific perk type.
     * This provides access to both the perk and metadata about its source.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The perk data, or null if no perk of this type is active
     */
    public static PerkData getActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.getActivePerk(perkType);
    }
    
    /**
     * Get the source school for a specific perk type.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The source school, or null if no perk of this type is active
     */
    public static SpellSchool getPerkSourceSchool(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.sourceSchool : null;
    }
    
    /**
     * Get the source tier for a specific perk type.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The source tier, or 0 if no perk of this type is active
     */
    public static int getPerkSourceTier(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.sourceTier : 0;
    }
    
    /**
     * Apply a consumer to the active perk if it exists.
     * This is the O(1) equivalent of the old applyAllHighestTierPerks method.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to apply
     * @param perkConsumer The consumer to apply to the perk
     */
    public static void applyActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        AffinityPerk perk = getActivePerk(progress, perkType);
        if (perk != null) {
            perkConsumer.accept(perk);
        }
    }
} 