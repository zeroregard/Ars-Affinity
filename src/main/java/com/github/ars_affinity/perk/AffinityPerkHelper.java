package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.List;
import java.util.function.Consumer;

public class AffinityPerkHelper {

    public static void applyPerks(SchoolAffinityProgress progress, int tier, SpellSchool school, Consumer<AffinityPerk> perkConsumer) {
        if (tier <= 0) return;

        List<AffinityPerk> perks = AffinityPerkManager.getPerksForLevel(school, tier);

        for (AffinityPerk perk : perks) {
            perkConsumer.accept(perk);
        }
    }
    
    public static void applyAllPerks(SchoolAffinityProgress progress, Consumer<AffinityPerk> perkConsumer) {
        for (SpellSchool school : com.github.ars_affinity.school.SchoolRelationshipHelper.ALL_SCHOOLS) {
            int tier = progress.getTier(school);
            if (tier > 0) {
                applyPerks(progress, tier, school, perkConsumer);
            }
        }
    }
} 