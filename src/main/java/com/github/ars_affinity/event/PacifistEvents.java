package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.EffectResolveEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellResolveEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectAOE;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectShield;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectWall;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectLight;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectDispel;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PacifistEvents {

    private static final Map<UUID, Float> playerPacifistReduction = new HashMap<>();

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // Check for PACIFIST perk from abjuration school
            int abjurationTier = progress.getTier(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION);
            AffinityPerkHelper.applyHighestTierPerk(progress, abjurationTier, com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION, AffinityPerkType.PACIFIST, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    // Check if the spell contains abjuration effects
                    var abjurationEffects = event.spell.unsafeList().stream()
                        .filter(part -> part instanceof EffectHeal || 
                                       part instanceof EffectShield || 
                                       part instanceof EffectWall || 
                                       part instanceof EffectLight || 
                                       part instanceof EffectDispel || 
                                       part instanceof EffectAOE).toList();
                    
                    if (abjurationEffects.size() > 0) {
                        // Store the reduction amount for this player
                        playerPacifistReduction.put(player.getUUID(), amountPerk.amount);
                        ArsAffinity.LOGGER.info("Player {} cast abjuration spell with {}% PACIFIST power reduction", 
                            player.getName().getString(), (int)(amountPerk.amount * 100));
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void onSpellResolve(SpellResolveEvent.Post event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        // Clear the reduction data after spell resolution
        playerPacifistReduction.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onEffectResolve(EffectResolveEvent.Pre event) {
        if (!(event.shooter instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) return;

        // Check if this is an abjuration effect and apply PACIFIST reduction
        if (event.resolveEffect instanceof EffectHeal || 
            event.resolveEffect instanceof EffectShield || 
            event.resolveEffect instanceof EffectWall || 
            event.resolveEffect instanceof EffectLight || 
            event.resolveEffect instanceof EffectDispel || 
            event.resolveEffect instanceof EffectAOE) {
            
            Float reduction = playerPacifistReduction.get(player.getUUID());
            if (reduction != null) {
                double currentAmp = event.spellStats.getAmpMultiplier();
                double newAmp = currentAmp * (1.0 - reduction); // Reduce by the percentage
                event.spellStats.setAmpMultiplier(newAmp);
                
                ArsAffinity.LOGGER.info("Applied {}% PACIFIST power reduction to {} for player {}", 
                    (int)(reduction * 100), event.resolveEffect.getClass().getSimpleName(), player.getName().getString());
            }
        }
    }
}