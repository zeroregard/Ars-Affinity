package com.github.arsaffinity;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ArsAffinity.MOD_ID)
public class ArsAffinity {
    public static final String MOD_ID = "arsaffinity";

    public ArsAffinity() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
} 