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

public class ManaRegenCalcEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if player is wet (in water, rain, etc.)
        boolean isWet = player.isInWater() || player.isInWaterRainOrBubble();
        
        if (isWet) {
            // Get player's affinity progress for Doused penalty
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int fireTier = progress.getTier(SpellSchools.ELEMENTAL_FIRE);
                if (fireTier > 0) {
                    AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_DOUSED, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
                        double currentRegen = event.getRegen();
                        double reduction = currentRegen * amountPerk.amount;
                        double newRegen = currentRegen - reduction;
                        
                        ArsAffinity.LOGGER.info("Player {} has PASSIVE_DOUSED perk ({}%) - reducing mana regen from {} to {}", 
                            player.getName().getString(), (int)(amountPerk.amount * 100), currentRegen, newRegen);
                        
                        event.setRegen(newRegen);
                    });
                }
            }    
        }
    }
} 