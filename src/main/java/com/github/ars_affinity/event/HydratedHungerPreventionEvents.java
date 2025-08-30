package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class HydratedHungerPreventionEvents {
    
    private static final java.util.Map<java.util.UUID, Integer> lastFoodLevels = new java.util.HashMap<>();
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if player has Hydrated effect at amplifier 0
        if (player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            var effect = player.getEffect(ModPotions.HYDRATED_EFFECT);
            if (effect != null && effect.getAmplifier() == 0) {
                // At amplifier 0, prevent hunger from decreasing
                java.util.UUID playerId = player.getUUID();
                int currentFoodLevel = player.getFoodData().getFoodLevel();
                
                // Get the last known food level
                Integer lastFoodLevel = lastFoodLevels.get(playerId);
                if (lastFoodLevel != null && currentFoodLevel < lastFoodLevel) {
                    // Food level decreased, restore it
                    player.getFoodData().setFoodLevel(lastFoodLevel);
                    ArsAffinity.LOGGER.debug("Prevented hunger decrease for player {} (restored from {} to {})", 
                        player.getName().getString(), currentFoodLevel, lastFoodLevel);
                }
                
                // Update the last food level
                lastFoodLevels.put(playerId, player.getFoodData().getFoodLevel());
            } else {
                // Player doesn't have Hydrated I, allow normal hunger behavior
                lastFoodLevels.remove(player.getUUID());
            }
        } else {
            // Player doesn't have Hydrated effect, allow normal hunger behavior
            lastFoodLevels.remove(player.getUUID());
        }
    }
}