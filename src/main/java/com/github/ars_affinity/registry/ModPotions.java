package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.potion.IceBlastCooldownEffect;
import com.github.ars_affinity.potion.SanctuaryCooldownEffect;
import com.github.ars_affinity.potion.SanctuaryEffect;
import com.github.ars_affinity.potion.SilencedEffect;
import com.github.ars_affinity.potion.DeflectionCooldownEffect;
import com.github.ars_affinity.potion.FireDashCooldownEffect;
import com.github.ars_affinity.potion.GhostStepCooldownEffect;
import com.github.ars_affinity.potion.GroundSlamCooldownEffect;
import com.github.ars_affinity.potion.StoneSkinCooldownEffect;
import com.github.ars_affinity.potion.AirDashCooldownEffect;
import com.github.ars_affinity.potion.CurseFieldCooldownEffect;
import com.github.ars_affinity.potion.SwarmCooldownEffect;
import com.github.ars_affinity.potion.SwarmingEffect;
import com.github.ars_affinity.common.potion.ManipulationSicknessEffect;
import com.github.ars_affinity.potion.HydratedEffect;
import com.github.ars_affinity.potion.FireAffinityEffect;
import com.github.ars_affinity.potion.WaterAffinityEffect;
import com.github.ars_affinity.potion.EarthAffinityEffect;
import com.github.ars_affinity.potion.AirAffinityEffect;
import com.github.ars_affinity.potion.AbjurationAffinityEffect;
import com.github.ars_affinity.potion.NecromancyAffinityEffect;
import com.github.ars_affinity.potion.ConjurationAffinityEffect;
import com.github.ars_affinity.potion.ManipulationAffinityEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ArsAffinity.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ArsAffinity.MOD_ID);
    
    public static final DeferredHolder<MobEffect, IceBlastCooldownEffect> ICE_BLAST_COOLDOWN_EFFECT = EFFECTS.register("ice_blast_cooldown", IceBlastCooldownEffect::new);
    public static final DeferredHolder<MobEffect, DeflectionCooldownEffect> DEFLECTION_COOLDOWN_EFFECT = EFFECTS.register("deflection_cooldown", DeflectionCooldownEffect::new);
    public static final DeferredHolder<MobEffect, GhostStepCooldownEffect> GHOST_STEP_COOLDOWN_EFFECT = EFFECTS.register("ghost_step_cooldown", GhostStepCooldownEffect::new);
    public static final DeferredHolder<MobEffect, GroundSlamCooldownEffect> GROUND_SLAM_COOLDOWN_EFFECT = EFFECTS.register("ground_slam_cooldown", GroundSlamCooldownEffect::new);
    public static final DeferredHolder<MobEffect, StoneSkinCooldownEffect> STONE_SKIN_COOLDOWN_EFFECT = EFFECTS.register("stone_skin_cooldown", StoneSkinCooldownEffect::new);
    public static final DeferredHolder<MobEffect, AirDashCooldownEffect> AIR_DASH_COOLDOWN_EFFECT = EFFECTS.register("air_dash_cooldown", AirDashCooldownEffect::new);
    public static final DeferredHolder<MobEffect, FireDashCooldownEffect> FIRE_DASH_COOLDOWN_EFFECT = EFFECTS.register("fire_dash_cooldown", FireDashCooldownEffect::new);

    public static final DeferredHolder<MobEffect, SanctuaryEffect> SANCTUARY_EFFECT = EFFECTS.register("sanctuary", SanctuaryEffect::new);
    public static final DeferredHolder<MobEffect, SanctuaryCooldownEffect> SANCTUARY_COOLDOWN_EFFECT = EFFECTS.register("sanctuary_cooldown", SanctuaryCooldownEffect::new);
    public static final DeferredHolder<MobEffect, CurseFieldCooldownEffect> CURSE_FIELD_COOLDOWN_EFFECT = EFFECTS.register("curse_field_cooldown", CurseFieldCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SilencedEffect> SILENCED_EFFECT = EFFECTS.register("silenced", SilencedEffect::new);
    public static final DeferredHolder<MobEffect, SwarmCooldownEffect> SWARM_COOLDOWN_EFFECT = EFFECTS.register("swarm_cooldown", SwarmCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SwarmingEffect> SWARMING_EFFECT = EFFECTS.register("swarming", SwarmingEffect::new);
    public static final DeferredHolder<MobEffect, ManipulationSicknessEffect> MANIPULATION_SICKNESS_EFFECT = EFFECTS.register("manipulation_sickness", ManipulationSicknessEffect::new);
    public static final DeferredHolder<MobEffect, HydratedEffect> HYDRATED_EFFECT = EFFECTS.register("hydrated", HydratedEffect::new);
    
    // Affinity potion effects
    public static final DeferredHolder<MobEffect, FireAffinityEffect> FIRE_AFFINITY_EFFECT = EFFECTS.register("fire_affinity", FireAffinityEffect::new);
    public static final DeferredHolder<MobEffect, WaterAffinityEffect> WATER_AFFINITY_EFFECT = EFFECTS.register("water_affinity", WaterAffinityEffect::new);
    public static final DeferredHolder<MobEffect, EarthAffinityEffect> EARTH_AFFINITY_EFFECT = EFFECTS.register("earth_affinity", EarthAffinityEffect::new);
    public static final DeferredHolder<MobEffect, AirAffinityEffect> AIR_AFFINITY_EFFECT = EFFECTS.register("air_affinity", AirAffinityEffect::new);
    public static final DeferredHolder<MobEffect, AbjurationAffinityEffect> ABJURATION_AFFINITY_EFFECT = EFFECTS.register("abjuration_affinity", AbjurationAffinityEffect::new);
    public static final DeferredHolder<MobEffect, NecromancyAffinityEffect> NECROMANCY_AFFINITY_EFFECT = EFFECTS.register("necromancy_affinity", NecromancyAffinityEffect::new);
    public static final DeferredHolder<MobEffect, ConjurationAffinityEffect> CONJURATION_AFFINITY_EFFECT = EFFECTS.register("conjuration_affinity", ConjurationAffinityEffect::new);
    public static final DeferredHolder<MobEffect, ManipulationAffinityEffect> MANIPULATION_AFFINITY_EFFECT = EFFECTS.register("manipulation_affinity", ManipulationAffinityEffect::new);

    public static final DeferredHolder<Potion, Potion> SILENCED_POTION = POTIONS.register("silenced", () -> new Potion(new MobEffectInstance(SILENCED_EFFECT, 20 * 30)));
    
    // Affinity potions - instant effect, no duration
    public static final DeferredHolder<Potion, Potion> FIRE_AFFINITY_POTION = POTIONS.register("fire_affinity", () -> new Potion(new MobEffectInstance(FIRE_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> WATER_AFFINITY_POTION = POTIONS.register("water_affinity", () -> new Potion(new MobEffectInstance(WATER_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> EARTH_AFFINITY_POTION = POTIONS.register("earth_affinity", () -> new Potion(new MobEffectInstance(EARTH_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> AIR_AFFINITY_POTION = POTIONS.register("air_affinity", () -> new Potion(new MobEffectInstance(AIR_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> ABJURATION_AFFINITY_POTION = POTIONS.register("abjuration_affinity", () -> new Potion(new MobEffectInstance(ABJURATION_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> NECROMANCY_AFFINITY_POTION = POTIONS.register("necromancy_affinity", () -> new Potion(new MobEffectInstance(NECROMANCY_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> CONJURATION_AFFINITY_POTION = POTIONS.register("conjuration_affinity", () -> new Potion(new MobEffectInstance(CONJURATION_AFFINITY_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> MANIPULATION_AFFINITY_POTION = POTIONS.register("manipulation_affinity", () -> new Potion(new MobEffectInstance(MANIPULATION_AFFINITY_EFFECT, 1)));
} 