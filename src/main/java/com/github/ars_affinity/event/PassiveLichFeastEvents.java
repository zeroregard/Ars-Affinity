package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveLichFeastEvents {
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        LivingEntity killedEntity = event.getEntity();
        if (killedEntity.getType().is(EntityTypeTags.UNDEAD)) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            int necromancyTier = progress.getTier(SpellSchools.NECROMANCY);
            if (necromancyTier >= 3) {
                AffinityPerkHelper.applyHighestTierPerk(progress, necromancyTier, SpellSchools.NECROMANCY, AffinityPerkType.PASSIVE_LICH_FEAST, perk -> {
                    if (perk instanceof AffinityPerk.LichFeastPerk lichPerk) {
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
                    }
                });
            }
        }
    }
} 