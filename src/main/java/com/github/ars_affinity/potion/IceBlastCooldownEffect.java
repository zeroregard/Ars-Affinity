package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class IceBlastCooldownEffect extends MobEffect {
    public IceBlastCooldownEffect() {
        super(MobEffectCategory.HARMFUL, 0x87CEEB);
    }

    @Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
    }
}