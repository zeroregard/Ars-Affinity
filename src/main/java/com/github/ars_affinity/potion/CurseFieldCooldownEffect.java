package com.github.ars_affinity.potion;

import java.util.Set;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;

public class CurseFieldCooldownEffect extends MobEffect {
	public CurseFieldCooldownEffect() {
		super(MobEffectCategory.HARMFUL, 0xFF3A2E39);
	}
	
	@Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
    }
}

