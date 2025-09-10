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
                // Create a basic AffinityPerk from the allocation
                // This is a simplified approach - in a full implementation, you'd want to
                // store the actual perk data in the allocation
                return new AffinityPerk.AmountBasedPerk(perkType, 1.0f, true);
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
                AffinityPerk perk = new AffinityPerk.AmountBasedPerk(perkType, 1.0f, true);
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
} 