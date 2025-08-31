package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class ManipulationAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public ManipulationAffinityEffect() {
        super(SpellSchools.MANIPULATION, "manipulation", 0xFFFF8800); // Manipulation school color
    }
}