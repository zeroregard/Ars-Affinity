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
                // Allow negative values for -100% healing effectiveness and even damage
                event.spellStats.setAmpMultiplier(newAmp);
                
                ArsAffinity.LOGGER.info("Applied {}% blight reduction to EffectHeal for player {} (new amp: {})", 
                    (int)(blightReduction * 100), player.getName().getString(), newAmp);
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
                // Apply food poisoning effects similar to rotten flesh
                // Duration scales with blight amount (5-10 seconds)
                int hungerDuration = Math.round(100 + (blightReduction * 100)); // 5-10 seconds (100-200 ticks)
                // Amplifier increases with higher blight levels
                int hungerAmplifier = Math.min(1, Math.round(blightReduction * 2)); // 0-1 amplifier
                
                // Apply hunger effect (food poisoning)
                MobEffectInstance hungerEffect = new MobEffectInstance(
                    MobEffects.HUNGER, 
                    hungerDuration, 
                    hungerAmplifier, 
                    false, 
                    true
                );
                
                player.addEffect(hungerEffect);
                
                // Apply nausea effect for visual disorientation
                int nauseaDuration = Math.round(60 + (blightReduction * 60)); // 3-6 seconds (60-120 ticks)
                int nauseaAmplifier = Math.min(2, Math.round(blightReduction * 3)); // 0-2 amplifier
                
                MobEffectInstance nauseaEffect = new MobEffectInstance(
                    MobEffects.CONFUSION, 
                    nauseaDuration, 
                    nauseaAmplifier, 
                    false, 
                    true
                );
                
                player.addEffect(nauseaEffect);
                
                // Apply additional food exhaustion to simulate the sickness
                var foodData = player.getFoodData();
                float exhaustion = 2.0f + (blightReduction * 3.0f); // 2.0-5.0 exhaustion
                foodData.addExhaustion(exhaustion);
                
                ArsAffinity.LOGGER.info("Applied food poisoning (hunger amplifier {}, duration {}s, nausea amplifier {}, duration {}s, exhaustion {}) to player {} due to blight", 
                    hungerAmplifier, hungerDuration / 20, nauseaAmplifier, nauseaDuration / 20, exhaustion, player.getName().getString());
            }
        }
    }
}