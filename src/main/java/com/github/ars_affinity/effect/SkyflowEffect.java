package com.github.ars_affinity.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class SkyflowEffect extends MobEffect {
    
    public SkyflowEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x00FFFF); // Cyan color for water theme
        // This effect is used as a marker for mana regeneration boost when wet
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // This effect doesn't need to tick every frame
        // The movement speed modifier is handled automatically by the effect system
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // This effect doesn't need to tick every frame
        return false;
    }
}
