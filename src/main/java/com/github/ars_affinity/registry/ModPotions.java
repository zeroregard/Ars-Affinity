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
import java.util.HashMap;
import java.util.Map;

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
    
    // Affinity potion effects - organized by helper method
    private static final Map<String, DeferredHolder<MobEffect, ? extends MobEffect>> AFFINITY_EFFECTS = new HashMap<>();
    private static final Map<String, DeferredHolder<Potion, Potion>> AFFINITY_POTIONS = new HashMap<>();
    
    static {
        registerAffinityEffects();
    }

    public static final DeferredHolder<Potion, Potion> SILENCED_POTION = POTIONS.register("silenced", () -> new Potion(new MobEffectInstance(SILENCED_EFFECT, 20 * 30)));
    

    /**
     * Helper method to register all affinity effects and potions
     */
    private static void registerAffinityEffects() {
        // Level 1 Effects (placeholder)
        registerAffinityEffect("fire", "level_1", new FireAffinityLevel1Effect());
        registerAffinityEffect("water", "level_1", new WaterAffinityLevel1Effect());
        registerAffinityEffect("earth", "level_1", new EarthAffinityLevel1Effect());
        registerAffinityEffect("air", "level_1", new AirAffinityLevel1Effect());
        registerAffinityEffect("abjuration", "level_1", new AbjurationAffinityLevel1Effect());
        registerAffinityEffect("anima", "level_1", new AnimaAffinityLevel1Effect());
        registerAffinityEffect("conjuration", "level_1", new ConjurationAffinityLevel1Effect());
        registerAffinityEffect("manipulation", "level_1", new ManipulationAffinityLevel1Effect());
        
        // Level 2 Effects (increases affinity)
        registerAffinityEffect("fire", "level_2", new FireAffinityLevel2Effect());
        registerAffinityEffect("water", "level_2", new WaterAffinityLevel2Effect());
        registerAffinityEffect("earth", "level_2", new EarthAffinityLevel2Effect());
        registerAffinityEffect("air", "level_2", new AirAffinityLevel2Effect());
        registerAffinityEffect("abjuration", "level_2", new AbjurationAffinityLevel2Effect());
        registerAffinityEffect("anima", "level_2", new AnimaAffinityLevel2Effect());
        registerAffinityEffect("conjuration", "level_2", new ConjurationAffinityLevel2Effect());
        registerAffinityEffect("manipulation", "level_2", new ManipulationAffinityLevel2Effect());
    }
    
    /**
     * Helper method to register a single affinity effect and its corresponding potion
     */
    private static void registerAffinityEffect(String school, String level, MobEffect effect) {
        String effectId = school + "_affinity_" + level;
        DeferredHolder<MobEffect, MobEffect> effectHolder = EFFECTS.register(effectId, () -> effect);
        AFFINITY_EFFECTS.put(effectId, effectHolder);
        
        // Create and register the corresponding potion
        DeferredHolder<Potion, Potion> potionHolder = POTIONS.register(effectId, () -> new Potion(new MobEffectInstance(effectHolder, 1)));
        AFFINITY_POTIONS.put(effectId, potionHolder);
    }
    
    /**
     * Get an affinity effect by school and level
     */
    @SuppressWarnings("unchecked")
    public static DeferredHolder<MobEffect, MobEffect> getAffinityEffect(String school, String level) {
        return (DeferredHolder<MobEffect, MobEffect>) AFFINITY_EFFECTS.get(school + "_affinity_" + level);
    }
    
    /**
     * Get an affinity potion by school and level
     */
    public static DeferredHolder<Potion, Potion> getAffinityPotion(String school, String level) {
        return AFFINITY_POTIONS.get(school + "_affinity_" + level);
    }
    
    /**
     * Get all affinity effects
     */
    public static Map<String, DeferredHolder<MobEffect, ? extends MobEffect>> getAllAffinityEffects() {
        return new HashMap<>(AFFINITY_EFFECTS);
    }
    
    /**
     * Get all affinity potions
     */
    public static Map<String, DeferredHolder<Potion, Potion>> getAllAffinityPotions() {
        return new HashMap<>(AFFINITY_POTIONS);
    }
} 