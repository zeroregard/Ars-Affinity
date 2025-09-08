package com.github.ars_affinity.client.particles;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;

import java.util.HashMap;
import java.util.Map;

public class SpiralParticleProvider implements ParticleProvider<SpiralParticleTypeData> {
    private final Map<String, SpriteSet> spriteSets = new HashMap<>();
    private final SpriteSet defaultSpriteSet;

    public SpiralParticleProvider(SpriteSet defaultSprite) {
        this.defaultSpriteSet = defaultSprite;
    }
    
    public void registerSpriteSet(String spriteType, SpriteSet spriteSet) {
        this.spriteSets.put(spriteType, spriteSet);
    }

    @Override
    public Particle createParticle(SpiralParticleTypeData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        ArsAffinity.LOGGER.info("SpiralParticleProvider.createParticle called at ({}, {}, {}) with color ({}, {}, {}), scale={}, lifetime={}, spriteType={}",
            x, y, z, data.color.getRed(), data.color.getGreen(), data.color.getBlue(), data.size, data.age, data.spriteType);
        
        SpriteSet spriteSet = getSpriteSetForType(data.spriteType);
        
        return new SpiralParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(), data.color.getBlue(),
                data.size,
                data.age, spriteSet);
    }
    
    private SpriteSet getSpriteSetForType(String spriteType) {
        // Try to get from cache first
        if (spriteSets.containsKey(spriteType)) {
            return spriteSets.get(spriteType);
        }
        
        // For now, just use default sprite set for all types
        // TODO: Implement proper sprite set loading when needed
        ArsAffinity.LOGGER.debug("Using default sprite set for type: {}", spriteType);
        return defaultSpriteSet;
    }
}
