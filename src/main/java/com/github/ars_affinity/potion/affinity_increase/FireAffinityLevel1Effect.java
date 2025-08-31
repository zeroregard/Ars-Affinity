package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class FireAffinityLevel1Effect extends MobEffect {
    
    public FireAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFf06666); // Fire school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.fire_affinity_level_1";
    }
}