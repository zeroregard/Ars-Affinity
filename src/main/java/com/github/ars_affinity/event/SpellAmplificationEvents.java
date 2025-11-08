package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.EffectResolveEvent;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellResolveEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHeal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// The idea with this one is to only get the tier in onSpellCast because then we don'tneed to look up 
// the player's tier  on every single effect resolve - so we look up once instead of up to 9 times.
public class SpellAmplificationEvents {

    private static final Map<UUID, Float> playerHealingAmplification = new HashMap<>();

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        // Check if player has the healing amplification perk
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_HEALING_AMPLIFICATION)) {
            // Check if the spell contains healing effects
            var healingEffects = event.spell.unsafeList().stream()
                .filter(part -> part instanceof EffectHeal).toList();
            
            if (healingEffects.size() > 0) {
                // Store the amplification amount for this player
                float amount = AffinityPerkHelper.getPerkAmount(player, AffinityPerkType.PASSIVE_HEALING_AMPLIFICATION);
                playerHealingAmplification.put(player.getUUID(), amount);
                ArsAffinity.LOGGER.info("Player {} cast healing spell with {}% amplification", 
                    player.getName().getString(), (int)(amount * 100));
            }
        }
    }

    @SubscribeEvent
    public static void onSpellResolve(SpellResolveEvent.Post event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        // Clear the amplification data after spell resolution
        playerHealingAmplification.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onEffectResolve(EffectResolveEvent.Pre event) {
        if (!(event.shooter instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) return;

        if (event.resolveEffect instanceof EffectHeal) {
            Float amplification = playerHealingAmplification.get(player.getUUID());
            if (amplification != null) {
                double currentAmp = event.spellStats.getAmpMultiplier();
                double newAmp = currentAmp + amplification;
                event.spellStats.setAmpMultiplier(newAmp);
                
                ArsAffinity.LOGGER.info("Applied {}% healing amplification to EffectHeal for player {}", 
                    (int)(amplification * 100), player.getName().getString());
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
            Float amplification = playerHealingAmplification.get(player.getUUID());
            if (amplification != null && amplification > 0) {
                Player targetPlayer = null;
                
                if (event.rayTraceResult instanceof EntityHitResult entityHit) {
                    if (entityHit.getEntity() instanceof Player hitPlayer) {
                        targetPlayer = hitPlayer;
                    }
                }
                
                if (targetPlayer != null && targetPlayer.getUUID().equals(player.getUUID())) {
                    float reductionPercent = Math.min(1.0f, amplification);
                    float baseExhaustion = 2.5f;
                    float exhaustionToCompensate = baseExhaustion * reductionPercent;
                    
                    var foodData = targetPlayer.getFoodData();
                    int currentFood = foodData.getFoodLevel();
                    float currentSaturation = foodData.getSaturationLevel();
                    
                    float saturationToAdd = exhaustionToCompensate;
                    float newSaturation = Math.min(currentFood, currentSaturation + saturationToAdd);
                    foodData.setSaturation(newSaturation);
                    
                    float remainingExhaustion = exhaustionToCompensate - (newSaturation - currentSaturation);
                    if (remainingExhaustion > 0.001f) {
                        int foodToAdd = (int) Math.ceil(remainingExhaustion / 4.0f);
                        int newFood = Math.min(20, currentFood + foodToAdd);
                        foodData.setFoodLevel(newFood);
                        
                        ArsAffinity.LOGGER.info("Healing Amplification compensated exhaustion for {}: +{} food, +{} saturation ({}% reduction)", 
                            targetPlayer.getName().getString(), foodToAdd, saturationToAdd, (int)(reductionPercent * 100));
                    } else {
                        ArsAffinity.LOGGER.info("Healing Amplification compensated exhaustion for {}: +{} saturation ({}% reduction)", 
                            targetPlayer.getName().getString(), saturationToAdd, (int)(reductionPercent * 100));
                    }
                }
            }
        }
    }
} 