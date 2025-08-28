package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.function.Consumer;
import java.util.Map;
import java.util.Set;

public class AffinityPerkHelper {
    
    public static boolean hasActivePerk(SchoolAffinityProgress progress, AffinityPerkType perkType) {
        return progress.hasActivePerk(perkType);
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