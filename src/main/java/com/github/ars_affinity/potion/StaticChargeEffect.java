package com.github.ars_affinity.potion;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.AirborneCapabilityProvider;
import com.github.ars_affinity.registry.ModCapabilities;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.EffectCure;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class StaticChargeEffect extends MobEffect {
    
    public static final int MAX_AMPLIFIER = 3;
    
    public StaticChargeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFFF00); // Yellow color for electricity
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Visual feedback - electric spark particles around the player
        ServerLevel level = (ServerLevel) player.level();
        var pos = player.position();
        
        // More particles for higher amplifiers
        int particleCount = (amplifier + 1) * 3;
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
            pos.x, pos.y + 1.0, pos.z, 
            particleCount, 0.5, 0.5, 0.5, 0.05);
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Apply visual effects every 10 ticks
        return duration % 10 == 0;
    }
    
    @Override
    public void fillEffectCures(@NotNull Set<EffectCure> cures, @NotNull MobEffectInstance effectInstance) {
        // Static charge cannot be cured by normal means
    }
    
    /**
     * Update the static charge effect based on airborne time
     */
    public static void updateStaticCharge(Player player) {
        if (player.level().isClientSide()) return;
        
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            player.getCapability(ModCapabilities.AIRBORNE_CAPABILITY).ifPresent(airborne -> {
                long airborneTime = airborne.getAirborneTime();
                
                // Calculate what amplifier level we should be at
                int targetAmplifier = calculateAmplifier(airborneTime, perk.buildUpTime);
                
                // Get current effect
                MobEffectInstance currentEffect = player.getEffect(this);
                int currentAmplifier = currentEffect != null ? currentEffect.getAmplifier() : -1;
                
                // Update effect if needed
                if (targetAmplifier != currentAmplifier) {
                    if (targetAmplifier >= 0) {
                        // Add or update the effect
                        player.addEffect(new MobEffectInstance(this, Integer.MAX_VALUE, targetAmplifier, false, true, true));
                        ArsAffinity.LOGGER.debug("Static Charge updated to amplifier {} for player {}", targetAmplifier, player.getName().getString());
                    } else {
                        // Remove the effect
                        player.removeEffect(this);
                        ArsAffinity.LOGGER.debug("Static Charge removed for player {}", player.getName().getString());
                    }
                }
            });
        });
    }
    
    /**
     * Calculate the amplifier level based on airborne time and build up time
     */
    private static int calculateAmplifier(long airborneTime, int buildUpTime) {
        if (airborneTime < buildUpTime) {
            return -1; // No effect yet
        }
        
        // Each level takes 2x the previous level's time
        long timeForLevel1 = buildUpTime;
        long timeForLevel2 = timeForLevel1 * 2;
        long timeForLevel3 = timeForLevel2 * 2;
        
        if (airborneTime >= timeForLevel3) {
            return MAX_AMPLIFIER; // Level 3
        } else if (airborneTime >= timeForLevel2) {
            return 2; // Level 2
        } else if (airborneTime >= timeForLevel1) {
            return 1; // Level 1
        }
        
        return 0; // Level 0
    }
}