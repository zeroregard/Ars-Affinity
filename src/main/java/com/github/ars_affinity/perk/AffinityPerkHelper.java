package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.function.Consumer;
import java.util.Map;
import java.util.Set;

/**
 * Helper class for working with affinity perks using the new O(1) lookup system.
 * Now works with the memory-efficient PerkReference system.
 */
public class AffinityPerkHelper {
    
    /**
     * Check if a player has an active perk of the specified type.
     * O(1) lookup using the perk index.
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
     * O(1) lookup using the perk index.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The perk data, or null if no perk of this type is active
     */
    public static AffinityPerk getActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.perk : null;
    }
    
    /**
     * Get the active perk data object for a specific perk type.
     * O(1) lookup using the perk index.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to look up
     * @return The PerkData object, or null if no perk of this type is active
     */
    public static PerkData getActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.getActivePerk(perkType);
    }
    
    /**
     * Get the source school for a specific perk type.
     * O(1) lookup using the perk index.
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
     * O(1) lookup using the perk index.
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
     * Apply a consumer function to the active perk of the specified type.
     * O(1) lookup using the perk index.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to apply
     * @param perkConsumer The consumer function to apply to the perk
     */
    public static void applyActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        AffinityPerk perk = getActivePerk(progress, perkType);
        if (perk != null) {
            perkConsumer.accept(perk);
        }
    }
    
    /**
     * Apply a consumer function to the active perk data of the specified type.
     * O(1) lookup using the perk index.
     * 
     * @param progress The player's affinity progress
     * @param perkType The type of perk to apply
     * @param perkDataConsumer The consumer function to apply to the perk data
     */
    public static void applyActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<PerkData> perkDataConsumer) {
        PerkData perkData = getActivePerkData(progress, perkType);
        if (perkData != null) {
            perkDataConsumer.accept(perkData);
        }
    }
    
    /**
     * Get all active perks for a player.
     * 
     * @param progress The player's affinity progress
     * @return A map of perk types to their active perk data
     */
    public static Map<AffinityPerkType, PerkData> getAllActivePerks(SchoolAffinityProgress progress) {
        return progress.getAllActivePerks();
    }
    
    /**
     * Get all active perk references for a player.
     * 
     * @param progress The player's affinity progress
     * @return A set of all active perk references
     */
    public static Set<PerkReference> getAllActivePerkReferences(SchoolAffinityProgress progress) {
        return progress.getAllActivePerkReferences();
    }
} 