package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ArsAffinity.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> ESSENCE_TYPE = DATA.register("essence_type", () -> DataComponentType.<String>builder().persistent(com.mojang.serialization.Codec.STRING).build());

} 