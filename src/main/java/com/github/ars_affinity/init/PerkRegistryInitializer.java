package com.github.ars_affinity.init;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.PerkRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.stream.Collectors;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PerkRegistryInitializer {
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        ArsAffinity.LOGGER.debug("Initializing PerkRegistry...");
        
        PerkRegistry.initializeFromConfig();
        
        ArsAffinity.LOGGER.debug("PerkRegistry initialization complete. Registered {} perk configurations.", 
            PerkRegistry.getTotalPerkCount());
        
        // Log some sample perks to verify they're loaded
        ArsAffinity.LOGGER.debug("Sample perk keys: {}", 
            PerkRegistry.getAllPerkKeys().stream().limit(10).collect(Collectors.joining(", ")));
    }
}