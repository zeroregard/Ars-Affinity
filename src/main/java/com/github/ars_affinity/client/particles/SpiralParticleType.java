package com.github.ars_affinity.client.particles;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class SpiralParticleType extends ParticleType<SpiralParticleTypeData> {
    public SpiralParticleType() {
        super(false);
    }

    @Override
    public MapCodec<SpiralParticleTypeData> codec() {
        return SpiralParticleTypeData.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, SpiralParticleTypeData> streamCodec() {
        return SpiralParticleTypeData.STREAM_CODEC;
    }
}

