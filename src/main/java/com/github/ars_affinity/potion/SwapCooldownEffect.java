package com.github.ars_affinity.potion;

import java.util.Set;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.common.EffectCure;

public class SwapCooldownEffect extends MobEffect {
    public SwapCooldownEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B008B); // Dark magenta color
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, net.minecraft.world.effect.MobEffectInstance effectInstance) {}
}