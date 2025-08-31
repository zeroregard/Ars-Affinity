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
import com.github.ars_affinity.potion.affinity_increase.FireAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.WaterAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.EarthAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.AirAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.AbjurationAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.AnimaAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.ConjurationAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.ManipulationAffinityLevel1Effect;
import com.github.ars_affinity.potion.affinity_increase.FireAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.WaterAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.EarthAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.AirAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.AbjurationAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.AnimaAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.ConjurationAffinityLevel2Effect;
import com.github.ars_affinity.potion.affinity_increase.ManipulationAffinityLevel2Effect;

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
    
    // Affinity potion effects - Level 1 (placeholder)
    public static final DeferredHolder<MobEffect, FireAffinityLevel1Effect> FIRE_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("fire_affinity_level_1", FireAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, WaterAffinityLevel1Effect> WATER_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("water_affinity_level_1", WaterAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, EarthAffinityLevel1Effect> EARTH_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("earth_affinity_level_1", EarthAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, AirAffinityLevel1Effect> AIR_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("air_affinity_level_1", AirAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, AbjurationAffinityLevel1Effect> ABJURATION_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("abjuration_affinity_level_1", AbjurationAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, AnimaAffinityLevel1Effect> ANIMA_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("anima_affinity_level_1", AnimaAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, ConjurationAffinityLevel1Effect> CONJURATION_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("conjuration_affinity_level_1", ConjurationAffinityLevel1Effect::new);
    public static final DeferredHolder<MobEffect, ManipulationAffinityLevel1Effect> MANIPULATION_AFFINITY_LEVEL1_EFFECT = EFFECTS.register("manipulation_affinity_level_1", ManipulationAffinityLevel1Effect::new);
    
    // Affinity potion effects - Level 2 (increases affinity)
    public static final DeferredHolder<MobEffect, FireAffinityLevel2Effect> FIRE_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("fire_affinity_level_2", FireAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, WaterAffinityLevel2Effect> WATER_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("water_affinity_level_2", WaterAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, EarthAffinityLevel2Effect> EARTH_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("earth_affinity_level_2", EarthAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, AirAffinityLevel2Effect> AIR_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("air_affinity_level_2", AirAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, AbjurationAffinityLevel2Effect> ABJURATION_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("abjuration_affinity_level_2", AbjurationAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, AnimaAffinityLevel2Effect> ANIMA_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("anima_affinity_level_2", AnimaAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, ConjurationAffinityLevel2Effect> CONJURATION_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("conjuration_affinity_level_2", ConjurationAffinityLevel2Effect::new);
    public static final DeferredHolder<MobEffect, ManipulationAffinityLevel2Effect> MANIPULATION_AFFINITY_LEVEL2_EFFECT = EFFECTS.register("manipulation_affinity_level_2", ManipulationAffinityLevel2Effect::new);

    public static final DeferredHolder<Potion, Potion> SILENCED_POTION = POTIONS.register("silenced", () -> new Potion(new MobEffectInstance(SILENCED_EFFECT, 20 * 30)));
    
    // Affinity potions - Level 1 (placeholder, no duration)
    public static final DeferredHolder<Potion, Potion> FIRE_AFFINITY_LEVEL1_POTION = POTIONS.register("fire_affinity_level_1", () -> new Potion(new MobEffectInstance(FIRE_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> WATER_AFFINITY_LEVEL1_POTION = POTIONS.register("water_affinity_level_1", () -> new Potion(new MobEffectInstance(WATER_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> EARTH_AFFINITY_LEVEL1_POTION = POTIONS.register("earth_affinity_level_1", () -> new Potion(new MobEffectInstance(EARTH_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> AIR_AFFINITY_LEVEL1_POTION = POTIONS.register("air_affinity_level_1", () -> new Potion(new MobEffectInstance(AIR_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> ABJURATION_AFFINITY_LEVEL1_POTION = POTIONS.register("abjuration_affinity_level_1", () -> new Potion(new MobEffectInstance(ABJURATION_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> ANIMA_AFFINITY_LEVEL1_POTION = POTIONS.register("anima_affinity_level_1", () -> new Potion(new MobEffectInstance(ANIMA_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> CONJURATION_AFFINITY_LEVEL1_POTION = POTIONS.register("conjuration_affinity_level_1", () -> new Potion(new MobEffectInstance(CONJURATION_AFFINITY_LEVEL1_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> MANIPULATION_AFFINITY_LEVEL1_POTION = POTIONS.register("manipulation_affinity_level_1", () -> new Potion(new MobEffectInstance(MANIPULATION_AFFINITY_LEVEL1_EFFECT, 1)));
    
    // Affinity potions - Level 2 (increases affinity, instant effect, no duration)
    public static final DeferredHolder<Potion, Potion> FIRE_AFFINITY_LEVEL2_POTION = POTIONS.register("fire_affinity_level_2", () -> new Potion(new MobEffectInstance(FIRE_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> WATER_AFFINITY_LEVEL2_POTION = POTIONS.register("water_affinity_level_2", () -> new Potion(new MobEffectInstance(WATER_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> EARTH_AFFINITY_LEVEL2_POTION = POTIONS.register("earth_affinity_level_2", () -> new Potion(new MobEffectInstance(EARTH_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> AIR_AFFINITY_LEVEL2_POTION = POTIONS.register("air_affinity_level_2", () -> new Potion(new MobEffectInstance(AIR_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> ABJURATION_AFFINITY_LEVEL2_POTION = POTIONS.register("abjuration_affinity_level_2", () -> new Potion(new MobEffectInstance(ABJURATION_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> ANIMA_AFFINITY_LEVEL2_POTION = POTIONS.register("anima_affinity_level_2", () -> new Potion(new MobEffectInstance(ANIMA_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> CONJURATION_AFFINITY_LEVEL2_POTION = POTIONS.register("conjuration_affinity_level_2", () -> new Potion(new MobEffectInstance(CONJURATION_AFFINITY_LEVEL2_EFFECT, 1)));
    public static final DeferredHolder<Potion, Potion> MANIPULATION_AFFINITY_LEVEL2_POTION = POTIONS.register("manipulation_affinity_level_2", () -> new Potion(new MobEffectInstance(MANIPULATION_AFFINITY_LEVEL2_EFFECT, 1)));
} 