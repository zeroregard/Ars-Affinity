package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.neoforged.bus.api.SubscribeEvent;

public class PassivePacifistEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Pre event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_PACIFIST, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
            float reduction = amountPerk.amount;
            float originalDamage = event.damage;
            float newDamage = originalDamage * (1.0f - reduction);
            
            event.damage = newDamage;
            
            ArsAffinity.LOGGER.info("Player {} spell damage reduced from {} to {} due to PASSIVE_PACIFIST ({}% reduction)", 
                player.getName().getString(), originalDamage, newDamage, (int)(reduction * 100));
        });
    }
}