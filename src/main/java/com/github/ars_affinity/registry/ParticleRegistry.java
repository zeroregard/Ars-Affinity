package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.client.particles.SpiralParticleTypeData;
import com.github.ars_affinity.client.particles.SpiralParticleType;
import com.github.ars_affinity.client.particles.VanillaWrappedSpiralProvider;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
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

    // School-specific spiral particle types that use vanilla sprites
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_FIRE = PARTICLES.register("spiral_fire", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_WATER = PARTICLES.register("spiral_water", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_EARTH = PARTICLES.register("spiral_earth", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_AIR = PARTICLES.register("spiral_air", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_MANIPULATION = PARTICLES.register("spiral_manipulation", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_ABJURATION = PARTICLES.register("spiral_abjuration", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_NECROMANCY = PARTICLES.register("spiral_necromancy", SpiralParticleType::new);
    public static final DeferredHolder<ParticleType<?>, ParticleType<SpiralParticleTypeData>> SPIRAL_CONJURATION = PARTICLES.register("spiral_conjuration", SpiralParticleType::new);

    @SubscribeEvent
    public static void registerFactories(RegisterParticleProvidersEvent evt) {
        // School-specific particles using vanilla sprites
        evt.registerSpriteSet(SPIRAL_FIRE.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.FLAME, sprites));
        evt.registerSpriteSet(SPIRAL_WATER.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.BUBBLE, sprites));
                evt.registerSpriteSet(SPIRAL_EARTH.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.DUST_PLUME, sprites));
                evt.registerSpriteSet(SPIRAL_AIR.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.ELECTRIC_SPARK, sprites));
        evt.registerSpriteSet(SPIRAL_MANIPULATION.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.FIREWORK, sprites));
        evt.registerSpriteSet(SPIRAL_ABJURATION.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.HEART, sprites));
        evt.registerSpriteSet(SPIRAL_NECROMANCY.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.SOUL, sprites));
        evt.registerSpriteSet(SPIRAL_CONJURATION.get(), (sprites) -> new VanillaWrappedSpiralProvider(ParticleTypes.ENCHANT, sprites));
    }
}

