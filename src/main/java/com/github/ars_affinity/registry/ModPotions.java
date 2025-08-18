package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.potion.IceBlastCooldownEffect;
import com.github.ars_affinity.potion.DeflectionCooldownEffect;
import com.github.ars_affinity.potion.GhostStepCooldownEffect;
import com.github.ars_affinity.potion.StoneSkinCooldownEffect;
import com.github.ars_affinity.potion.AirDashCooldownEffect;
import com.github.ars_affinity.potion.SanctuaryEffect;
import com.github.ars_affinity.potion.SanctuaryCooldownEffect;
import com.github.ars_affinity.potion.CurseFieldCooldownEffect;
import com.github.ars_affinity.potion.SilencedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ArsAffinity.MOD_ID);
    
    public static final DeferredHolder<MobEffect, IceBlastCooldownEffect> ICE_BLAST_COOLDOWN_EFFECT = EFFECTS.register("ice_blast_cooldown", IceBlastCooldownEffect::new);
    public static final DeferredHolder<MobEffect, DeflectionCooldownEffect> DEFLECTION_COOLDOWN_EFFECT = EFFECTS.register("deflection_cooldown", DeflectionCooldownEffect::new);
    public static final DeferredHolder<MobEffect, GhostStepCooldownEffect> GHOST_STEP_COOLDOWN_EFFECT = EFFECTS.register("ghost_step_cooldown", GhostStepCooldownEffect::new);
    public static final DeferredHolder<MobEffect, com.github.ars_affinity.potion.GroundSlamCooldownEffect> GROUND_SLAM_COOLDOWN_EFFECT = EFFECTS.register("ground_slam_cooldown", com.github.ars_affinity.potion.GroundSlamCooldownEffect::new);
    public static final DeferredHolder<MobEffect, StoneSkinCooldownEffect> STONE_SKIN_COOLDOWN_EFFECT = EFFECTS.register("stone_skin_cooldown", StoneSkinCooldownEffect::new);
    public static final DeferredHolder<MobEffect, AirDashCooldownEffect> AIR_DASH_COOLDOWN_EFFECT = EFFECTS.register("air_dash_cooldown", AirDashCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SanctuaryEffect> SANCTUARY_EFFECT = EFFECTS.register("sanctuary", SanctuaryEffect::new);
    public static final DeferredHolder<MobEffect, SanctuaryCooldownEffect> SANCTUARY_COOLDOWN_EFFECT = EFFECTS.register("sanctuary_cooldown", SanctuaryCooldownEffect::new);
    public static final DeferredHolder<MobEffect, CurseFieldCooldownEffect> CURSE_FIELD_COOLDOWN_EFFECT = EFFECTS.register("curse_field_cooldown", CurseFieldCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SilencedEffect> SILENCED_EFFECT = EFFECTS.register("silenced", SilencedEffect::new);
} 