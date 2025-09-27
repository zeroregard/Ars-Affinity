package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.ParticleEffectPacket;
import com.github.ars_affinity.registry.ModSounds;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class SchoolAffinityPointAllocatedEvents {
    
    // Map of schools to their corresponding point allocation sounds
    private static final Map<SpellSchool, SoundEvent> SCHOOL_SOUNDS = new HashMap<>();
    
    // Map of schools to their corresponding colors
    private static final Map<SpellSchool, String> SCHOOL_COLORS = new HashMap<>();
    
    static {
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_FIRE, ModSounds.TIER_CHANGE_FIRE.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_WATER, ModSounds.TIER_CHANGE_WATER.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_EARTH, ModSounds.TIER_CHANGE_EARTH.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_AIR, ModSounds.TIER_CHANGE_AIR.get());
        SCHOOL_SOUNDS.put(SpellSchools.ABJURATION, ModSounds.TIER_CHANGE_ABJURATION.get());
        SCHOOL_SOUNDS.put(SpellSchools.CONJURATION, ModSounds.TIER_CHANGE_CONJURATION.get());
        SCHOOL_SOUNDS.put(SpellSchools.NECROMANCY, ModSounds.TIER_CHANGE_NECROMANCY.get());
        SCHOOL_SOUNDS.put(SpellSchools.MANIPULATION, ModSounds.TIER_CHANGE_MANIPULATION.get());
    }
    
    static {
        // Define colors for each school using Minecraft color codes
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_FIRE, "§c");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_WATER, "§9");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_EARTH, "§a");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_AIR, "§e");
        SCHOOL_COLORS.put(SpellSchools.ABJURATION, "§d");
        SCHOOL_COLORS.put(SpellSchools.CONJURATION, "§b");
        SCHOOL_COLORS.put(SpellSchools.NECROMANCY, "§8"); 
        SCHOOL_COLORS.put(SpellSchools.MANIPULATION, "§6"); 
    }
    
    @SubscribeEvent
    public static void onPointAllocated(SchoolAffinityPointAllocatedEvent event) {
        if (!event.hasPointsGained()) {
            return;
        }
        
        Player player = event.getPlayer();
        SpellSchool school = event.getSchool();
        int pointsGained = event.getPointsGained();
        
        // Only handle server-side events
        if (player.level().isClientSide()) {
            return;
        }
        
        playPointAllocatedSound(player, school);
        spawnPointAllocatedParticles(player, school, pointsGained);
    }
    
    private static void playPointAllocatedSound(Player player, SpellSchool school) {
        SoundEvent schoolSound = SCHOOL_SOUNDS.get(school);
        if (schoolSound == null) {
            ArsAffinity.LOGGER.warn("No sound found for school: " + school.getId());
            return;
        }
        
        // Play sound at player location for all nearby players to hear
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                schoolSound,
                SoundSource.BLOCKS,
                1.0f, // Volume
                1.0f  // Pitch
        );
    }

    private static void spawnPointAllocatedParticles(Player player, SpellSchool school, int pointsGained) {
        ArsAffinity.LOGGER.info("=== SPAWNING POINT ALLOCATED PARTICLES ===");
        ArsAffinity.LOGGER.info("spawnPointAllocatedParticles called for player {} school {} points {}", 
            player.getName().getString(), school.getId(), pointsGained);
        ArsAffinity.LOGGER.info("Player position: ({}, {}, {})", 
            player.getX(), player.getY(), player.getZ());
        ArsAffinity.LOGGER.info("Player level: {}, isClientSide: {}", 
            player.level().dimension().location(), player.level().isClientSide());
            
        // Calculate particle count based on points gained (more particles for more points)
        int particleCount = 5 + (pointsGained * 3); // 8, 11, 14 particles for 1, 2, 3 points
        
        ArsAffinity.LOGGER.info("Calculated particle count: {} (base 5 + points {} * 3)", particleCount, pointsGained);
        ArsAffinity.LOGGER.info("Creating ParticleEffectPacket with playerId={}, schoolId={}, particleCount={}", 
            player.getId(), school.getId().toString(), particleCount);
        
        // Send particle effect packet to all clients
        ParticleEffectPacket packet = new ParticleEffectPacket(
            player.getId(),
            school.getId().toString(),
            particleCount
        );
        
        ArsAffinity.LOGGER.info("ParticleEffectPacket created successfully");
        ArsAffinity.LOGGER.info("Sending particle packet to nearby clients at position: {}", player.blockPosition());
        
        try {
            Networking.sendToNearbyClient(player.level(), player.blockPosition(), packet);
            ArsAffinity.LOGGER.info("Particle packet sent successfully to nearby clients");
            
            // Schedule position updates every 3 ticks for 60 ticks (3 seconds)
            ParticleUpdateScheduler.startPositionUpdates(player, school.getId().toString());
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to send particle packet: {}", e.getMessage(), e);
        }
        
        ArsAffinity.LOGGER.info("Spawning {} spiral particles for {} point allocation (+{} points)", 
            particleCount, school.getId(), pointsGained);
        ArsAffinity.LOGGER.info("=== PARTICLE SPAWNING COMPLETE ===");
    }
    
}