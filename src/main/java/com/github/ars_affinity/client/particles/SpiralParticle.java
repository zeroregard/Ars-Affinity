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
        this.radius = 0.3f * scale;
        this.speed = 0.2f;
        this.angle = (float) (Math.random() * 2 * Math.PI);
        this.quadSize = scale * 0.15f + (float)(Math.random() * 0.05f);
        this.initialQuadSize = this.quadSize;
        this.hasPhysics = false;
        this.initScale = scale * 0.01f;
        this.xd = ParticleUtil.inRange(-0.01, 0.01);
        this.yd = 0.001;
        this.zd = ParticleUtil.inRange(-0.01, 0.01);
        this.setPos(x, y, z);
        this.friction = 0.99F;
        this.speedUpWhenYMotionIsBlocked = false;
        this.setColor(r, g, b);
        this.lifetime = 40;

        this.pickSprite(sprite);
        
        com.github.ars_affinity.ArsAffinity.LOGGER.info("SpiralParticle created at ({}, {}, {}) with color ({}, {}, {}), size={}, lifetime={}, radius={}, speed={}", 
            x, y, z, r, g, b, this.quadSize, this.lifetime, this.radius, this.speed);
    }

    @Override
    public void tick() {
        super.tick();

        angle += speed;

        float progress = Math.min((float) this.age / this.lifetime, 1.0f);

        // Spiral radius growth
        float currentRadius = radius * (0.3f + progress * 0.7f);

        // Spiral X/Z
        double x = this.x + currentRadius * Math.sin(angle);
        double z = this.z + currentRadius * Math.cos(angle);

        // --- EaseOutExpo on upward movement ---
        double easedY = this.y + easeOutExpo(progress) * 1.0; // up to 2 blocks high

        // --- EaseOutExpo inverse for size fade ---
        float sizeProgress = Math.min((float) this.age / 10.0f, 1.0f);
        float sizeFade = 1.0f - easeOutExpo(sizeProgress);
        // this.quadSize = this.initialQuadSize * sizeFade;

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
