package com.github.ars_affinity.event;

import com.alexthw.sauce.registry.ModRegistry;
import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataProvider;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkAllocation;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

public class SauceLibAttributeEvents {

    private static final Map<AffinityPerkType, Holder<Attribute>> POWER_PERK_TO_ATTRIBUTE = new HashMap<>();
    private static final Map<AffinityPerkType, Holder<Attribute>> RESISTANCE_PERK_TO_ATTRIBUTE = new HashMap<>();
    
    private static final ResourceLocation WATER_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "water_power_perk");
    private static final ResourceLocation WATER_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "water_resistance_perk");
    private static final ResourceLocation FIRE_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "fire_power_perk");
    private static final ResourceLocation FIRE_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "fire_resistance_perk");
    private static final ResourceLocation AIR_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "air_power_perk");
    private static final ResourceLocation AIR_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "air_resistance_perk");
    private static final ResourceLocation EARTH_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "earth_power_perk");
    private static final ResourceLocation EARTH_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "earth_resistance_perk");
    private static final ResourceLocation SUMMON_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "summon_power_perk");
    private static final ResourceLocation CONJURATION_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "conjuration_resistance_perk");
    private static final ResourceLocation ABJURATION_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "abjuration_power_perk");
    private static final ResourceLocation ABJURATION_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "abjuration_resistance_perk");
    private static final ResourceLocation MANIPULATION_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "manipulation_power_perk");
    private static final ResourceLocation MANIPULATION_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "manipulation_resistance_perk");
    private static final ResourceLocation NECROMANCY_POWER_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "necromancy_power_perk");
    private static final ResourceLocation NECROMANCY_RESISTANCE_ID = ResourceLocation.fromNamespaceAndPath("ars_affinity", "necromancy_resistance_perk");

    static {
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_WATER_POWER, ModRegistry.WATER_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_WATER_RESISTANCE, ModRegistry.WATER_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_FIRE_POWER, ModRegistry.FIRE_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_FIRE_RESISTANCE, ModRegistry.FIRE_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_AIR_POWER, ModRegistry.AIR_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_AIR_RESISTANCE, ModRegistry.AIR_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_EARTH_POWER, ModRegistry.EARTH_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_EARTH_RESISTANCE, ModRegistry.EARTH_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_SUMMONING_POWER, ModRegistry.SUMMON_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_CONJURATION_RESISTANCE, ModRegistry.CONJURATION_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_ABJURATION_POWER, ModRegistry.ABJURATION_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_ABJURATION_RESISTANCE, ModRegistry.ABJURATION_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_MANIPULATION_POWER, ModRegistry.MANIPULATION_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_MANIPULATION_RESISTANCE, ModRegistry.MANIPULATION_RESISTANCE);
        
        POWER_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_ANIMA_POWER, ModRegistry.NECROMANCY_POWER);
        RESISTANCE_PERK_TO_ATTRIBUTE.put(AffinityPerkType.PASSIVE_ANIMA_RESISTANCE, ModRegistry.NECROMANCY_RESISTANCE);
    }

    private static ResourceLocation getIdForPerk(AffinityPerkType perkType) {
        return switch (perkType) {
            case PASSIVE_WATER_POWER -> WATER_POWER_ID;
            case PASSIVE_WATER_RESISTANCE -> WATER_RESISTANCE_ID;
            case PASSIVE_FIRE_POWER -> FIRE_POWER_ID;
            case PASSIVE_FIRE_RESISTANCE -> FIRE_RESISTANCE_ID;
            case PASSIVE_AIR_POWER -> AIR_POWER_ID;
            case PASSIVE_AIR_RESISTANCE -> AIR_RESISTANCE_ID;
            case PASSIVE_EARTH_POWER -> EARTH_POWER_ID;
            case PASSIVE_EARTH_RESISTANCE -> EARTH_RESISTANCE_ID;
            case PASSIVE_SUMMONING_POWER -> SUMMON_POWER_ID;
            case PASSIVE_CONJURATION_RESISTANCE -> CONJURATION_RESISTANCE_ID;
            case PASSIVE_ABJURATION_POWER -> ABJURATION_POWER_ID;
            case PASSIVE_ABJURATION_RESISTANCE -> ABJURATION_RESISTANCE_ID;
            case PASSIVE_MANIPULATION_POWER -> MANIPULATION_POWER_ID;
            case PASSIVE_MANIPULATION_RESISTANCE -> MANIPULATION_RESISTANCE_ID;
            case PASSIVE_ANIMA_POWER -> NECROMANCY_POWER_ID;
            case PASSIVE_ANIMA_RESISTANCE -> NECROMANCY_RESISTANCE_ID;
            default -> null;
        };
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        updatePlayerAttributes(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide() && player.tickCount % 20 == 0) {
            updatePlayerAttributes(player);
        }
    }

    private static void updatePlayerAttributes(Player player) {
        PlayerAffinityData affinityData = PlayerAffinityDataProvider.getPlayerAffinityData(player);
        if (affinityData == null) return;

        Map<AffinityPerkType, Integer> perkAmounts = new HashMap<>();
        
        for (PerkAllocation allocation : affinityData.getAllAllocatedPerks()) {
            AffinityPerkType type = allocation.getPerkType();
            int amount = (int)allocation.getNode().getAmount();
            int currentAmount = perkAmounts.getOrDefault(type, 0);
            perkAmounts.put(type, currentAmount + amount);
        }

        for (Map.Entry<AffinityPerkType, Holder<Attribute>> entry : POWER_PERK_TO_ATTRIBUTE.entrySet()) {
            applyAttributeModifier(player, entry.getKey(), entry.getValue(), perkAmounts.getOrDefault(entry.getKey(), 0));
        }
        
        for (Map.Entry<AffinityPerkType, Holder<Attribute>> entry : RESISTANCE_PERK_TO_ATTRIBUTE.entrySet()) {
            applyAttributeModifier(player, entry.getKey(), entry.getValue(), perkAmounts.getOrDefault(entry.getKey(), 0));
        }
    }

    private static void applyAttributeModifier(Player player, AffinityPerkType perkType, Holder<Attribute> attribute, int amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        ResourceLocation modifierId = getIdForPerk(perkType);
        if (modifierId == null) {
            return;
        }

        AttributeModifier existingModifier = instance.getModifier(modifierId);
        
        if (amount > 0) {
            if (existingModifier == null || existingModifier.amount() != amount) {
                if (existingModifier != null) {
                    instance.removeModifier(existingModifier);
                }
                
                AttributeModifier newModifier = new AttributeModifier(
                    modifierId,
                    (double)amount,
                    AttributeModifier.Operation.ADD_VALUE
                );
                instance.addTransientModifier(newModifier);
            }
        } else {
            if (existingModifier != null) {
                instance.removeModifier(existingModifier);
            }
        }
    }
}

