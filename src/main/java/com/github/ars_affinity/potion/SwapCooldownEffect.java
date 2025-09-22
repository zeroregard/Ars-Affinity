package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class SwapCooldownEffect extends MobEffect {
    public SwapCooldownEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B008B); // Dark magenta color
    }
}