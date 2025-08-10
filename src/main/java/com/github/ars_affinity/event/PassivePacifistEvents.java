package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassivePacifistEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Pre event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // Check all schools for pacifist perks
            AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_PACIFIST, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    // Reduce damage by the specified percentage
                    float reduction = amountPerk.amount;
                    float originalDamage = event.damage;
                    float newDamage = originalDamage * (1.0f - reduction);
                    
                    event.damage = newDamage;
                    
                    ArsAffinity.LOGGER.info("Player {} spell damage reduced from {} to {} due to PASSIVE_PACIFIST ({}% reduction)", 
                        player.getName().getString(), originalDamage, newDamage, (int)(reduction * 100));
                }
            });
        }
    }
}