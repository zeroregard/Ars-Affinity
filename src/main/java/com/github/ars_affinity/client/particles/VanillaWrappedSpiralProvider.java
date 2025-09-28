package com.github.ars_affinity.client.particles;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class VanillaWrappedSpiralProvider implements ParticleProvider<SpiralParticleTypeData> {

    private final SpriteSet sprite;

    public VanillaWrappedSpiralProvider(ParticleType<?> originalType, SpriteSet customSpriteSet) {
        ResourceLocation key = BuiltInRegistries.PARTICLE_TYPE.getKey(originalType);
        SpriteSet spriteSet = Minecraft.getInstance().particleEngine.spriteSets.get(key);
        this.sprite = spriteSet != null ? spriteSet : customSpriteSet; // Fallback to custom if vanilla not found
        ArsAffinity.LOGGER.debug("VanillaWrappedSpiralProvider created for particle type: {} with sprite: {}", originalType, spriteSet != null ? "vanilla" : "custom");
    }

    @Override
    public @Nullable Particle createParticle(SpiralParticleTypeData data, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        ArsAffinity.LOGGER.debug("VanillaWrappedSpiralProvider.createParticle called with vanilla sprites");
        
        // Create our SpiralParticle using the vanilla sprite set
        return new SpiralParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, data.color.getRed(), data.color.getGreen(), data.color.getBlue(),
                data.size,
                data.age, this.sprite, data.playerId, data.schoolId);
    }
}