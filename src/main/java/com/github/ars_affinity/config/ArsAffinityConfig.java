package com.github.ars_affinity.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ArsAffinityConfig {
    
    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec.DoubleValue OPPOSING_SCHOOL_PENALTY_PERCENTAGE;
    public static ModConfigSpec.DoubleValue AFFINITY_GAIN_MULTIPLIER;
    public static ModConfigSpec.IntValue DEEP_UNDERGROUND_Y_THRESHOLD;
    public static ModConfigSpec.IntValue ANCHOR_CHARM_DEFAULT_CHARGES;
    
    static {
        ModConfigSpec.Builder SERVER_BUILDER = new ModConfigSpec.Builder();
        
        SERVER_BUILDER.comment("Affinity System").push("affinity");
        OPPOSING_SCHOOL_PENALTY_PERCENTAGE = SERVER_BUILDER
            .comment("Percentage of affinity gain that opposing schools lose (0.0 to 1.0)")
            .defineInRange("opposingSchoolPenaltyPercentage", 0.66, 0.0, 1.0);
        AFFINITY_GAIN_MULTIPLIER = SERVER_BUILDER
            .comment("Multiplier for affinity gain per mana spent. Higher values = faster progression (0.001 to 0.1)")
            .defineInRange("affinityGainMultiplier", 0.01, 0.001, 0.1);
        DEEP_UNDERGROUND_Y_THRESHOLD = SERVER_BUILDER
            .comment("Y coordinate threshold for deep underground detection. Players below this Y level are considered deep underground")
            .defineInRange("deepUndergroundYThreshold", 20, -64, 320);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Anchor Charm Configuration").push("anchor_charm");
        ANCHOR_CHARM_DEFAULT_CHARGES = SERVER_BUILDER
            .comment("Default number of charges for new Anchor Charms. Each spell cast consumes 1 charge when preventing affinity changes.")
            .defineInRange("defaultCharges", 1000, 1, 10000);
        SERVER_BUILDER.pop();
        
        SERVER_CONFIG = SERVER_BUILDER.build();
    }
} 