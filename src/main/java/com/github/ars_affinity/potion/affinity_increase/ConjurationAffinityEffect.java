package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class ConjurationAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public ConjurationAffinityEffect() {
        super(SpellSchools.CONJURATION, "conjuration", 0xFFF06292); // Conjuration school color
    }
}
