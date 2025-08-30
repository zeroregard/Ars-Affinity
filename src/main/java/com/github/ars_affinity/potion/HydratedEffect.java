package com.github.ars_affinity.potion;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

public class HydratedEffect extends MobEffect {
    
    public HydratedEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x87CEEB); // sky blue color
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            FoodData foodData = player.getFoodData();
            
            // At amplifier 0: hunger prevention is handled by HydratedHungerPreventionEvents
            if (amplifier == 0) {
                ArsAffinity.LOGGER.debug("HYDRATION - Hydrated I effect tick for player: {} (hunger prevention active)", 
                    player.getName().getString());
                return true;
            }
            
            // At amplifier 1: satiate hunger (1 hunger per 40 ticks)
            if (amplifier == 1) {
                if (foodData.getFoodLevel() < 20) {
                    int oldFood = foodData.getFoodLevel();
                    foodData.setFoodLevel(foodData.getFoodLevel() + 1);
                    ArsAffinity.LOGGER.debug("HYDRATION - Hydrated II effect tick for player: {} - restored hunger: {} -> {}", 
                        player.getName().getString(), oldFood, foodData.getFoodLevel());
                }
            }
            
            // At amplifier 2: TODO - will be implemented later
            if (amplifier == 2) {
                ArsAffinity.LOGGER.debug("HYDRATION - Hydrated III effect tick for player: {} (placeholder for future features)", 
                    player.getName().getString());
                // TODO: Implement additional hydration benefits
                // For now, also restore hunger like amplifier 1
                if (foodData.getFoodLevel() < 20) {
                    int oldFood = foodData.getFoodLevel();
                    foodData.setFoodLevel(foodData.getFoodLevel() + 1);
                    ArsAffinity.LOGGER.debug("HYDRATION - Hydrated III effect tick for player: {} - restored hunger: {} -> {}", 
                        player.getName().getString(), oldFood, foodData.getFoodLevel());
                }
            }
        }
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // For amplifier 1 and 2, we want to tick every 40 ticks to restore hunger
        if (amplifier >= 1) {
            return duration % 40 == 0;
        }
        // For amplifier 0, no ticking needed (hunger prevention is handled elsewhere)
        return false;
    }
    
    @Override
    public boolean isBeneficial() {
        return true;
    }
}