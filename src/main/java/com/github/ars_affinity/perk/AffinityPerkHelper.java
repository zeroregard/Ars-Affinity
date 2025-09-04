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
        PerkReference perkRef = progress.getActivePerkReference(perkType);
        if (perkRef != null) {
            // Get the actual perk data from AffinityPerkManager
            return AffinityPerkManager.getPerk(perkRef.getSourceSchool(), perkRef.getSourceTier(), perkRef.getPerkType());
        }
        return null;
    }

    public static AffinityPerk getActivePerk(Player player, AffinityPerkType perkType) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            return getActivePerk(progress, perkType);
        }
        return null;
    }
    
    public static PerkData getActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkReference perkRef = progress.getActivePerkReference(perkType);
        if (perkRef != null) {
            // Create a PerkData object from the reference and the actual perk
            AffinityPerk perk = AffinityPerkManager.getPerk(perkRef.getSourceSchool(), perkRef.getSourceTier(), perkRef.getPerkType());
            if (perk != null) {
                return new PerkData(perk, perkRef.getSourceSchool(), perkRef.getSourceTier());
            }
        }
        return null;
    }
    
    public static SpellSchool getPerkSourceSchool(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkReference perkRef = progress.getActivePerkReference(perkType);
        return perkRef != null ? perkRef.getSourceSchool() : null;
    }
    
    public static int getPerkSourceTier(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        PerkReference perkRef = progress.getActivePerkReference(perkType);
        return perkRef != null ? perkRef.getSourceTier() : 0;
    }
    
    public static void applyActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        AffinityPerk perk = getActivePerk(progress, perkType);
        if (perk != null) {
            perkConsumer.accept(perk);
        }
    }
    
    public static void applyActivePerk(Player player, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            AffinityPerk perk = getActivePerk(progress, perkType);
            if (perk != null) {
                perkConsumer.accept(perk);
            }
        }
    }
    
    public static <T extends AffinityPerk> void applyActivePerk(Player player, AffinityPerkType perkType, Class<T> perkClass, Consumer<T> perkConsumer) {
        SchoolAffinityProgress progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            AffinityPerk perk = getActivePerk(progress, perkType);
            if (perk != null && perkClass.isInstance(perk)) {
                perkConsumer.accept(perkClass.cast(perk));
            }
        }
    }
    
    public static void applyActivePerkData(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<PerkData> perkDataConsumer) {
        PerkData perkData = getActivePerkData(progress, perkType);
        if (perkData != null) {
            perkDataConsumer.accept(perkData);
        }
    }
    
    public static Set<PerkReference> getAllActivePerkReferences(SchoolAffinityProgress progress) {
        return progress.getAllActivePerkReferences();
    }
} 