package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class PassiveLichFeastEvents {
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        LivingEntity killedEntity = event.getEntity();
        if (killedEntity.getType().is(EntityTypeTags.UNDEAD)) return;
        
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_LICH_FEAST, AffinityPerk.LichFeastPerk.class, lichPerk -> {
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float newHealth = Math.min(currentHealth + lichPerk.health, maxHealth);
            
            if (newHealth > currentHealth) {
                player.setHealth(newHealth);
            }
            
            if (lichPerk.hunger > 0) {
                var foodData = player.getFoodData();
                float oldFoodLevel = foodData.getFoodLevel();
                float oldSaturation = foodData.getSaturationLevel();
                
                int newFoodLevel = Math.min(20, Math.round(oldFoodLevel + lichPerk.hunger));
                foodData.setFoodLevel(newFoodLevel);
                
                float saturationGain = lichPerk.hunger * 0.5f;
                float newSaturation = Math.min(newFoodLevel, oldSaturation + saturationGain);
                foodData.setSaturation(newSaturation);
            }
            
            ArsAffinity.LOGGER.info("Player {} killed non-undead entity {} - PASSIVE_LICH_FEAST restored {} health and {} hunger", 
                player.getName().getString(), killedEntity.getName().getString(), lichPerk.health, lichPerk.hunger);
        });
    }
} 