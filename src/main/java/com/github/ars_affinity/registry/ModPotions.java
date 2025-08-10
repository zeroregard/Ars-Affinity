package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.potion.ProjectileReversalCooldownEffect;
import com.github.ars_affinity.potion.KnockbackImmunityEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ArsAffinity.MOD_ID);
    
    public static final DeferredHolder<MobEffect, ProjectileReversalCooldownEffect> PROJECTILE_REVERSAL_COOLDOWN_EFFECT = EFFECTS.register("projectile_reversal_cooldown", ProjectileReversalCooldownEffect::new);
    public static final DeferredHolder<MobEffect, KnockbackImmunityEffect> KNOCKBACK_IMMUNITY_EFFECT = EFFECTS.register("knockback_immunity", KnockbackImmunityEffect::new);
} 