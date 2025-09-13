package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.Set;

public class AffinityPerkHelper {
    
    private static boolean hasActivePerk(PlayerAffinityData data, AffinityPerkType perkType) {
        // Check if any allocated perk matches this type
        return data.getAllAllocatedPerks().stream()
            .anyMatch(allocation -> allocation.getPerkType() == perkType);
    }

    public static boolean hasActivePerk(Player player, AffinityPerkType perkType) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data != null) {
            return hasActivePerk(data, perkType);
        }
        return false;
    }
    
    public static AffinityPerk getActivePerk(PlayerAffinityData data, AffinityPerkType perkType) {
        // Find the first allocated perk of this type
        return data.getAllAllocatedPerks().stream()
            .filter(allocation -> allocation.getPerkType() == perkType)
            .findFirst()
            .map(allocation -> {
                // Create AffinityPerk from the PerkNode data
                PerkNode node = allocation.getNode();
                return createAffinityPerkFromNode(node);
            })
            .orElse(null);
    }

    public static AffinityPerk getActivePerk(Player player, AffinityPerkType perkType) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data != null) {
            return getActivePerk(data, perkType);
        }
        return null;
    }
    
    public static PerkData getActivePerkData(PlayerAffinityData data, AffinityPerkType perkType) {
        // Find the first allocated perk of this type
        return data.getAllAllocatedPerks().stream()
            .filter(allocation -> allocation.getPerkType() == perkType)
            .findFirst()
            .map(allocation -> {
                PerkNode node = allocation.getNode();
                AffinityPerk perk = createAffinityPerkFromNode(node);
                return new PerkData(perk, allocation.getSchool(), allocation.getTier());
            })
            .orElse(null);
    }
    
    public static SpellSchool getPerkSourceSchool(PlayerAffinityData data, AffinityPerkType perkType) {
        return data.getAllAllocatedPerks().stream()
            .filter(allocation -> allocation.getPerkType() == perkType)
            .findFirst()
            .map(allocation -> allocation.getSchool())
            .orElse(null);
    }
    
    public static int getPerkSourceTier(PlayerAffinityData data, AffinityPerkType perkType) {
        return data.getAllAllocatedPerks().stream()
            .filter(allocation -> allocation.getPerkType() == perkType)
            .findFirst()
            .map(allocation -> allocation.getTier())
            .orElse(0);
    }
    
    public static void applyActivePerk(PlayerAffinityData data, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        AffinityPerk perk = getActivePerk(data, perkType);
        if (perk != null) {
            perkConsumer.accept(perk);
        }
    }
    
    public static void applyActivePerk(Player player, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data != null) {
            AffinityPerk perk = getActivePerk(data, perkType);
            if (perk != null) {
                perkConsumer.accept(perk);
            }
        }
    }
    
    public static <T extends AffinityPerk> void applyActivePerk(Player player, AffinityPerkType perkType, Class<T> perkClass, Consumer<T> perkConsumer) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data != null) {
            AffinityPerk perk = getActivePerk(data, perkType);
            if (perk != null && perkClass.isInstance(perk)) {
                perkConsumer.accept(perkClass.cast(perk));
            }
        }
    }
    
    public static void applyActivePerkData(PlayerAffinityData data, AffinityPerkType perkType, Consumer<PerkData> perkDataConsumer) {
        PerkData perkData = getActivePerkData(data, perkType);
        if (perkData != null) {
            perkDataConsumer.accept(perkData);
        }
    }
    
    public static Set<PerkAllocation> getAllActivePerkAllocations(PlayerAffinityData data) {
        return data.getAllAllocatedPerks();
    }
    
    /**
     * Creates an AffinityPerk from a PerkNode using the node's configured values
     */
    public static AffinityPerk createAffinityPerkFromNode(PerkNode node) {
        AffinityPerkType perkType = node.getPerkType();
        boolean isBuff = true; // All perks in the new system are buffs
        
        switch (perkType) {
            case PASSIVE_FIRE_THORNS:
            case PASSIVE_MANA_TAP:
            case PASSIVE_HEALING_AMPLIFICATION:
            case PASSIVE_SOULSPIKE:
            case PASSIVE_SUMMONING_POWER:
            case PASSIVE_ABJURATION_POWER:
            case PASSIVE_AIR_POWER:
            case PASSIVE_EARTH_POWER:
            case PASSIVE_FIRE_POWER:
            case PASSIVE_MANIPULATION_POWER:
            case PASSIVE_ANIMA_POWER:
            case PASSIVE_WATER_POWER:
            case PASSIVE_ABJURATION_RESISTANCE:
            case PASSIVE_CONJURATION_RESISTANCE:
            case PASSIVE_AIR_RESISTANCE:
            case PASSIVE_EARTH_RESISTANCE:
            case PASSIVE_FIRE_RESISTANCE:
            case PASSIVE_MANIPULATION_RESISTANCE:
            case PASSIVE_ANIMA_RESISTANCE:
            case PASSIVE_WATER_RESISTANCE:
            case PASSIVE_COLD_WALKER:
                return new AffinityPerk.AmountBasedPerk(perkType, node.getAmount(), isBuff);
                
            case PASSIVE_SUMMON_HEALTH:
            case PASSIVE_SUMMON_DEFENSE:
            case PASSIVE_STONE_SKIN:
            case PASSIVE_DEFLECTION:
            case PASSIVE_HYDRATION:
                return new AffinityPerk.DurationBasedPerk(perkType, node.getAmount(), node.getTime(), isBuff);
                
            case PASSIVE_LICH_FEAST:
                // For Lich Feast, we need to calculate health and hunger from amount
                float healthRestore = node.getAmount() * 0.1f; // 10% per amount
                float hungerRestore = node.getAmount() * 0.05f; // 5% per amount
                return new AffinityPerk.LichFeastPerk(perkType, healthRestore, hungerRestore, isBuff);
                
            case ACTIVE_ICE_BLAST:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    node.getDamage(), node.getFreezeTime(), node.getRadius(), isBuff);
                    
            case ACTIVE_GROUND_SLAM:
            case ACTIVE_SWAP_ABILITY:
            case ACTIVE_SANCTUARY:
            case ACTIVE_CURSE_FIELD:
            case ACTIVE_SWARM:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    0.0f, 0, 0.0f, isBuff);
                    
            case ACTIVE_AIR_DASH:
            case ACTIVE_FIRE_DASH:
            case ACTIVE_GHOST_STEP:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    0.0f, 0, 0.0f, node.getDashLength(), node.getDashDuration(), isBuff);
                    
            case PASSIVE_GHOST_STEP:
                return new AffinityPerk.GhostStepPerk(perkType, node.getAmount(), node.getTime(), 
                    node.getCooldown(), isBuff);
                    
            case PASSIVE_ROTTING_GUISE:
                return new AffinityPerk.SimplePerk(perkType, isBuff);
                
            default:
                com.github.ars_affinity.ArsAffinity.LOGGER.warn("Unknown perk type: {}, using default AmountBasedPerk", perkType);
                return new AffinityPerk.AmountBasedPerk(perkType, node.getAmount(), isBuff);
        }
    }
} 