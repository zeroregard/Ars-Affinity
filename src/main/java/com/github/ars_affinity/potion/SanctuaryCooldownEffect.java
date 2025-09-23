package com.github.ars_affinity.potion;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

public class SanctuaryCooldownEffect extends MobEffect {
	public SanctuaryCooldownEffect() {
		super(MobEffectCategory.HARMFUL, 0xFFAAA979);
	}

	@Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
    }
}

