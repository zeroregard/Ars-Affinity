package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FireThornsEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        // Check if the spell is from the fire school
        boolean hasFireSchool = event.context.getSpell().unsafeList().stream()
            .anyMatch(part -> part.spellSchools.contains(SpellSchools.ELEMENTAL_FIRE));
        if (!hasFireSchool) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // O(1) perk lookup using the new perk index
            AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_FIRE_THORNS, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    // Apply fire thorns effect
                    // This would typically involve reflecting damage back to attackers
                    ArsAffinity.LOGGER.info("Player {} cast fire spell - PASSIVE_FIRE_THORNS active (amount: {})", 
                        player.getName().getString(), 
                        amountPerk.amount);
                }
            });
        }
    }
} 