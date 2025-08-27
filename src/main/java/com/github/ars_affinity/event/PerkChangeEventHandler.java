package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Event handler for perk change events.
 * This demonstrates how to listen for perk changes and can be used for
 * debugging, logging, or implementing additional perk-related functionality.
 */
@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PerkChangeEventHandler {
    
    @SubscribeEvent
    public static void onPerkChange(PerkChangeEvent event) {
        if (event.isPerkGained()) {
            ArsAffinity.LOGGER.info("Player {} gained {} perk from {} school at tier {}", 
                event.getEntity().getName().getString(),
                event.getPerkType(),
                event.getSourceSchool(),
                event.getSourceTier());
        } else if (event.isPerkLost()) {
            ArsAffinity.LOGGER.info("Player {} lost {} perk", 
                event.getEntity().getName().getString(),
                event.getPerkType());
        } else if (event.isPerkUpgraded()) {
            ArsAffinity.LOGGER.info("Player {} upgraded {} perk from tier {} to {} ({} school)", 
                event.getEntity().getName().getString(),
                event.getPerkType(),
                event.getOldPerkData().sourceTier,
                event.getSourceTier(),
                event.getSourceSchool());
        } else if (event.isPerkDowngraded()) {
            ArsAffinity.LOGGER.info("Player {} downgraded {} perk from tier {} to {} ({} school)", 
                event.getEntity().getName().getString(),
                event.getPerkType(),
                event.getOldPerkData().sourceTier,
                event.getSourceTier(),
                event.getSourceSchool());
        } else if (event.isSourceSchoolChanged()) {
            ArsAffinity.LOGGER.info("Player {} changed {} perk source from {} school to {} school", 
                event.getEntity().getName().getString(),
                event.getPerkType(),
                event.getOldPerkData().sourceSchool,
                event.getSourceSchool());
        }
    }
}