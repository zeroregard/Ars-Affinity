package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.Arrays;
import java.util.List;

/**
 * Server-wide registry for all perk configurations.
 * Initialized once from config and shared across all players.
 */
public class PerkRegistry {
    private static final Map<String, PerkData> PERK_REGISTRY = new HashMap<>();
    private static final int MAX_TIER = 5; // Adjust based on your tier system
    
    // Define the spell schools we want to support
    private static final SpellSchool[] SUPPORTED_SCHOOLS = {
        SpellSchools.ELEMENTAL_FIRE,
        SpellSchools.ELEMENTAL_WATER,
        SpellSchools.ELEMENTAL_EARTH,
        SpellSchools.ELEMENTAL_AIR,
        SpellSchools.ABJURATION,
        SpellSchools.NECROMANCY,
        SpellSchools.CONJURATION,
        SpellSchools.MANIPULATION
    };
    
    /**
     * Initialize the perk registry from configuration.
     * Called once during server startup.
     */
    public static void initializeFromConfig() {
        PERK_REGISTRY.clear();
        
        // Initialize all possible perk combinations
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            for (AffinityPerkType perkType : AffinityPerkType.values()) {
                for (int tier = 1; tier <= MAX_TIER; tier++) {
                    String key = generateKey(school, perkType, tier);
                    PerkData data = createPerkDataFromConfig(school, perkType, tier);
                    if (data != null) {
                        PERK_REGISTRY.put(key, data);
                        ArsAffinity.LOGGER.debug("Registered perk: {} -> {}", key, data);
                    }
                }
            }
        }
        
        ArsAffinity.LOGGER.info("PerkRegistry initialized with {} perk configurations", PERK_REGISTRY.size());
    }
    
    /**
     * Generate a unique key for a perk configuration.
     * Format: "School_PerkType_Tier" (e.g., "ELEMENTAL_FIRE_PASSIVE_FIRE_THORNS_3")
     */
    private static String generateKey(SpellSchool school, AffinityPerkType perkType, int tier) {
        return String.format("%s_%s_%d", 
            school.getId().toString().toUpperCase().replace(":", "_"), 
            perkType.name(), 
            tier);
    }
    
    /**
     * Create PerkData from configuration for a specific school, perk type, and tier.
     * This is where you'd load the actual perk configuration values.
     */
    private static PerkData createPerkDataFromConfig(SpellSchool school, AffinityPerkType perkType, int tier) {
        // TODO: Replace this with actual config loading logic
        // For now, create some example perks based on type
        
        AffinityPerk perk = createPerkFromConfig(perkType, tier);
        if (perk == null) {
            return null; // This perk combination doesn't exist in config
        }
        
        return new PerkData(perk, school, tier);
    }
    
    /**
     * Create the actual AffinityPerk instance from configuration.
     * This is where you'd load perk-specific values like amounts, durations, etc.
     */
    private static AffinityPerk createPerkFromConfig(AffinityPerkType perkType, int tier) {
        // TODO: Replace this with actual config loading logic
        // For now, create example perks based on type
        
        switch (perkType) {
            case PASSIVE_MANA_TAP:
                return new AffinityPerk.AmountBasedPerk(perkType, 0.1f * tier, true);
            case PASSIVE_FIRE_THORNS:
                return new AffinityPerk.AmountBasedPerk(perkType, 0.15f * tier, true);
            case PASSIVE_STONE_SKIN:
                return new AffinityPerk.AmountBasedPerk(perkType, 0.2f * tier, true);
            case PASSIVE_SUMMON_HEALTH:
                return new AffinityPerk.AmountBasedPerk(perkType, 0.25f * tier, true);
            case PASSIVE_MANIPULATION_SICKNESS:
                return new AffinityPerk.ManipulationSicknessPerk(perkType, 20 * tier, 2 * tier, false);
            case PASSIVE_LICH_FEAST:
                return new AffinityPerk.LichFeastPerk(perkType, 0.3f * tier, 0.1f * tier, true);
            case PASSIVE_UNSTABLE_SUMMONING:
                return new AffinityPerk.UnstableSummoningPerk(perkType, 0.4f * tier, Arrays.asList("zombie", "skeleton"), true);
            case PASSIVE_GHOST_STEP:
                return new AffinityPerk.GhostStepPerk(perkType, 0.5f * tier, 20 * tier, 60 * tier, true);
            default:
                // For other perk types, create a basic amount-based perk
                return new AffinityPerk.AmountBasedPerk(perkType, 0.1f * tier, true);
        }
    }
    
    /**
     * Get a perk configuration by key.
     * @param key The perk key (e.g., "ELEMENTAL_FIRE_PASSIVE_FIRE_THORNS_3")
     * @return The PerkData, or null if not found
     */
    public static PerkData getPerk(String key) {
        return PERK_REGISTRY.get(key);
    }
    
    /**
     * Get a perk configuration by school, perk type, and tier.
     * @param school The spell school
     * @param perkType The perk type
     * @param tier The tier
     * @return The PerkData, or null if not found
     */
    public static PerkData getPerk(SpellSchool school, AffinityPerkType perkType, int tier) {
        String key = generateKey(school, perkType, tier);
        return PERK_REGISTRY.get(key);
    }
    
    /**
     * Check if a perk configuration exists.
     */
    public static boolean hasPerk(String key) {
        return PERK_REGISTRY.containsKey(key);
    }
    
    /**
     * Get all registered perk keys (for debugging/testing).
     */
    public static Set<String> getAllPerkKeys() {
        return new HashSet<>(PERK_REGISTRY.keySet());
    }
    
    /**
     * Get the total number of registered perk configurations.
     */
    public static int getTotalPerkCount() {
        return PERK_REGISTRY.size();
    }
}