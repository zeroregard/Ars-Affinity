package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;
import java.util.Map;
import java.util.Set;

public class AffinityPerkHelper {
    
    private static boolean hasActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.hasActivePerk(perkType);
    }

    public static boolean hasActivePerk(Player player, AffinityPerkType perkType) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            return hasActivePerk(progress, perkType);
        }
        return false;
    }
    
    public static AffinityPerk getActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.perk : null;
    }
    
    public static PerkData getActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.getActivePerk(perkType);
    }
    
    public static SpellSchool getPerkSourceSchool(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.sourceSchool : null;
    }
    
    public static int getPerkSourceTier(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkData perkData = progress.getActivePerk(perkType);
        return perkData != null ? perkData.sourceTier : 0;
    }
    
    public static void applyActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        AffinityPerk perk = getActivePerk(progress, perkType);
        if (perk != null) {
            perkConsumer.accept(perk);
        }
    }
    
    private static <T extends AffinityPerk> void applyActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType, Class<T> perkClass, Consumer<T> perkConsumer) {
        AffinityPerk perk = getActivePerk(progress, perkType);
        if (perk != null && perkClass.isInstance(perk)) {
            perkConsumer.accept(perkClass.cast(perk));
        }
    }
    
    public static void applyActivePerk(Player player, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            applyActivePerk(progress, perkType, perkConsumer);
        }
    }
    
    public static <T extends AffinityPerk> void applyActivePerk(Player player, AffinityPerkType perkType, Class<T> perkClass, Consumer<T> perkConsumer) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            applyActivePerk(progress, perkType, perkClass, perkConsumer);
        }
    }
    
    public static void applyActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<PerkData> perkDataConsumer) {
        PerkData perkData = getActivePerkData(progress, perkType);
        if (perkData != null) {
            perkDataConsumer.accept(perkData);
        }
    }
    
    public static Map<AffinityPerkType, PerkData> getAllActivePerks(SchoolAffinityProgress progress) {
        return progress.getAllActivePerks();
    }
    
    public static Set<PerkReference> getAllActivePerkReferences(SchoolAffinityProgress progress) {
        return progress.getAllActivePerkReferences();
    }
} 