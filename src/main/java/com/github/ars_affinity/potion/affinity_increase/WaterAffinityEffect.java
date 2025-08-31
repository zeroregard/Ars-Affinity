package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class WaterAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public WaterAffinityEffect() {
        super(SpellSchools.ELEMENTAL_WATER, "water", 0xFF82a2ed); // Water school color
    }
}