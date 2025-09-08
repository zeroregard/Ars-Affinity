package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.particles.SpiralParticleProvider;
import com.github.ars_affinity.client.particles.SpiralParticleTypeData;
import com.github.ars_affinity.client.particles.SpiralParticleType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ParticleRegistry {

    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, ArsAffinity.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_PARTICLE_TYPE = PARTICLES.register("spiral_particle", SpiralParticleType::new);

    @SubscribeEvent
    public static void registerFactories(RegisterParticleProvidersEvent evt) {
        evt.registerSpriteSet(SPIRAL_PARTICLE_TYPE.get(), SpiralParticleProvider::new);
    }
}
