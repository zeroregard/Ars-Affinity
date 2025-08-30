package com.github.ars_affinity.potion;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
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
            
            int countdownTicks = getCountdownFromPerks(amplifier);
            
            // Check if it's time to restore hunger
            if (player.tickCount % countdownTicks == 0) {
                if (foodData.getFoodLevel() < 20) {
                    int oldFood = foodData.getFoodLevel();
                    foodData.setFoodLevel(foodData.getFoodLevel() + 1);
                    ArsAffinity.LOGGER.info("HYDRATION - Hydrated {} effect tick for player: {} - restored hunger: {} -> {} (every {} ticks)", 
                        amplifier + 1, player.getName().getString(), oldFood, foodData.getFoodLevel(), countdownTicks);
                }
            }
        }
        return true;
    }
    
    private int getCountdownFromPerks(int amplifier) {
        int tier = amplifier + 1;
        
        AffinityPerk perk = AffinityPerkManager.getPerk(SpellSchools.ELEMENTAL_WATER, tier, AffinityPerkType.PASSIVE_HYDRATION);
        if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
            return durationPerk.time;
        }

        return 600;
     
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // All amplifiers need ticking for hunger restoration
        return true;
    }
    
    @Override
    public boolean isBeneficial() {
        return true;
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.hydrated";
    }
}