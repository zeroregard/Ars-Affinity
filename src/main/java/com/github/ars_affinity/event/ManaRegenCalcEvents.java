package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ManaRegenCalcEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if player is wet (in water, rain, etc.)
        boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
        
        if (player.isInWater() || player.isInWaterRainOrBubble()) {
            // Get player's affinity progress for Doused penalty
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int fireTier = progress.getTier(SpellSchools.ELEMENTAL_FIRE);
                if (fireTier > 0) {
                    AffinityPerkHelper.applyHighestTierPerk(progress, fireTier, SpellSchools.ELEMENTAL_FIRE, AffinityPerkType.PASSIVE_DOUSED, perk -> {
                        if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                            double currentRegen = event.getRegen();
                            double reduction = currentRegen * amountPerk.amount;
                            double newRegen = currentRegen - reduction;
                            
                            ArsAffinity.LOGGER.info("Player {} has PASSIVE_DOUSED perk ({}%) - reducing mana regen from {} to {}", 
                                player.getName().getString(), (int)(amountPerk.amount * 100), currentRegen, newRegen);
                            
                            event.setRegen(newRegen);
                        }
                    });
                }
                return;
            }    
            
            MobEffectInstance skyflowEffect = player.getEffect(ModPotions.SKYFLOW_EFFECT);
            if (skyflowEffect != null) {
                // SkyflowEffect increases mana regen when wet based on amplifier
                double currentRegen = event.getRegen();
                double boost = currentRegen * (0.2 * (skyflowEffect.getAmplifier() + 1)); // 20% per amplifier level
                double newRegen = currentRegen + boost;
                
                ArsAffinity.LOGGER.info("Player {} has SkyflowEffect (amplifier {}) - increasing mana regen from {} to {} when wet", 
                    player.getName().getString(), skyflowEffect.getAmplifier(), currentRegen, newRegen);
                
                event.setRegen(newRegen);
                return; // Skip the Doused penalty if SkyflowEffect is active
            }
        }

         
    }
} 