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
import com.github.ars_affinity.potion.MidAirPhasingCooldownEffect;
import com.github.ars_affinity.potion.CurseFieldCooldownEffect;
import com.github.ars_affinity.potion.SwarmCooldownEffect;
import com.github.ars_affinity.potion.SwarmingEffect;
import com.github.ars_affinity.common.potion.ManipulationSicknessEffect;
import com.github.ars_affinity.potion.HydratedEffect;
import com.github.ars_affinity.potion.AffinityConsumableCooldownEffect;
import com.github.ars_affinity.potion.affinity_increase.FireAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.WaterAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.EarthAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.AirAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.AbjurationAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.AnimaAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.ConjurationAffinityEffect;
import com.github.ars_affinity.potion.affinity_increase.ManipulationAffinityEffect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import java.util.HashMap;
import java.util.Map;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID)
public class ModPotions {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ArsAffinity.MOD_ID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, ArsAffinity.MOD_ID);
    
    public static final DeferredHolder<MobEffect, IceBlastCooldownEffect> ICE_BLAST_COOLDOWN_EFFECT = EFFECTS.register("ice_blast_cooldown", IceBlastCooldownEffect::new);
    public static final DeferredHolder<MobEffect, DeflectionCooldownEffect> DEFLECTION_COOLDOWN_EFFECT = EFFECTS.register("deflection_cooldown", DeflectionCooldownEffect::new);
    public static final DeferredHolder<MobEffect, GhostStepCooldownEffect> GHOST_STEP_COOLDOWN_EFFECT = EFFECTS.register("ghost_step_cooldown", GhostStepCooldownEffect::new);
    public static final DeferredHolder<MobEffect, GroundSlamCooldownEffect> GROUND_SLAM_COOLDOWN_EFFECT = EFFECTS.register("ground_slam_cooldown", GroundSlamCooldownEffect::new);
    public static final DeferredHolder<MobEffect, StoneSkinCooldownEffect> STONE_SKIN_COOLDOWN_EFFECT = EFFECTS.register("stone_skin_cooldown", StoneSkinCooldownEffect::new);
    public static final DeferredHolder<MobEffect, AirDashCooldownEffect> AIR_DASH_COOLDOWN_EFFECT = EFFECTS.register("air_dash_cooldown", AirDashCooldownEffect::new);
    public static final DeferredHolder<MobEffect, MidAirPhasingCooldownEffect> MID_AIR_PHASING_COOLDOWN_EFFECT = EFFECTS.register("mid_air_phasing_cooldown", MidAirPhasingCooldownEffect::new);
    public static final DeferredHolder<MobEffect, FireDashCooldownEffect> FIRE_DASH_COOLDOWN_EFFECT = EFFECTS.register("fire_dash_cooldown", FireDashCooldownEffect::new);

    public static final DeferredHolder<MobEffect, SanctuaryEffect> SANCTUARY_EFFECT = EFFECTS.register("sanctuary", SanctuaryEffect::new);
    public static final DeferredHolder<MobEffect, SanctuaryCooldownEffect> SANCTUARY_COOLDOWN_EFFECT = EFFECTS.register("sanctuary_cooldown", SanctuaryCooldownEffect::new);
    public static final DeferredHolder<MobEffect, CurseFieldCooldownEffect> CURSE_FIELD_COOLDOWN_EFFECT = EFFECTS.register("curse_field_cooldown", CurseFieldCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SilencedEffect> SILENCED_EFFECT = EFFECTS.register("silenced", SilencedEffect::new);
    public static final DeferredHolder<MobEffect, SwarmCooldownEffect> SWARM_COOLDOWN_EFFECT = EFFECTS.register("swarm_cooldown", SwarmCooldownEffect::new);
    public static final DeferredHolder<MobEffect, SwarmingEffect> SWARMING_EFFECT = EFFECTS.register("swarming", SwarmingEffect::new);
    public static final DeferredHolder<MobEffect, ManipulationSicknessEffect> MANIPULATION_SICKNESS_EFFECT = EFFECTS.register("manipulation_sickness", ManipulationSicknessEffect::new);
    public static final DeferredHolder<MobEffect, HydratedEffect> HYDRATED_EFFECT = EFFECTS.register("hydrated", HydratedEffect::new);
    public static final DeferredHolder<MobEffect, AffinityConsumableCooldownEffect> AFFINITY_CONSUMABLE_COOLDOWN_EFFECT = EFFECTS.register("affinity_consumable_cooldown", AffinityConsumableCooldownEffect::new);
    
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
        // Single tier affinity effects
        registerAffinityEffect("fire", new FireAffinityEffect());
        registerAffinityEffect("water", new WaterAffinityEffect());
        registerAffinityEffect("earth", new EarthAffinityEffect());
        registerAffinityEffect("air", new AirAffinityEffect());
        registerAffinityEffect("abjuration", new AbjurationAffinityEffect());
        registerAffinityEffect("anima", new AnimaAffinityEffect());
        registerAffinityEffect("conjuration", new ConjurationAffinityEffect());
        registerAffinityEffect("manipulation", new ManipulationAffinityEffect());
    }
    
    /**
     * Helper method to register a single affinity effect and its corresponding potion
     */
    private static void registerAffinityEffect(String school, MobEffect effect) {
        String effectId = school + "_affinity";
        DeferredHolder<MobEffect, MobEffect> effectHolder = EFFECTS.register(effectId, () -> effect);
        AFFINITY_EFFECTS.put(effectId, effectHolder);
        
        // Create and register the corresponding potion
        DeferredHolder<Potion, Potion> potionHolder = POTIONS.register(effectId, () -> new Potion(new MobEffectInstance(effectHolder, 1)));
        AFFINITY_POTIONS.put(effectId, potionHolder);
    }
    
    /**
     * Get an affinity effect by school
     */
    @SuppressWarnings("unchecked")
    public static DeferredHolder<MobEffect, MobEffect> getAffinityEffect(String school) {
        return (DeferredHolder<MobEffect, MobEffect>) AFFINITY_EFFECTS.get(school + "_affinity");
    }
    
    /**
     * Get an affinity potion by school
     */
    public static DeferredHolder<Potion, Potion> getAffinityPotion(String school) {
        return AFFINITY_POTIONS.get(school + "_affinity");
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
    
    /**
     * Check if a player has the affinity consumable cooldown effect
     */
    public static boolean hasAffinityCooldown(net.minecraft.world.entity.LivingEntity entity) {
        return entity.hasEffect(AFFINITY_CONSUMABLE_COOLDOWN_EFFECT);
    }
    
    /**
     * Register brewing recipes for affinity potions
     * Note: Brewing recipes are now handled by JSON files in data/ars_affinity/recipe/
     * This method is kept for potential future use but currently disabled to avoid duplicates
     */
    @SubscribeEvent
    public static void addBrewingRecipes(final RegisterBrewingRecipesEvent event) {
        // Brewing recipes are now handled by JSON files in data/ars_affinity/recipe/
        // This prevents duplicate recipe registration that was causing 20% instead of 10% increase
        ArsAffinity.LOGGER.info("Brewing recipes for affinity potions are handled by JSON files, skipping code-based registration");
        
        // Note: Anima (Necromancy) is skipped as there's no corresponding essence in Ars Nouveau
    }
} 