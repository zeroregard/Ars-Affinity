package com.github.ars_affinity.school;

import com.github.ars_affinity.config.ArsAffinityConfig;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for managing relationships between magic schools and affinity calculations.
 * All methods are static utility functions.
 */
public class SchoolRelationshipHelper {

    private SchoolRelationshipHelper() {}

    private static final Map<SpellSchool, SpellSchool> OPPOSITE_SCHOOLS = new HashMap<>();
    
    static {
        OPPOSITE_SCHOOLS.put(SpellSchools.ELEMENTAL_FIRE, SpellSchools.ELEMENTAL_WATER);
        OPPOSITE_SCHOOLS.put(SpellSchools.ELEMENTAL_WATER, SpellSchools.ELEMENTAL_FIRE);
        OPPOSITE_SCHOOLS.put(SpellSchools.ELEMENTAL_EARTH, SpellSchools.ELEMENTAL_AIR);
        OPPOSITE_SCHOOLS.put(SpellSchools.ELEMENTAL_AIR, SpellSchools.ELEMENTAL_EARTH);
        OPPOSITE_SCHOOLS.put(SpellSchools.ABJURATION, SpellSchools.NECROMANCY);
        OPPOSITE_SCHOOLS.put(SpellSchools.NECROMANCY, SpellSchools.ABJURATION);
        OPPOSITE_SCHOOLS.put(SpellSchools.CONJURATION, SpellSchools.MANIPULATION);
        OPPOSITE_SCHOOLS.put(SpellSchools.MANIPULATION, SpellSchools.CONJURATION);
    }
    

    public static SpellSchool getOppositeSchool(SpellSchool school) {
        return OPPOSITE_SCHOOLS.get(school);
    }

    public static final SpellSchool[] ALL_SCHOOLS = {
            SpellSchools.ELEMENTAL_FIRE,
            SpellSchools.ELEMENTAL_WATER,
            SpellSchools.ELEMENTAL_EARTH,
            SpellSchools.ELEMENTAL_AIR,
            SpellSchools.ABJURATION,
            SpellSchools.NECROMANCY,
            SpellSchools.CONJURATION,
            SpellSchools.MANIPULATION
    };

    public static boolean areOpposites(SpellSchool school1, SpellSchool school2) {
        if (school1 == null || school2 == null) return false;
        return school1.equals(getOppositeSchool(school2));
    }

    public static Map<SpellSchool, Float> calculateAffinityChanges(SpellSchool castSchool, float mana) {
        Map<SpellSchool, Float> changes = new HashMap<>();
        
        float multiplier = ArsAffinityConfig.AFFINITY_GAIN_MULTIPLIER.get().floatValue();
        float baseAmount = (mana * multiplier) / 100.0f; // This gives us 1% per 100 mana
        changes.put(castSchool, baseAmount);
        
        SpellSchool oppositeSchool = getOppositeSchool(castSchool);
        float opposingPenaltyPercentage = ArsAffinityConfig.OPPOSING_SCHOOL_PENALTY_PERCENTAGE.get().floatValue();
        
        int otherSchoolsCount = 0;
        for (SpellSchool school : ALL_SCHOOLS) {
            if (school != castSchool && school != oppositeSchool) {
                otherSchoolsCount++;
            }
        }
        
        for (SpellSchool school : ALL_SCHOOLS) {
            if (school == castSchool) {
                continue;
            }
            
            float penalty;
            if (school == oppositeSchool) {
                // Opposing school gets the configured penalty percentage
                penalty = -baseAmount * opposingPenaltyPercentage;
            } else {
                // Other schools split the remaining penalty equally
                float remainingPenaltyPercentage = 1.0f - opposingPenaltyPercentage;
                penalty = -(baseAmount * remainingPenaltyPercentage) / otherSchoolsCount;
            }
            
            changes.put(school, penalty);
        }
        
        return changes;
    }
    
    public static Map<SpellSchool, Float> applyAffinityChanges(
            Map<SpellSchool, Float> currentAffinities, 
            Map<SpellSchool, Float> changes) {
        
        Map<SpellSchool, Float> newAffinities = new HashMap<>();

        for (SpellSchool school : ALL_SCHOOLS) {
            float currentAffinity = currentAffinities.getOrDefault(school, 0.0f);
            float change = changes.getOrDefault(school, 0.0f);
            float newAffinity = Math.max(0.0f, Math.min(1.0f, currentAffinity + change));
            newAffinities.put(school, newAffinity);
        }

        float total = 0.0f;
        for (float value : newAffinities.values()) {
            total += value;
        }
        
        if (total > 1.0f) {
            for (SpellSchool school : newAffinities.keySet()) {
                float value = newAffinities.get(school);
                newAffinities.put(school, value / total);
            }
        }
        
        return newAffinities;
    }
    
    private static boolean configValidated = false;
    
    public static int calculateTierFromAffinity(float affinity) {
        // Lazy validation - validate config the first time this method is called
        if (!configValidated) {
            ArsAffinityConfig.validateTierThresholds();
            configValidated = true;
        }
        
        float percentage = affinity * 100.0f;
        
        double tier3Threshold = ArsAffinityConfig.TIER_3_THRESHOLD_PERCENTAGE.get();
        double tier2Threshold = ArsAffinityConfig.TIER_2_THRESHOLD_PERCENTAGE.get();
        double tier1Threshold = ArsAffinityConfig.TIER_1_THRESHOLD_PERCENTAGE.get();
        
        if (percentage >= tier3Threshold) {
            return 3;
        } else if (percentage >= tier2Threshold) {
            return 2;
        } else if (percentage >= tier1Threshold) {
            return 1;
        } else {
            return 0;
        }
    }

    public static int getColorForSchool(SpellSchool school) {
        if (school == SpellSchools.ELEMENTAL_FIRE) {
            return 0xFFf06666;
        } else if (school == SpellSchools.ELEMENTAL_WATER) {
            return 0xFF82a2ed;
        } else if (school == SpellSchools.ELEMENTAL_EARTH) {
            return 0xFF62e296;
        } else if (school == SpellSchools.ELEMENTAL_AIR) {
            return 0xFFd4cf5a;
        } else if (school == SpellSchools.ABJURATION) {
            return 0xFFeb7cce;
        } else if (school == SpellSchools.NECROMANCY) {
            return 0xFF6d6d6d;
        } else if (school == SpellSchools.CONJURATION) {
            return 0xFF6ae3ce;
        } else if (school == SpellSchools.MANIPULATION) {
            return 0xFFFF8800;
        } else {
            return 0xFFe99a58;
        }
    }
}