package com.github.ars_affinity.client.particles;

import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SpiralParticle extends TextureSheetParticle {
    private final float radius;
    private final float speed;
    private final double centerX, centerY, centerZ;
    private final float initialAngle;
    private final float initialQuadSize;

    protected SpiralParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, float scale, int lifetime, SpriteSet sprite) {
        super(worldIn, x, y, z, 0, 0, 0);
        this.radius = 5f * scale;
        this.speed = 0.1f;
        
        // Store the center point (where all particles should spiral around)
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        
        // Each particle gets a random starting angle offset
        this.initialAngle = (float) (Math.random() * 2 * Math.PI);
        
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

        // Calculate spiral position relative to the center point
        double x = centerX + currentRadius * Math.sin(currentAngle);
        double z = centerZ + currentRadius * Math.cos(currentAngle);

        // --- EaseOutExpo on upward movement ---
        double easedY = centerY + easeOutExpo(progress) * 2f; // up to 2 blocks high

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


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
