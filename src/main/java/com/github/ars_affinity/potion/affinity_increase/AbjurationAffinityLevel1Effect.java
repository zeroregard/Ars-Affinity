package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AbjurationAffinityLevel1Effect extends MobEffect {
    
    public AbjurationAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFeb7cce); // Abjuration school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.abjuration_affinity_level_1";
    }
}