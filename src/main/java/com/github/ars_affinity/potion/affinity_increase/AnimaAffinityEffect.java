package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class AnimaAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public AnimaAffinityEffect() {
        super(SpellSchools.NECROMANCY, "anima", 0xFF6d6d6d); // Anima school color (same as necromancy)
    }
}