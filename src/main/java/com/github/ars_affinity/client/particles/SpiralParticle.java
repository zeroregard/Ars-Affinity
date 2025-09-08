package com.github.ars_affinity.client.particles;

import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

public class SpiralParticle extends TextureSheetParticle {
    private final float radius;
    private final float speed;
    private float angle;
    public float initScale;
    private final float initialQuadSize;

    protected SpiralParticle(ClientLevel worldIn, double x, double y, double z, double vx, double vy, double vz, float r, float g, float b, float scale, int lifetime, SpriteSet sprite) {
        super(worldIn, x, y, z, 0, 0, 0);
        this.radius = 0.1f * scale; // Smaller radius (50% of original)
        this.speed = 0.3f; // Much faster spiral (100% faster)
        this.angle = (float) (Math.random() * 2 * Math.PI);
        this.quadSize = scale * 0.15f + (float)(Math.random() * 0.05f); // Smaller base size (50% of original)
        this.initialQuadSize = this.quadSize;
        this.hasPhysics = false;
        this.initScale = scale * 0.01f; // Smaller initial scale
        this.xd = ParticleUtil.inRange(-0.01, 0.01); // Less horizontal movement
        this.yd = 0.15; // Upward movement (2 blocks in ~10 ticks)
        this.zd = ParticleUtil.inRange(-0.01, 0.01);

        this.friction = 0.99F; // More friction for controlled movement
        this.speedUpWhenYMotionIsBlocked = false; // Don't speed up when blocked
        this.setColor(r, g, b);
        this.lifetime = 20; // Fixed 1 second lifetime (20 ticks)

        this.pickSprite(sprite);
        
        com.github.ars_affinity.ArsAffinity.LOGGER.info("SpiralParticle created at ({}, {}, {}) with color ({}, {}, {}), size={}, lifetime={}, radius={}, speed={}", 
            x, y, z, r, g, b, this.quadSize, this.lifetime, this.radius, this.speed);
    }

    @Override
    public void tick() {
        super.tick();

        angle += speed;
        if (angle > 2 * Math.PI) {
            angle -= 2 * Math.PI;
        }
        
        // Create upward spiral movement from spawn position
        float spiralProgress = Math.min((float) this.age / this.lifetime, 1.0f);
        float currentRadius = radius * (0.3f + spiralProgress * 0.7f); // Radius grows slightly over time
        
        // Spiral movement around the spawn position
        double x = this.x + currentRadius * Math.sin(angle);
        double z = this.z + currentRadius * Math.cos(angle);
        
        // Upward movement - particles go from bottom to 2 blocks above spawn position
        double y = this.y + (spiralProgress * 2.0); // 2 blocks in 20 ticks (1 second)

        // Size fading - particles fade to 0 after 0.5 seconds (10 ticks)
        float sizeProgress = Math.min((float) this.age / 10.0f, 1.0f);
        float sizeFade = 1.0f - sizeProgress; // Fade from 1.0 to 0.0
        this.quadSize = this.initialQuadSize * sizeFade;

        this.setPos(x, y, z);
    }


    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
