package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.item.data.AnchorCharmData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ArsAffinity.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AnchorCharmData>> ANCHOR_CHARM_DATA = DATA.register("anchor_charm_data",
            () -> DataComponentType.<AnchorCharmData>builder().persistent(AnchorCharmData.CODEC).networkSynchronized(AnchorCharmData.STREAM_CODEC).build()
    );
} 