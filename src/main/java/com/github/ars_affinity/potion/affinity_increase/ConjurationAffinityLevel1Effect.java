package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ConjurationAffinityLevel1Effect extends MobEffect {
    
    public ConjurationAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6ae3ce); // Conjuration school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.conjuration_affinity_level_1";
    }
}