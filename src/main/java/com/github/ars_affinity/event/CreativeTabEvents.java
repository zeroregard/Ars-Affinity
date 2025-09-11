package com.github.ars_affinity.event;

import com.github.ars_affinity.registry.ModItems;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class CreativeTabEvents {

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().toString().equals("ars_nouveau:general")) {
            event.accept(ModItems.TABLET_OF_AMNESIA.get());
        }
    }
} 