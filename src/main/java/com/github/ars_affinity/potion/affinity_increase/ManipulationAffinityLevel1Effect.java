package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ManipulationAffinityLevel1Effect extends MobEffect {
    
    public ManipulationAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF8800); // Manipulation school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.manipulation_affinity_level_1";
    }
}