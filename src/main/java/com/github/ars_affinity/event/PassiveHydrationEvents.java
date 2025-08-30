package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.WetTicks;
import com.github.ars_affinity.capability.WetTicksCapability;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveHydrationEvents {
    
    private static final java.util.Map<java.util.UUID, Integer> lastFoodLevels = new java.util.HashMap<>();
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check every 20th tick
        if (player.tickCount % 20 != 0) return;
        
        ArsAffinity.LOGGER.debug("HYDRATION - Processing tick for player: {} (tick: {})", 
            player.getName().getString(), player.tickCount);
        
        // Get player's affinity progress
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            ArsAffinity.LOGGER.debug("HYDRATION - No affinity progress found for player: {}", 
                player.getName().getString());
            return;
        }
        
        int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
        if (waterTier == 0) {
            ArsAffinity.LOGGER.debug("HYDRATION - Player {} has no water affinity tier", 
                player.getName().getString());
            return;
        }
        
        ArsAffinity.LOGGER.debug("HYDRATION - Player {} has water tier: {}", 
            player.getName().getString(), waterTier);
        
        // Check if player has PASSIVE_HYDRATION perk
        AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_HYDRATION, perk -> {
            if (!(perk instanceof AffinityPerk.AmountBasedPerk amountPerk)) {
                ArsAffinity.LOGGER.warn("HYDRATION - Player {} has PASSIVE_HYDRATION but perk is not AmountBasedPerk: {}", 
                    player.getName().getString(), perk.getClass().getSimpleName());
                return;
            }
            
            ArsAffinity.LOGGER.debug("HYDRATION - Player {} has PASSIVE_HYDRATION perk with amount: {}", 
                player.getName().getString(), amountPerk.amount);
            
            // Get wet ticks capability
            WetTicks wetTicks = player.getCapability(WetTicksCapability.WET_TICKS);
            if (wetTicks == null) {
                ArsAffinity.LOGGER.warn("HYDRATION - Player {} has no WetTicks capability", 
                    player.getName().getString());
                return;
            }
            
            // Check if player is wet (in water, rain, etc.)
            boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
            boolean isOnFire = player.isOnFire();
            
            ArsAffinity.LOGGER.debug("HYDRATION - Player {} - Wet: {}, OnFire: {}, Current wet ticks: {}", 
                player.getName().getString(), isWet, isOnFire, wetTicks.getWetTicks());
            
            // Check if player is on fire - if so, remove Hydrated effect and reset wet ticks
            if (isOnFire) {
                ArsAffinity.LOGGER.info("HYDRATION - Player {} is on fire, removing Hydrated effect and resetting wet ticks", 
                    player.getName().getString());
                player.removeEffect(ModPotions.HYDRATED_EFFECT);
                wetTicks.resetWetTicks();
                lastFoodLevels.remove(player.getUUID());
                return;
            }
            
            // Handle hunger prevention for Hydrated I effect
            handleHungerPrevention(player);
            
            if (isWet) {
                // Add 20 wet ticks
                int oldWetTicks = wetTicks.getWetTicks();
                wetTicks.addWetTicks(20);
                int newWetTicks = wetTicks.getWetTicks();
                
                ArsAffinity.LOGGER.debug("HYDRATION - Player {} is wet, wet ticks: {} -> {}", 
                    player.getName().getString(), oldWetTicks, newWetTicks);
                
                // Apply or refresh Hydrated effect based on perk amount and wet ticks
                applyHydratedEffect(player, amountPerk.amount, wetTicks.getWetTicks());
                
            } else {
                // Player is not wet, gradually reduce wet ticks
                if (wetTicks.getWetTicks() > 0) {
                    int oldWetTicks = wetTicks.getWetTicks();
                    wetTicks.addWetTicks(-1);
                    int newWetTicks = wetTicks.getWetTicks();
                    
                    ArsAffinity.LOGGER.debug("HYDRATION - Player {} is not wet, reducing wet ticks: {} -> {}", 
                        player.getName().getString(), oldWetTicks, newWetTicks);
                }
                
                // Remove Hydrated effect if no longer wet and wet ticks are depleted
                if (wetTicks.getWetTicks() <= 0) {
                    ArsAffinity.LOGGER.info("HYDRATION - Player {} has no wet ticks remaining, removing Hydrated effect", 
                        player.getName().getString());
                    player.removeEffect(ModPotions.HYDRATED_EFFECT);
                    lastFoodLevels.remove(player.getUUID());
                }
            }
        });
    }
    
    private static void handleHungerPrevention(Player player) {
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
                    ArsAffinity.LOGGER.info("HYDRATION - Player {} hunger decreased from {} to {}, restoring to {}", 
                        player.getName().getString(), lastFoodLevel, currentFoodLevel, lastFoodLevel);
                    player.getFoodData().setFoodLevel(lastFoodLevel);
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
    
    private static void applyHydratedEffect(Player player, float maxAmplification, int wetTicks) {
        ArsAffinity.LOGGER.debug("HYDRATION - Applying Hydrated effect for player {} - Max amplification: {}, Wet ticks: {}", 
            player.getName().getString(), maxAmplification, wetTicks);
        
        // At 20 wet ticks: Hydrated I (amplifier 0)
        if (wetTicks >= 20) {
            if (!player.hasEffect(ModPotions.HYDRATED_EFFECT) || 
                player.getEffect(ModPotions.HYDRATED_EFFECT).getAmplifier() != 0) {
                ArsAffinity.LOGGER.info("HYDRATION - Player {} applying Hydrated I (amplifier 0)", 
                    player.getName().getString());
                player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 0, false, false, true));
            }
        }
        
        // At 200 wet ticks and maxAmplification >= 1: Hydrated II (amplifier 1)
        if (wetTicks >= 200 && maxAmplification >= 1.0f && player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            if (player.getEffect(ModPotions.HYDRATED_EFFECT).getAmplifier() < 1) {
                ArsAffinity.LOGGER.info("HYDRATION - Player {} upgrading to Hydrated II (amplifier 1)", 
                    player.getName().getString());
                player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 1, false, false, true));
            }
        }
        
        // At 400 wet ticks and maxAmplification >= 2: Hydrated III (amplifier 2)
        if (wetTicks >= 400 && maxAmplification >= 2.0f && player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            if (player.getEffect(ModPotions.HYDRATED_EFFECT).getAmplifier() < 2) {
                ArsAffinity.LOGGER.info("HYDRATION - Player {} upgrading to Hydrated III (amplifier 2)", 
                    player.getName().getString());
                player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 2, false, false, true));
            }
        }
        
        // Log current effect status
        if (player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            var effect = player.getEffect(ModPotions.HYDRATED_EFFECT);
            ArsAffinity.LOGGER.debug("HYDRATION - Player {} current Hydrated effect: amplifier {}, duration {}", 
                player.getName().getString(), effect.getAmplifier(), effect.getDuration());
        }
    }
}