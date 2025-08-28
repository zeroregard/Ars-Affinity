package com.github.ars_affinity.init;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.PerkRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Initializes the PerkRegistry during mod setup.
 * This ensures all perk configurations are loaded before any players join.
 */
@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class PerkRegistryInitializer {
    
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        ArsAffinity.LOGGER.info("Initializing PerkRegistry...");
        
        // Initialize the perk registry from configuration
        PerkRegistry.initializeFromConfig();
        
        ArsAffinity.LOGGER.info("PerkRegistry initialization complete. Registered {} perk configurations.", 
            PerkRegistry.getTotalPerkCount());
    }
}