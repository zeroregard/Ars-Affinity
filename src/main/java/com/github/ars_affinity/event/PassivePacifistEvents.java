package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassivePacifistEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Post event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_PACIFIST, perk -> {
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                double damageReduction = event.damage * amountPerk.amount;
                double newDamage = Math.max(0, event.damage - damageReduction);
                
                // Use reflection to set the damage since it's a final field
                try {
                    java.lang.reflect.Field damageField = event.getClass().getDeclaredField("damage");
                    damageField.setAccessible(true);
                    damageField.set(event, newDamage);
                } catch (Exception e) {
                    ArsAffinity.LOGGER.warn("Failed to reduce spell damage for pacifist perk: {}", e.getMessage());
                }
                
                ArsAffinity.LOGGER.debug("Player {} spell damage reduced by PASSIVE_PACIFIST: {} -> {} (reduction: {}%)", 
                    player.getName().getString(), 
                    event.damage + damageReduction,
                    newDamage,
                    (int)(amountPerk.amount * 100));
            }
        });
    }
}