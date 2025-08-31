package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class EarthAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public EarthAffinityEffect() {
        super(SpellSchools.ELEMENTAL_EARTH, "earth", 0xFF62e296); // Earth school color
    }
}