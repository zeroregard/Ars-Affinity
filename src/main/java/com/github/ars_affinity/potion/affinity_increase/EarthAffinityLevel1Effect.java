package com.github.ars_affinity.potion.affinity_increase;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class EarthAffinityLevel1Effect extends MobEffect {
    
    public EarthAffinityLevel1Effect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF62e296); // Earth school color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.earth_affinity_level_1";
    }
}