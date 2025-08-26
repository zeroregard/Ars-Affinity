package com.github.ars_affinity.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.List;

public class ArsAffinityConfig {
    
    public static ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec.DoubleValue OPPOSING_SCHOOL_PENALTY_PERCENTAGE;
    public static ModConfigSpec.DoubleValue AFFINITY_GAIN_MULTIPLIER;
    public static ModConfigSpec.IntValue DEEP_UNDERGROUND_Y_THRESHOLD;
    public static ModConfigSpec.IntValue ANCHOR_CHARM_DEFAULT_CHARGES;
    
    // ICE BLAST Configuration
    public static ModConfigSpec.DoubleValue ICE_BLAST_DEFAULT_MANA_COST;
    public static ModConfigSpec.IntValue ICE_BLAST_DEFAULT_COOLDOWN;
    public static ModConfigSpec.DoubleValue ICE_BLAST_DEFAULT_DAMAGE;
    public static ModConfigSpec.IntValue ICE_BLAST_DEFAULT_FREEZE_TIME;
    public static ModConfigSpec.DoubleValue ICE_BLAST_DEFAULT_RADIUS;
    public static ModConfigSpec.DoubleValue GROUND_SLAM_MAX_DROP_DISTANCE;
    public static ModConfigSpec.DoubleValue GROUND_SLAM_MAX_RADIUS;
    
    // SWARM Active Ability Configuration
    public static ModConfigSpec.DoubleValue SWARM_DEFAULT_RADIUS;
    public static ModConfigSpec.DoubleValue SWARM_DEFAULT_TARGET_RANGE;
    
    // Summon Distance Override Configuration
    public static ModConfigSpec.DoubleValue SWARM_SUMMON_DISTANCE_OVERRIDE_MIN_DISTANCE;
    public static ModConfigSpec.DoubleValue SWARM_SUMMON_DISTANCE_OVERRIDE_MAX_DISTANCE;
    public static ModConfigSpec.DoubleValue SWARM_SUMMON_DISTANCE_OVERRIDE_TELEPORT_DISTANCE;
    
    // Glyph Blacklist Configuration
    public static ModConfigSpec.ConfigValue<List<? extends String>> GLYPH_BLACKLIST;
    
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
        
        SERVER_BUILDER.comment("ICE BLAST Active Ability Configuration").push("ice_blast");
        ICE_BLAST_DEFAULT_MANA_COST = SERVER_BUILDER
            .comment("Default mana cost percentage for ICE BLAST ability (0.0 to 1.0)")
            .defineInRange("defaultManaCost", 0.3, 0.1, 0.8);
        ICE_BLAST_DEFAULT_COOLDOWN = SERVER_BUILDER
            .comment("Default cooldown in ticks for ICE BLAST ability (20 ticks = 1 second)")
            .defineInRange("defaultCooldown", 200, 60, 1200);
        ICE_BLAST_DEFAULT_DAMAGE = SERVER_BUILDER
            .comment("Default damage for ICE BLAST ability")
            .defineInRange("defaultDamage", 8.0, 1.0, 20.0);
        ICE_BLAST_DEFAULT_FREEZE_TIME = SERVER_BUILDER
            .comment("Default freeze time in ticks for ICE BLAST ability (20 ticks = 1 second)")
            .defineInRange("defaultFreezeTime", 100, 20, 400);
        ICE_BLAST_DEFAULT_RADIUS = SERVER_BUILDER
            .comment("Default radius for ICE BLAST ability")
            .defineInRange("defaultRadius", 6.0, 2.0, 15.0);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("GROUND SLAM Configuration").push("ground_slam");
        GROUND_SLAM_MAX_DROP_DISTANCE = SERVER_BUILDER
            .comment("Max drop distance used for logarithmic scaling (blocks)")
            .defineInRange("maxDropDistance", 10.0, 1.0, 64.0);
        GROUND_SLAM_MAX_RADIUS = SERVER_BUILDER
            .comment("Max affect radius (blocks)")
            .defineInRange("maxRadius", 5.0, 1.0, 16.0);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("SWARM Active Ability Configuration").push("swarm");
        SWARM_DEFAULT_RADIUS = SERVER_BUILDER
            .comment("Radius to search for player summons when using SWARM ability (blocks)")
            .defineInRange("defaultRadius", 32.0, 16.0, 128.0);
        SWARM_DEFAULT_TARGET_RANGE = SERVER_BUILDER
            .comment("Maximum range to look for targets when using SWARM ability (blocks)")
            .defineInRange("defaultTargetRange", 100.0, 50.0, 200.0);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Summon Distance Override Configuration").push("summon_distance_override");
        SWARM_SUMMON_DISTANCE_OVERRIDE_MIN_DISTANCE = SERVER_BUILDER
            .comment("Minimum distance before summons start following player when PASSIVE_SUMMONING_POWER is active (blocks)")
            .defineInRange("minDistance", 50.0, 25.0, 100.0);
        SWARM_SUMMON_DISTANCE_OVERRIDE_MAX_DISTANCE = SERVER_BUILDER
            .comment("Maximum distance before summons stop following player when PASSIVE_SUMMONING_POWER is active (blocks)")
            .defineInRange("maxDistance", 75.0, 50.0, 150.0);
        SWARM_SUMMON_DISTANCE_OVERRIDE_TELEPORT_DISTANCE = SERVER_BUILDER
            .comment("Distance at which summons teleport back to player when PASSIVE_SUMMONING_POWER is active (blocks)")
            .defineInRange("teleportDistance", 100.0, 75.0, 200.0);
        SERVER_BUILDER.pop();
        
        SERVER_BUILDER.comment("Glyph Blacklist Configuration").push("glyph_blacklist");
        GLYPH_BLACKLIST = SERVER_BUILDER
            .comment("List of glyph IDs to ignore for affinity progress tracking. Use format 'modid:glyph_name' (e.g., 'ars_nouveau:effect_heal')")
            .defineList("blacklistedGlyphs", List.of("ars_nouveau:effect_break"), o -> o instanceof String);
        SERVER_BUILDER.pop();
        
        SERVER_CONFIG = SERVER_BUILDER.build();
    }
} 