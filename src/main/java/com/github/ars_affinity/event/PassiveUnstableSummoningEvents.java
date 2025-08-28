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
public class PassiveUnstableSummoningEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        boolean hasManipulationSchool = event.context.getSpell().unsafeList().stream()
            .anyMatch(part -> part.spellSchools.contains(SpellSchools.MANIPULATION));
        if (!hasManipulationSchool) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_UNSTABLE_SUMMONING, perk -> {
                if (perk instanceof AffinityPerk.UnstableSummoningPerk unstablePerk) {
                    ArsAffinity.LOGGER.info("Player {} cast manipulation spell - PASSIVE_UNSTABLE_SUMMONING active (chance: {}, entities: {})", 
                        player.getName().getString(), 
                        unstablePerk.chance, 
                        unstablePerk.entities.size());
                }
            });
        }
    }
}