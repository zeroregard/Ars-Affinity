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
import net.minecraft.world.entity.player.Player;
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
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_HEALING_AMPLIFICATION, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
            // Check if the spell contains healing effects
            var healingEffects = event.spell.unsafeList().stream()
                .filter(part -> part instanceof EffectHeal).toList();
            
            if (healingEffects.size() > 0) {
                // Store the amplification amount for this player
                playerHealingAmplification.put(player.getUUID(), amountPerk.amount);
                ArsAffinity.LOGGER.info("Player {} cast healing spell with {}% amplification", 
                    player.getName().getString(), (int)(amountPerk.amount * 100));
            }
        });
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
            if (amplification != null) {
                // Add food/saturation to compensate for the exhaustion
                // EffectHeal applies 2.5f food exhaustion, we can compensate with food
                float compensation = 2.5f * amplification;
                var foodData = player.getFoodData();
                
                // Add food level (hunger)
                int currentFood = foodData.getFoodLevel();
                int foodGain = Math.round(compensation);
                int newFood = Math.min(20, currentFood + foodGain);
                foodData.setFoodLevel(newFood);
                
                // Add saturation
                float currentSaturation = foodData.getSaturationLevel();
                float saturationGain = compensation * 0.5f;
                float newSaturation = Math.min(newFood, currentSaturation + saturationGain);
                foodData.setSaturation(newSaturation);
                
                ArsAffinity.LOGGER.info("Compensated food exhaustion with {} food and {} saturation for player {} due to healing amplification", 
                    foodGain, saturationGain, player.getName().getString());
            }
        }
    }
} 