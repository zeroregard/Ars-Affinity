package com.github.ars_affinity.client.particles;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

public class SpiralParticleProvider implements ParticleProvider<SpiralParticleTypeData> {
    private final SpriteSet spriteSet;

    public SpiralParticleProvider(SpriteSet spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle createParticle(SpiralParticleTypeData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        ArsAffinity.LOGGER.info("SpiralParticleProvider.createParticle called at ({}, {}, {}) with color ({}, {}, {}), scale={}, lifetime={}, spriteType={}",
            x, y, z, data.color.getRed(), data.color.getGreen(), data.color.getBlue(), data.size, data.age, data.spriteType);
        
        return new SpiralParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(), data.color.getBlue(),
                data.size,
                data.age, spriteSet);
    }
}
