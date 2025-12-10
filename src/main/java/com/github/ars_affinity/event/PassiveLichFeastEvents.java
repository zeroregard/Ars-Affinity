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
        
        // Check if player has the lich feast perk
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_LICH_FEAST)) {
            float health = AffinityPerkHelper.getPerkHealth(player, AffinityPerkType.PASSIVE_LICH_FEAST);
            float hunger = AffinityPerkHelper.getPerkHunger(player, AffinityPerkType.PASSIVE_LICH_FEAST);
            
            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            float newHealth = Math.min(currentHealth + health, maxHealth);
            
            if (newHealth > currentHealth) {
                player.setHealth(newHealth);
            }
            
            if (hunger > 0) {
                var foodData = player.getFoodData();
                float oldFoodLevel = foodData.getFoodLevel();
                float oldSaturation = foodData.getSaturationLevel();
                
                int newFoodLevel = Math.min(20, Math.round(oldFoodLevel + hunger));
                foodData.setFoodLevel(newFoodLevel);
                
                float saturationGain = hunger * 0.5f;
                float newSaturation = Math.min(newFoodLevel, oldSaturation + saturationGain);
                foodData.setSaturation(newSaturation);
            }
            
            ArsAffinity.LOGGER.debug("Player {} killed non-undead entity {} - PASSIVE_LICH_FEAST restored {} health and {} hunger", 
                player.getName().getString(), killedEntity.getName().getString(), health, hunger);
        }
    }
} 