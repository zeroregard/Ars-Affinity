package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class WaterAffinityLevel1Effect extends MobEffect {
    
    public WaterAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF82a2ed); // Water school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.water_affinity_level_1";
    }
}