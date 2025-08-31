package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class FireAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public FireAffinityEffect() {
        super(SpellSchools.ELEMENTAL_FIRE, "fire", 0xFFf06666); // Fire school color
    }
}