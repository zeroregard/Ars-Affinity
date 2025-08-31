package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AirAffinityLevel1Effect extends MobEffect {
    
    public AirAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFd4cf5a); // Air school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.air_affinity_level_1";
    }
}