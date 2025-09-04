package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveGroundedEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        boolean isNotOnGround = !player.onGround();
        
        if (isNotOnGround) {
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_GROUNDED, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
                double currentRegen = event.getRegen();
                double reduction = currentRegen * amountPerk.amount;
                double newRegen = currentRegen - reduction;
                
                ArsAffinity.LOGGER.info("Player {} is not on ground - PASSIVE_GROUNDED perk ({}%) reducing mana regen from {} to {}", 
                    player.getName().getString(), (int)(amountPerk.amount * 100), currentRegen, newRegen);
                
                event.setRegen(newRegen);
            });
        }
    }
} 