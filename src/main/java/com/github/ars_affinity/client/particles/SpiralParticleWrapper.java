package com.github.ars_affinity.client.particles;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.resources.ResourceLocation;

public class SpiralParticleWrapper implements ParticleProvider<SpiralParticleTypeData> {
    private final SpriteSet defaultSpriteSet;
    private final SpriteSet bubbleSpriteSet;
    private final SpriteSet flameSpriteSet;

    public SpiralParticleWrapper(SpriteSet defaultSpriteSet) {
        this.defaultSpriteSet = defaultSpriteSet;
        
        // Try to get bubble sprite set
        SpriteSet bubble = null;
        SpriteSet flame = null;
        
        try {
            // Use reflection to access the private spriteSets field
            var particleEngine = Minecraft.getInstance().particleEngine;
            var spriteSetsField = particleEngine.getClass().getDeclaredField("spriteSets");
            spriteSetsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            var spriteSets = (java.util.Map<ResourceLocation, SpriteSet>) spriteSetsField.get(particleEngine);
            
            bubble = spriteSets.get(ResourceLocation.withDefaultNamespace("bubble"));
            flame = spriteSets.get(ResourceLocation.withDefaultNamespace("flame"));
            
            ArsAffinity.LOGGER.info("Successfully loaded sprite sets - bubble: {}, flame: {}", 
                bubble != null, flame != null);
        } catch (Exception e) {
            ArsAffinity.LOGGER.warn("Failed to load sprite sets via reflection: {}", e.getMessage());
        }
        
        this.bubbleSpriteSet = bubble;
        this.flameSpriteSet = flame;
    }

    @Override
    public Particle createParticle(SpiralParticleTypeData data, ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        ArsAffinity.LOGGER.info("SpiralParticleWrapper.createParticle called at ({}, {}, {}) with color ({}, {}, {}), scale={}, lifetime={}, spriteType={}",
            x, y, z, data.color.getRed(), data.color.getGreen(), data.color.getBlue(), data.size, data.age, data.spriteType);
        
        // Get the appropriate sprite set based on the sprite type
        SpriteSet spriteSet = getSpriteSetForType(data.spriteType);
        
        return new SpiralParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(), data.color.getBlue(),
                data.size,
                data.age, spriteSet, data.playerId, data.schoolId);
    }
    
    private SpriteSet getSpriteSetForType(String spriteType) {
        return switch (spriteType) {
            case "bubble" -> bubbleSpriteSet != null ? bubbleSpriteSet : defaultSpriteSet;
            case "flame" -> flameSpriteSet != null ? flameSpriteSet : defaultSpriteSet;
            default -> defaultSpriteSet;
        };
    }
}
