package com.github.ars_affinity.client.particles;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.github.ars_affinity.registry.ParticleRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SpiralParticleHelper {
    
    private static final Map<SpellSchool, ParticleColor> SCHOOL_COLORS = new HashMap<>();
    private static final Map<SpellSchool, Float> SCHOOL_SCALES = new HashMap<>();
    
    // Track active particle effects for position updates (no memory leak - auto-cleanup)
    private static final Map<String, SpiralParticleCenter> activeEffects = new ConcurrentHashMap<>();
    
    static {
        // Fire - Red/Orange
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_FIRE, new ParticleColor(255, 100, 0));
        SCHOOL_SCALES.put(SpellSchools.ELEMENTAL_FIRE, 1.2f);
        
        // Water - Blue/Cyan
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_WATER, new ParticleColor(0, 150, 255));
        SCHOOL_SCALES.put(SpellSchools.ELEMENTAL_WATER, 1.0f);
        
        // Earth - Brown/Green
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_EARTH, new ParticleColor(139, 69, 19));
        SCHOOL_SCALES.put(SpellSchools.ELEMENTAL_EARTH, 1.1f);
        
        // Air - White/Light Blue
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_AIR, new ParticleColor(200, 220, 255));
        SCHOOL_SCALES.put(SpellSchools.ELEMENTAL_AIR, 0.9f);
        
        // Manipulation - Purple
        SCHOOL_COLORS.put(SpellSchools.MANIPULATION, new ParticleColor(128, 0, 128));
        SCHOOL_SCALES.put(SpellSchools.MANIPULATION, 1.0f);
        
        // Abjuration - Gold/Yellow (holy looking)
        SCHOOL_COLORS.put(SpellSchools.ABJURATION, new ParticleColor(255, 215, 0));
        SCHOOL_SCALES.put(SpellSchools.ABJURATION, 1.1f);
        
        // Necromancy - Dark Purple/Black (death looking)
        SCHOOL_COLORS.put(SpellSchools.NECROMANCY, new ParticleColor(75, 0, 130));
        SCHOOL_SCALES.put(SpellSchools.NECROMANCY, 1.0f);
        
        // Conjuration - Green (nature/summoning)
        SCHOOL_COLORS.put(SpellSchools.CONJURATION, new ParticleColor(0, 128, 0));
        SCHOOL_SCALES.put(SpellSchools.CONJURATION, 1.0f);
    }
    
    public static void spawnSpiralParticles(ClientLevel level, Player player, SpellSchool school, int particleCount) {
        spawnSpiralParticles(level, player, school, particleCount, 0);
    }
    
    public static void spawnSpiralParticlesAtPosition(ClientLevel level, Vec3 position, SpellSchool school, int particleCount) {
        ArsAffinity.LOGGER.debug("SpiralParticleHelper.spawnSpiralParticlesAtPosition called at position ({}, {}, {})", 
            position.x, position.y, position.z);
            
        if (level == null || position == null || school == null) {
            ArsAffinity.LOGGER.warn("SpiralParticleHelper: Null parameters detected for position spawn");
            return;
        }
        
        ParticleColor color = SCHOOL_COLORS.get(school);
        Float scale = SCHOOL_SCALES.get(school);
        
        if (color == null || scale == null) {
            color = new ParticleColor(255, 255, 255);
            scale = 1.0f;
        }
        
        double x = position.x;
        double y = position.y;
        double z = position.z;
        
        int successfulSpawns = 0;
        for (int i = 0; i < particleCount; i++) {
            String spriteType = getSpriteTypeForSchool(school);
            SpiralParticleTypeData particleData = new SpiralParticleTypeData(
                ParticleRegistry.SPIRAL_PARTICLE_TYPE.get(),
                color, 
                false, 
                1.0f,
                1.0f, 
                40,
                spriteType,
                0,
                "default"
            );
            
            try {
                level.addParticle(
                    particleData,
                    x,
                    y,
                    z,
                    0, 0, 0
                );
                successfulSpawns++;
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("SpiralParticleHelper: Error spawning particle {}: {}", i, e.getMessage(), e);
            }
        }
        
        ArsAffinity.LOGGER.debug("SpiralParticleHelper: Successfully spawned {}/{} particles at position", successfulSpawns, particleCount);
    }
    
    public static void spawnSpiralParticles(ClientLevel level, Player player, SpellSchool school, int particleCount, int delayTicks) {
        ArsAffinity.LOGGER.info("=== SPIRAL PARTICLE HELPER START ===");
        ArsAffinity.LOGGER.info("SpiralParticleHelper.spawnSpiralParticles called with level={}, player={}, school={}, count={}",
            level != null ? "ClientLevel" : "null", 
            player != null ? player.getName().getString() : "null", 
            school != null ? school.getId() : "null", 
            particleCount);
            
        if (level == null || player == null || school == null) {
            ArsAffinity.LOGGER.warn("SpiralParticleHelper: Null parameters detected, aborting particle spawn");
            ArsAffinity.LOGGER.warn("SpiralParticleHelper: level={}, player={}, school={}", 
                level != null, player != null, school != null);
            ArsAffinity.LOGGER.info("=== SPIRAL PARTICLE HELPER END (NULL PARAMS) ===");
            return;
        }
        
        ParticleColor color = SCHOOL_COLORS.get(school);
        Float scale = SCHOOL_SCALES.get(school);
        
        if (color == null || scale == null) {
            // Default to white particles if school not found
            ArsAffinity.LOGGER.warn("SpiralParticleHelper: School not found in color/scale maps, using defaults");
            ArsAffinity.LOGGER.warn("SpiralParticleHelper: Available schools: {}", SCHOOL_COLORS.keySet());
            color = new ParticleColor(255, 255, 255);
            scale = 1.0f;
        }
        
        ArsAffinity.LOGGER.info("SpiralParticleHelper: Using color={}, scale={}", color, scale);
        
        Vec3 playerPos = player.position();
        double x = playerPos.x;
        double y = playerPos.y; // Spawn at bottom of player
        double z = playerPos.z;
        
        ArsAffinity.LOGGER.info("SpiralParticleHelper: Player position: ({}, {}, {}), eye height: {}", 
            playerPos.x, playerPos.y, playerPos.z, player.getEyeHeight());
        ArsAffinity.LOGGER.info("SpiralParticleHelper: Spawning particles at position ({}, {}, {})", x, y, z);
        ArsAffinity.LOGGER.info("SpiralParticleHelper: Particle type: {}", ParticleRegistry.SPIRAL_PARTICLE_TYPE.get());
        
        // Register the particle effect for position tracking
        registerParticleEffect(player.getId(), school.getId().toString(), x, y, z);
        
        int successfulSpawns = 0;
        for (int i = 0; i < particleCount; i++) {
            // Add some randomness to the spawn position
            double offsetX = 0;  //(Math.random() - 0.5) * 2.0;
            double offsetY = 0;  // (Math.random() - 0.5) * 1.0;
            double offsetZ = 0; // (Math.random() - 0.5) * 2.0;
            
            String spriteType = getSpriteTypeForSchool(school);
            SpiralParticleTypeData particleData = new SpiralParticleTypeData(
                ParticleRegistry.SPIRAL_PARTICLE_TYPE.get(),
                color, 
                false, 
                1.0f,
                1.0f, 
                40,
                spriteType,
                player.getId(),
                school.getId().toString()
            );
            
            try {
                level.addParticle(
                    particleData,
                    x + offsetX,
                    y + offsetY,
                    z + offsetZ,
                    0, 0, 0
                );
                successfulSpawns++;
                
                if (i < 5) { // Log first 5 particles for debugging
                    ArsAffinity.LOGGER.info("SpiralParticleHelper: Spawned particle {} at ({}, {}, {})", 
                        i, x + offsetX, y + offsetY, z + offsetZ);
                }
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("SpiralParticleHelper: Error spawning particle {}: {}", i, e.getMessage(), e);
            }
        }
        
        ArsAffinity.LOGGER.info("SpiralParticleHelper: Successfully spawned {}/{} particles", successfulSpawns, particleCount);
        ArsAffinity.LOGGER.info("=== SPIRAL PARTICLE HELPER END ===");
    }
    
    public static ParticleColor getSchoolColor(SpellSchool school) {
        return SCHOOL_COLORS.getOrDefault(school, new ParticleColor(255, 255, 255));
    }
    
    public static float getSchoolScale(SpellSchool school) {
        return SCHOOL_SCALES.getOrDefault(school, 1.0f);
    }
    
    public static String getSpriteTypeForSchool(SpellSchool school) {
        return switch (school.getId().toString()) {
            case "fire" -> "flame";
            case "elemental_water" -> "bubble";
            default -> "flame"; // Use default generic sprites for all other schools
        };
    }
    
    // Particle center tracking methods
    public static void updateParticleCenter(int playerId, String schoolId, double x, double y, double z) {
        String key = playerId + "_" + schoolId;
        SpiralParticleCenter center = activeEffects.get(key);
        if (center != null) {
            center.updateTargetPosition(x, y, z);
        }
    }
    
    public static void registerParticleEffect(int playerId, String schoolId, double x, double y, double z) {
        String key = playerId + "_" + schoolId;
        activeEffects.put(key, new SpiralParticleCenter(x, y, z));
    }
    
    public static void unregisterParticleEffect(int playerId, String schoolId) {
        String key = playerId + "_" + schoolId;
        activeEffects.remove(key);
    }
    
    public static SpiralParticleCenter getParticleCenter(int playerId, String schoolId) {
        String key = playerId + "_" + schoolId;
        return activeEffects.get(key);
    }
    
    // Inner class for particle center with auto-cleanup
    public static class SpiralParticleCenter {
        private double currentX, currentY, currentZ;
        private double targetX, targetY, targetZ;
        private double prevX, prevY, prevZ;
        private long lastUpdateTime;
        private long creationTime;
        
        public SpiralParticleCenter(double x, double y, double z) {
            this.currentX = this.targetX = this.prevX = x;
            this.currentY = this.targetY = this.prevY = y;
            this.currentZ = this.targetZ = this.prevZ = z;
            this.lastUpdateTime = System.currentTimeMillis();
            this.creationTime = System.currentTimeMillis();
        }
        
        public void updateTargetPosition(double x, double y, double z) {
            // Update position immediately - no interpolation
            this.currentX = x;
            this.currentY = y;
            this.currentZ = z;
            this.lastUpdateTime = System.currentTimeMillis();
        }
        
        public double getCurrentX() {
            return currentX;
        }
        
        public double getCurrentY() {
            return currentY;
        }
        
        public double getCurrentZ() {
            return currentZ;
        }
        
        // Auto-cleanup after 5 seconds
        public boolean isExpired() {
            return System.currentTimeMillis() - creationTime > 5000;
        }
    }
}
