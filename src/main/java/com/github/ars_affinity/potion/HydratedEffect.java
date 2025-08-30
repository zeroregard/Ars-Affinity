package com.github.ars_affinity.potion;

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
                return true;
            }
            
            // At amplifier 1: satiate hunger (1 hunger per 40 ticks)
            if (amplifier == 1) {
                if (foodData.getFoodLevel() < 20) {
                    foodData.setFoodLevel(foodData.getFoodLevel() + 1);
                }
            }
            
            // At amplifier 2: TODO - will be implemented later
            if (amplifier == 2) {
                // TODO: Implement additional hydration benefits
                // For now, also restore hunger like amplifier 1
                if (foodData.getFoodLevel() < 20) {
                    foodData.setFoodLevel(foodData.getFoodLevel() + 1);
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