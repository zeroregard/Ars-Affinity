package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class KnockbackImmunityEffect extends MobEffect {
    public KnockbackImmunityEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B4513); // Brown color for earth effect
    }

    @Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
        // This effect cannot be cured by normal means (milk, etc.)
        // It's only removed when the timer expires
    }
}