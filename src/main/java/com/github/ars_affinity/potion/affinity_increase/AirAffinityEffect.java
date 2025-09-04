package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class AirAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public AirAffinityEffect() {
        super(SpellSchools.ELEMENTAL_AIR, "air", 0xFF81C784); // Air school color
    }
}
