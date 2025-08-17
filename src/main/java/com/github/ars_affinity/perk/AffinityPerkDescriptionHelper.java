package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AffinityPerkDescriptionHelper {

    public static MutableComponent getPerkDescription(AffinityPerk perk) {
        String translationKey = "ars_affinity.perk." + perk.perk.name();

        switch (perk.perk) {
            case PASSIVE_DOUSED:
            case PASSIVE_DEHYDRATED:
            case PASSIVE_BURIED:
            case PASSIVE_GROUNDED:
            case PASSIVE_MANA_TAP:
            case PASSIVE_FIRE_THORNS:
            case PASSIVE_HEALING_AMPLIFICATION:
            case PASSIVE_BLIGHTED:
            case PASSIVE_FREE_JUMP:
            case PASSIVE_REVERBERATION:
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    return Component.translatable(translationKey, (int)(amountPerk.amount * 100));
                }
                return Component.translatable(translationKey, 0);
            case PASSIVE_SUMMON_HEALTH:
                if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
                    return Component.translatable(translationKey, (int)(durationPerk.amount * 4), durationPerk.time / 20);
                }
                return Component.translatable(translationKey, 0, 0);
            case PASSIVE_DEFLECTION:
                if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
                    return Component.translatable(translationKey, durationPerk.time / 20);
                }
                return Component.translatable(translationKey, 0);
            case PASSIVE_LICH_FEAST:
                if (perk instanceof AffinityPerk.LichFeastPerk lichPerk) {
                    return Component.translatable(translationKey, lichPerk.health, lichPerk.hunger);
                }
                return Component.translatable(translationKey, 0, 0);
            case PASSIVE_GHOST_STEP:
                if (perk instanceof AffinityPerk.GhostStepPerk ghostStepPerk) {
                    return Component.translatable(translationKey, (int)(ghostStepPerk.amount * 100), ghostStepPerk.time, ghostStepPerk.cooldown);
                }
                return Component.translatable(translationKey, 0, 0, 0);
            case PASSIVE_MOB_PACIFICATION:
                if (perk instanceof AffinityPerk.EntityBasedPerk entityPerk) {
                    if (entityPerk.entities != null && !entityPerk.entities.isEmpty()) {
                        String entityNames = getEntityNames(entityPerk.entities);
                        return Component.translatable(translationKey, entityNames);
                    } else {
                        return Component.translatable(translationKey, "unknown entities");
                    }
                }
                return Component.translatable(translationKey, "unknown entities");
            case ACTIVE_ICE_BLAST:
                if (perk instanceof AffinityPerk.ActiveAbilityPerk activePerk) {
                    return Component.translatable(translationKey, (int)(activePerk.manaCost * 100), activePerk.cooldown / 20);
                }
                return Component.translatable(translationKey, 0, 0);
            case ACTIVE_SWAP_ABILITY:
                if (perk instanceof AffinityPerk.ActiveAbilityPerk activePerk) {
                    return Component.translatable(translationKey, (int)activePerk.manaCost, activePerk.cooldown / 20);
                }
                return Component.translatable(translationKey, 0, 0);
            case PASSIVE_UNSTABLE_SUMMONING:
                if (perk instanceof AffinityPerk.UnstableSummoningPerk unstablePerk) {
                    if (unstablePerk.entities != null && !unstablePerk.entities.isEmpty()) {
                        String entityNames = getEntityNames(unstablePerk.entities);
                        return Component.translatable(translationKey, (int)(unstablePerk.chance * 100), entityNames);
                    } else {
                        return Component.translatable(translationKey, (int)(unstablePerk.chance * 100), "unknown entities");
                    }
                }
                return Component.translatable(translationKey, 0, "unknown entities");
            default:
                return Component.translatable(translationKey, 0);
        }
    }

    public static String getPerkPrefix(AffinityPerk perk) {
        return perk.isBuff ? "+ " : "- ";
    }
    
    private static String getEntityNames(List<String> entityIds) {
        return entityIds.stream()
            .map(id -> EntityType.byString(id))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(EntityType::getDescriptionId)
            .map(key -> Component.translatable(key).getString())
            .collect(Collectors.joining(", "));
    }
} 