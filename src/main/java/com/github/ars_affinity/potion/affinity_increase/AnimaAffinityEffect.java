package com.github.ars_affinity.potion.affinity_increase;

import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

public class AnimaAffinityEffect extends AbstractAffinityIncreaseEffect {
    
    public AnimaAffinityEffect() {
        super(SpellSchools.NECROMANCY, "anima", 0xFF9575CD); // Anima school color
    }
}
