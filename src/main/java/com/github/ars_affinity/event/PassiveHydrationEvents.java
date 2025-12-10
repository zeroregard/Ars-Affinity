package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.WetTicks;
import com.github.ars_affinity.capability.WetTicksCapability;
import com.github.ars_affinity.capability.WetTicksProvider;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class PassiveHydrationEvents {
    
    private static final java.util.Map<java.util.UUID, Integer> lastFoodLevels = new java.util.HashMap<>();
    
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check every 20th tick for hydration logic
        if (player.tickCount % 20 != 0) return;
        
        // Get player's affinity progress
        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            return;
        }
        
        int waterPoints = data.getSchoolPoints(SpellSchools.ELEMENTAL_WATER);
        if (waterPoints == 0) {
            return;
        }

        // Check if player has the hydration perk
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_HYDRATION)) {
            float amount = AffinityPerkHelper.getPerkAmount(player, AffinityPerkType.PASSIVE_HYDRATION);
            
            // Get wet ticks capability
            WetTicks wetTicks = player.getCapability(WetTicksCapability.WET_TICKS);
            if (wetTicks == null) {
                return;
            }
            
            // Check if player is wet (in water, rain, etc.)
            boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
            boolean isOnFire = player.isOnFire();

            if (isOnFire) {
                player.removeEffect(ModPotions.HYDRATED_EFFECT);
                wetTicks.resetWetTicks();
                // Save the reset wet ticks
                WetTicksProvider.savePlayerWetTicks(player);
                lastFoodLevels.remove(player.getUUID());
                return;
            }
            
            if (isWet) {
                wetTicks.addWetTicks(20);
                int newWetTicks = wetTicks.getWetTicks();
                WetTicksProvider.savePlayerWetTicks(player);
                applyHydratedEffect(player, amount, newWetTicks);
                
            } else {
                if (wetTicks.getWetTicks() > 0) {
                    ArsAffinity.LOGGER.debug("HYDRATION - Player {} is not wet, resetting wet ticks to 0", 
                        player.getName().getString());
                    wetTicks.resetWetTicks();
                    // Save the reset wet ticks
                    WetTicksProvider.savePlayerWetTicks(player);
                }
                if (player.hasEffect(ModPotions.HYDRATED_EFFECT)) {
                    ArsAffinity.LOGGER.debug("HYDRATION - Player {} is no longer wet, removing Hydrated effect", 
                        player.getName().getString());
                    player.removeEffect(ModPotions.HYDRATED_EFFECT);
                    lastFoodLevels.remove(player.getUUID());
                }
            }
        }
    }
    
    private static void applyHydratedEffect(Player player, float maxAmplification, int wetTicks) {
        ArsAffinity.LOGGER.debug("HYDRATION - Applying Hydrated effect for player {} - Max amplification: {}, Wet ticks: {}",
            player.getName().getString(), maxAmplification, wetTicks);

        int targetAmplifier = 0;
        if (wetTicks >= 400 && maxAmplification >= 2.0f) {
            targetAmplifier = 2; // Hydrated III
        } else if (wetTicks >= 200 && maxAmplification >= 1.0f) {
            targetAmplifier = 1; // Hydrated II
        } else if (wetTicks >= 20) {
            targetAmplifier = 0; // Hydrated I
        } else {
            return;
        }

        if (targetAmplifier >= 0) {
            var currentEffect = player.getEffect(ModPotions.HYDRATED_EFFECT);
            boolean needsEffect = currentEffect == null;
            boolean needsUpgrade = currentEffect != null && currentEffect.getAmplifier() != targetAmplifier;

            if (needsEffect || needsUpgrade) {
                player.addEffect(new MobEffectInstance(ModPotions.HYDRATED_EFFECT, 80, targetAmplifier, false, false, true));
            }
        }
    }
}