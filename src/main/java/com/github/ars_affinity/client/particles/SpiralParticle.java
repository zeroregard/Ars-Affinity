package com.github.ars_affinity.client.particles;

import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SpiralParticle extends TextureSheetParticle {
    private final float radius;
    private final float speed;
    private final int playerId;
    private final String schoolId;
    private final float initialAngle;
    private final float initialQuadSize;
    private final float chaosAmount;

    protected SpiralParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, float scale, int lifetime, SpriteSet sprite, int playerId, String schoolId) {
        super(worldIn, x, y, z, 0, 0, 0);
        this.radius = 5f * scale;
        this.speed = 0.05f; // Reduced speed
        
        // Store player and school info for position tracking
        this.playerId = playerId;
        this.schoolId = schoolId;
        
        // Each particle gets a random starting angle offset
        this.initialAngle = (float) (Math.random() * 2 * Math.PI);
        
        // Random chaos intensity per particle for "frizzle" effect
        this.chaosAmount = (float) (Math.random() * 0.5f + 0.25f);
        
        this.quadSize = scale * 0.25f + (float)(Math.random() * 0.05f);
        this.initialQuadSize = this.quadSize;
        this.hasPhysics = false;
        this.xd = ParticleUtil.inRange(-0.01, 0.01);
        this.yd = 0.001;
        this.zd = ParticleUtil.inRange(-0.01, 0.01);
        this.setPos(x, y, z);
        this.friction = 0.99F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.setColor(r, g, b);
        this.lifetime = 60;

        this.pickSprite(sprite);
        
        com.github.ars_affinity.ArsAffinity.LOGGER.info("SpiralParticle created at ({}, {}, {}) with color ({}, {}, {}), size={}, lifetime={}, radius={}, speed={}", 
            x, y, z, r, g, b, this.quadSize, this.lifetime, this.radius, this.speed);
    }

    @Override
    public void tick() {
        super.tick();

        float progress = Math.min((float) this.age / this.lifetime, 1.0f);
        
        // Calculate the current spiral rotation angle (shared by all particles)
        // Accelerate rotational speed over time using easeOutExpo (starts slow, speeds up)
        float rotationalAcceleration = 1.0f + easeOutExpo(progress) * 3.0f; // 1x to 4x speed
        float currentAngle = initialAngle + (this.age * speed * rotationalAcceleration);

        // Spiral radius growth
        float currentRadius = radius * (0.1f + progress * 0.01f);

        // Get current interpolated center from helper
        SpiralParticleHelper.SpiralParticleCenter center = SpiralParticleHelper.getParticleCenter(playerId, schoolId);
        if (center == null) {
            this.remove(); // Remove particle if center is no longer tracked
            return;
        }
        double centerX = center.getCurrentX();
        double centerY = center.getCurrentY();
        double centerZ = center.getCurrentZ();
        
        // Calculate spiral position relative to the interpolated center point
        double x = centerX + currentRadius * Math.sin(currentAngle);
        double z = centerZ + currentRadius * Math.cos(currentAngle);

        // --- EaseOutExpo on upward movement ---
        double easedY = centerY + easeOutExpo(progress) * 2f; // up to 2 blocks high
        
        // --- School-specific chaos modifier using easeInQuad for "frizzle" effect ---
        // Only apply chaos to Air and Fire schools
        if (shouldApplyChaos(schoolId)) {
            float chaosProgress = easeInQuad(progress); // More chaos as particle ages, kicks in earlier
            double chaosX = (Math.random() - 0.5) * 2.0 * chaosAmount * chaosProgress;
            double chaosY = (Math.random() - 0.5) * 2.0 * chaosAmount * chaosProgress;
            double chaosZ = (Math.random() - 0.5) * 2.0 * chaosAmount * chaosProgress;
            
            x += chaosX;
            easedY += chaosY;
            z += chaosZ;
        }

        // --- EaseOutExpo for size fade from initial size to 0 over lifetime ---
        float sizeProgress = Math.min((float) this.age / this.lifetime, 1.0f);
        float sizeFade = 1.0f - easeOutExpo(sizeProgress * 0.75f);
        this.quadSize = this.initialQuadSize * sizeFade;

        this.setPos(x, easedY, z);
    }

    // --- Utility easing ---
    private static float easeOutExpo(float t) {
        return (t == 1.0f) ? 1.0f : (float)(1 - Math.pow(2, -10 * t));
    }
    
    private static float easeInExpo(float t) {
        return (t == 0.0f) ? 0.0f : (float) Math.pow(2, 10 * (t - 1));
    }
    
    private static float easeInQuad(float t) {
        return t * t;
    }
    
    /**
     * Determines if chaos effect should be applied based on the school.
     * Only Air and Fire schools get the chaos "frizzle" effect.
     */
    private static boolean shouldApplyChaos(String schoolId) {
        return "elemental_fire".equals(schoolId) || "elemental_air".equals(schoolId);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}