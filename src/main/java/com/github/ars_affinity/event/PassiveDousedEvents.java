package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveDousedEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if player is wet (in water, rain, etc.)
        boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
        
        if (isWet) {
            // Get player's affinity progress
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int fireTier = progress.getTier(SpellSchools.ELEMENTAL_FIRE);
                if (fireTier > 0) {
                    AffinityPerkHelper.applyPerks(progress, fireTier, SpellSchools.ELEMENTAL_FIRE, perk -> {
                        if (perk.perk == AffinityPerkType.PASSIVE_DOUSED && perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                            double currentRegen = event.getRegen();
                            double reduction = currentRegen * amountPerk.amount;
                            double newRegen = currentRegen - reduction;
                            
                            ArsAffinity.LOGGER.info("Player {} has PASSIVE_DOUSED perk ({}%) - reducing mana regen from {} to {}", 
                                player.getName().getString(), (int)(amountPerk.amount * 100), currentRegen, newRegen);
                            
                            event.setRegen(newRegen);
                        }
                    });
                }
            }
        }
    }
} 