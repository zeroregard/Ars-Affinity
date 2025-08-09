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
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SpellBlightEvents {

    private static final Map<UUID, Float> playerBlightReduction = new HashMap<>();

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // Check all schools for blight perks
            AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_BLIGHTED, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    // Check if the spell contains healing effects
                    var healingEffects = event.spell.unsafeList().stream()
                        .filter(part -> part instanceof EffectHeal).toList();
                    
                    if (healingEffects.size() > 0) {
                        // Store the blight reduction amount for this player
                        playerBlightReduction.put(player.getUUID(), amountPerk.amount);
                        ArsAffinity.LOGGER.info("Player {} cast healing spell with {}% blight reduction", 
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

        // Clear the blight data after spell resolution
        playerBlightReduction.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onEffectResolve(EffectResolveEvent.Pre event) {
        if (!(event.shooter instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) return;

        if (event.resolveEffect instanceof EffectHeal) {
            Float blightReduction = playerBlightReduction.get(player.getUUID());
            if (blightReduction != null) {
                double currentAmp = event.spellStats.getAmpMultiplier();
                // Reduce healing effectiveness - negative amplification
                double newAmp = currentAmp - blightReduction;
                // Ensure we don't go below 0 (completely negating healing)
                newAmp = Math.max(0, newAmp);
                event.spellStats.setAmpMultiplier(newAmp);
                
                ArsAffinity.LOGGER.info("Applied {}% blight reduction to EffectHeal for player {}", 
                    (int)(blightReduction * 100), player.getName().getString());
            }
        }
    }

    @SubscribeEvent
    public static void onEffectResolvePost(EffectResolveEvent.Post event) {
        if (!(event.shooter instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) return;

        if (event.resolveEffect instanceof EffectHeal) {
            Float blightReduction = playerBlightReduction.get(player.getUUID());
            if (blightReduction != null) {
                // Add nausea effect based on blight level
                // Duration scales with blight amount (3-6 seconds)
                int nauseaDuration = Math.round(60 + (blightReduction * 60)); // 3-6 seconds (60-120 ticks)
                // Amplifier increases with higher blight levels
                int nauseaAmplifier = Math.min(2, Math.round(blightReduction * 3)); // 0-2 amplifier
                
                MobEffectInstance nauseaEffect = new MobEffectInstance(
                    MobEffects.CONFUSION, 
                    nauseaDuration, 
                    nauseaAmplifier, 
                    false, 
                    true
                );
                
                player.addEffect(nauseaEffect);
                
                ArsAffinity.LOGGER.info("Applied nausea (amplifier {}, duration {}s) to player {} due to blight", 
                    nauseaAmplifier, nauseaDuration / 20, player.getName().getString());
            }
        }
    }
}