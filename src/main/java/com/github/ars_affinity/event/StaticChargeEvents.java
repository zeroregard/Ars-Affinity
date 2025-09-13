package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StaticChargeEvents {

    // Track static charge buildup for each player
    private static final Map<UUID, Float> staticChargeMap = new HashMap<>();
    private static final Map<UUID, Long> lastGroundTimeMap = new HashMap<>();

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            UUID playerId = player.getUUID();
            
            if (player.onGround()) {
                // Player is on ground, reset tracking
                lastGroundTimeMap.remove(playerId);
            } else {
                // Player is airborne, build static charge
                long currentTime = player.level().getGameTime();
                Long lastGroundTime = lastGroundTimeMap.get(playerId);
                
                if (lastGroundTime == null) {
                    lastGroundTimeMap.put(playerId, currentTime);
                } else {
                    // Calculate time airborne
                    long timeAirborne = currentTime - lastGroundTime;
                    float chargeBuilt = (float) timeAirborne * perk.chargeRate;
                    
                    // Cap at max charge
                    float currentCharge = staticChargeMap.getOrDefault(playerId, 0.0f);
                    float newCharge = Math.min(currentCharge + chargeBuilt, perk.maxCharge);
                    staticChargeMap.put(playerId, newCharge);
                    
                    // Visual feedback - particles when charging
                    if (newCharge > currentCharge && newCharge > 0.1f) {
                        ServerLevel level = (ServerLevel) player.level();
                        Vec3 pos = player.position();
                        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                            pos.x, pos.y + 1.0, pos.z, 
                            (int)(newCharge * 2), 0.3, 0.3, 0.3, 0.05);
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        if (!(event.getCaster() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            UUID playerId = player.getUUID();
            Float staticCharge = staticChargeMap.get(playerId);
            
            if (staticCharge != null && staticCharge > 0.1f) {
                // Release static charge as lightning damage
                releaseStaticCharge(player, staticCharge, perk);
                staticChargeMap.put(playerId, 0.0f); // Reset charge
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            UUID playerId = player.getUUID();
            Float staticCharge = staticChargeMap.get(playerId);
            
            if (staticCharge != null && staticCharge > 0.1f) {
                // Release static charge as lightning damage
                releaseStaticCharge(player, staticCharge, perk);
                staticChargeMap.put(playerId, 0.0f); // Reset charge
            }
        });
    }

    private static void releaseStaticCharge(Player player, float chargeAmount, AffinityPerk.StaticChargePerk perk) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 playerPos = player.position();
        
        // Calculate damage based on charge amount
        float damage = perk.damage * (chargeAmount / perk.maxCharge);
        
        // Find entities in AoE radius
        AABB aoeBox = new AABB(
            playerPos.x - perk.aoeRadius, playerPos.y - perk.aoeRadius, playerPos.z - perk.aoeRadius,
            playerPos.x + perk.aoeRadius, playerPos.y + perk.aoeRadius, playerPos.z + perk.aoeRadius
        );
        
        var entities = level.getEntitiesOfClass(LivingEntity.class, aoeBox, 
            entity -> entity != player && entity.distanceTo(player) <= perk.aoeRadius);
        
        // Apply lightning damage to nearby entities
        for (LivingEntity target : entities) {
            DamageSource lightningSource = level.damageSources().source(DamageTypes.LIGHTNING_BOLT, player);
            target.hurt(lightningSource, damage);
            
            // Visual effect - lightning particles
            Vec3 targetPos = target.position();
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK, 
                targetPos.x, targetPos.y + 1.0, targetPos.z, 
                10, 0.5, 0.5, 0.5, 0.1);
        }
        
        // Visual feedback for the player
        level.sendParticles(ParticleTypes.LIGHTNING, 
            playerPos.x, playerPos.y + 2.0, playerPos.z, 
            5, 1.0, 1.0, 1.0, 0.1);
        
        ArsAffinity.LOGGER.info("Static Charge released! Player {} dealt {} lightning damage to {} entities (charge: {})", 
            player.getName().getString(), damage, entities.size(), chargeAmount);
    }
}