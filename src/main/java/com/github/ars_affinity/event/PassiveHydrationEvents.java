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
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check every 20th tick
        if (player.tickCount % 20 != 0) return;
        
        // Get player's affinity progress
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
        if (waterTier == 0) return;
        
        // Check if player has PASSIVE_HYDRATION perk
        AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_HYDRATION, perk -> {
            if (!(perk instanceof AffinityPerk.AmountBasedPerk amountPerk)) return;
            
            // Get wet ticks capability
            WetTicks wetTicks = player.getCapability(WetTicksCapability.WET_TICKS);
            if (wetTicks == null) return;
            
            // Check if player is wet (in water, rain, etc.)
            boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
            
            // Check if player is on fire - if so, remove Hydrated effect and reset wet ticks
            if (player.isOnFire()) {
                player.removeEffect(ModPotions.HYDRATED_EFFECT);
                wetTicks.resetWetTicks();
                return;
            }
            
            if (isWet) {
                // Add 20 wet ticks
                wetTicks.addWetTicks(20);
                
                // Apply or refresh Hydrated effect based on perk amount and wet ticks
                applyHydratedEffect(player, amountPerk.amount, wetTicks.getWetTicks());
                
                ArsAffinity.LOGGER.debug("Player {} is wet, wet ticks: {}", 
                    player.getName().getString(), wetTicks.getWetTicks());
            } else {
                // Player is not wet, gradually reduce wet ticks
                if (wetTicks.getWetTicks() > 0) {
                    wetTicks.addWetTicks(-1);
                }
                
                // Remove Hydrated effect if no longer wet and wet ticks are depleted
                if (wetTicks.getWetTicks() <= 0) {
                    player.removeEffect(ModPotions.HYDRATED_EFFECT);
                }
            }
        });
    }
    
    // We'll check for fire in the tick event instead of damage event
    // since we want to check for being on fire, not just fire damage
    
    private static void applyHydratedEffect(Player player, float maxAmplification, int wetTicks) {
        // At 20 wet ticks: Hydrated I (amplifier 0)
        if (wetTicks >= 20) {
            player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 0, false, false, true));
        }
        
        // At 200 wet ticks and maxAmplification >= 1: Hydrated II (amplifier 1)
        if (wetTicks >= 200 && maxAmplification >= 1.0f && player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 1, false, false, true));
        }
        
        // At 400 wet ticks and maxAmplification >= 2: Hydrated III (amplifier 2)
        if (wetTicks >= 400 && maxAmplification >= 2.0f && player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
            player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, 2, false, false, true));
        }
    }
}