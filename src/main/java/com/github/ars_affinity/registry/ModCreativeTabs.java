package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ArsAffinity.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ARS_AFFINITY_TAB = TABS.register("general", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.ars_affinity"))
            .icon(() -> ModItems.AFFINITY_ANCHOR_CHARM.get().getDefaultInstance())
            .displayItems((params, output) -> {
                output.accept(ModItems.AFFINITY_ANCHOR_CHARM.get().getDefaultInstance());
            })
            .build());
} 