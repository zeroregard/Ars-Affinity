package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.List;
import java.util.function.Consumer;

public class AffinityPerkHelper {

    public static void applyHighestTierPerk(SchoolAffinityProgress progress, int tier, SpellSchool school, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        if (tier <= 0) {
            return;
        }

        List<AffinityPerk> levelPerks = AffinityPerkManager.getPerksForCurrentLevel(school, tier);
        if (levelPerks != null) {
            for (AffinityPerk perk : levelPerks) {
                if (perk.perk == perkType) {
                    perkConsumer.accept(perk);
                    return;
                }
            }
        }
    }
    
    public static void applyAllHighestTierPerks(SchoolAffinityProgress progress, AffinityPerkType perkType, Consumer<AffinityPerk> perkConsumer) {
        for (SpellSchool school : com.github.ars_affinity.school.SchoolRelationshipHelper.ALL_SCHOOLS) {
            int tier = progress.getTier(school);
            if (tier > 0) {
                applyHighestTierPerk(progress, tier, school, perkType, perkConsumer);
            }
        }
    }

} 