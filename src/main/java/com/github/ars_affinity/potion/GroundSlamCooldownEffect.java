package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class GroundSlamCooldownEffect extends MobEffect {
    public GroundSlamCooldownEffect() {
        super(MobEffectCategory.HARMFUL, 0x7A5C3E); // earthy brown tint
    }

    @Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
    }
}