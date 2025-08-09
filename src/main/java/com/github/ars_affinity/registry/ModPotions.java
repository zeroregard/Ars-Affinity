package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.potion.BubbleGuardCooldownEffect;
import com.github.ars_affinity.potion.IceBlastCooldownEffect;
import com.github.ars_affinity.potion.KnockbackImmunityEffect;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ArsAffinity.MOD_ID);
    
    public static final DeferredHolder<MobEffect, BubbleGuardCooldownEffect> BUBBLE_GUARD_COOLDOWN_EFFECT = EFFECTS.register("bubble_guard_cooldown", BubbleGuardCooldownEffect::new);
    public static final DeferredHolder<MobEffect, IceBlastCooldownEffect> ICE_BLAST_COOLDOWN_EFFECT = EFFECTS.register("ice_blast_cooldown", IceBlastCooldownEffect::new);
    public static final DeferredHolder<MobEffect, KnockbackImmunityEffect> KNOCKBACK_IMMUNITY_EFFECT = EFFECTS.register("knockback_immunity", KnockbackImmunityEffect::new);
} 