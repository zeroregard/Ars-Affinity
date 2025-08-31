package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AnimaAffinityLevel1Effect extends MobEffect {
    
    public AnimaAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF6d6d6d); // Anima school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.anima_affinity_level_1";
    }
}