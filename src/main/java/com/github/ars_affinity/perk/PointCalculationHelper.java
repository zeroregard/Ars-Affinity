package com.github.ars_affinity.perk;

import com.github.ars_affinity.config.ArsAffinityConfig;

/**
 * Helper class for calculating affinity points based on mana usage.
 * Converts the old percentage-based system to a point-based system.
 */
public class PointCalculationHelper {
    
    private PointCalculationHelper() {}
    
    /**
     * Calculate the percentage increase for a school based on mana usage.
     * This is the core method for percentage-based progression.
     * 
     * @param mana The mana cost of the spell
     * @param currentPercentage The current percentage in this school (0.0 to 100.0)
     * @param totalPointsAcrossAllSchools The total points across all schools
     * @return The percentage increase (0.0 to 100.0)
     */
    public static float calculatePercentageIncrease(float mana, float currentPercentage, int totalPointsAcrossAllSchools) {
        float multiplier = ArsAffinityConfig.AFFINITY_GAIN_MULTIPLIER.get().floatValue();
        float basePercentageIncrease = mana * multiplier;
        
        // Apply school-specific scaling decay - percentage becomes harder to gain as you have more in this school
        float schoolDecayStrength = ArsAffinityConfig.AFFINITY_SCALING_DECAY_STRENGTH.get().floatValue();
        float schoolMinimumFactor = ArsAffinityConfig.AFFINITY_SCALING_MINIMUM_FACTOR.get().floatValue();
        float schoolScalingFactor = calculateScalingFactor((int) currentPercentage, schoolDecayStrength, schoolMinimumFactor);
        
        // Apply global scaling decay - percentage becomes harder to gain as you have more total points across all schools
        float globalDecayStrength = ArsAffinityConfig.GLOBAL_SCALING_DECAY_STRENGTH.get().floatValue();
        float globalMinimumFactor = ArsAffinityConfig.GLOBAL_SCALING_MINIMUM_FACTOR.get().floatValue();
        float globalScalingFactor = calculateScalingFactor(totalPointsAcrossAllSchools, globalDecayStrength, globalMinimumFactor);
        
        // Combine both scaling factors (multiplicative)
        float combinedScalingFactor = schoolScalingFactor * globalScalingFactor;
        
        return Math.max(0.0f, basePercentageIncrease * combinedScalingFactor);
    }
    
    /**
     * Calculate the base points gained from mana usage for a specific school.
     * This is now deprecated in favor of percentage-based progression.
     * 
     * @param mana The mana cost of the spell
     * @param currentPoints The current total points in this school
     * @param totalPointsAcrossAllSchools The total points across all schools
     * @return The points to add to this school
     */
    @Deprecated
    public static int calculatePointsGained(float mana, int currentPoints, int totalPointsAcrossAllSchools) {
        // Convert to percentage-based calculation for backward compatibility
        float percentageIncrease = calculatePercentageIncrease(mana, (float) currentPoints, totalPointsAcrossAllSchools);
        // This is a rough approximation - the new system should use addSchoolProgress directly
        return Math.max(0, Math.round(percentageIncrease / 10.0f)); // Rough conversion
    }
    
    /**
     * Calculate the base points gained from mana usage for a specific school.
     * This is a convenience method for backward compatibility.
     * 
     * @param mana The mana cost of the spell
     * @param currentPoints The current total points in this school
     * @return The points to add to this school
     */
    public static int calculatePointsGained(float mana, int currentPoints) {
        // Use 0 total points for backward compatibility (no global scaling)
        return calculatePointsGained(mana, currentPoints, 0);
    }
    
    /**
     * Calculate the scaling factor for point gain based on current points.
     * This uses the same logarithmic scaling as the old percentage system.
     * 
     * @param currentPoints Current total points in the school
     * @param decayStrength How quickly gain decreases (from config)
     * @param minimumFactor Minimum percentage of original gain (from config)
     * @return Scaling factor between minimumFactor and 1.0
     */
    private static float calculateScalingFactor(int currentPoints, float decayStrength, float minimumFactor) {
        if (currentPoints <= 0) {
            return 1.0f;
        }
        
        // Use logarithmic scaling: factor = 1 / (1 + currentPoints^decayStrength)
        // This matches the old percentage system's behavior
        double scaledPoints = Math.pow(currentPoints, decayStrength);
        float factor = (float) (1.0 / (1.0 + scaledPoints));
        
        // Ensure we don't go below the minimum factor
        return Math.max(minimumFactor, factor);
    }
    
    /**
     * Calculate the maximum possible points for a school based on the number of perks available.
     * This determines the "100% affinity" equivalent in the new system.
     * 
     * @param perkCount Number of perks available in this school
     * @return Maximum points possible for this school
     */
    public static int calculateMaxPointsForSchool(int perkCount) {
        // Max points should equal the number of perks available
        // This ensures that 100% affinity = all perks unlocked
        return perkCount;
    }
    
    /**
     * Convert old percentage affinity to new point system.
     * Used for data migration from old to new system.
     * 
     * @param percentageAffinity Old percentage (0.0 to 1.0)
     * @param maxPoints Maximum points possible for this school
     * @return Equivalent points in new system
     */
    public static int convertPercentageToPoints(float percentageAffinity, int maxPoints) {
        return Math.round(percentageAffinity * maxPoints);
    }
    
    /**
     * Convert new point system back to percentage for display purposes.
     * 
     * @param currentPoints Current points in school
     * @param maxPoints Maximum points possible for this school
     * @return Percentage equivalent (0.0 to 1.0)
     */
    public static float convertPointsToPercentage(int currentPoints, int maxPoints) {
        if (maxPoints <= 0) return 0.0f;
        return Math.min(1.0f, (float) currentPoints / maxPoints);
    }
    
}
