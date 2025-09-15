package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.AirborneCapabilityProvider;
import com.github.ars_affinity.registry.ModCapabilities;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.potion.StaticChargeEffect;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;

public class StaticChargeEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Update airborne capability
        AirborneCapabilityProvider.attach(player);
        
        // Update static charge effect
        StaticChargeEffect.updateStaticCharge(player);
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent event) {
        if (!(event.getCaster() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            MobEffectInstance staticChargeEffect = player.getEffect(StaticChargeEffect.class);
            
            if (staticChargeEffect != null && staticChargeEffect.getAmplifier() >= 0) {
                // Release static charge as lightning damage
                int amplifier = staticChargeEffect.getAmplifier();
                releaseStaticCharge(player, amplifier + 1, perk); // +1 because amplifier is 0-based
                
                // Remove the effect
                player.removeEffect(StaticChargeEffect.class);
            }
        });
    }

    @SubscribeEvent
    public static void onPlayerAttack(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STATIC_CHARGE, AffinityPerk.StaticChargePerk.class, perk -> {
            MobEffectInstance staticChargeEffect = player.getEffect(StaticChargeEffect.class);
            
            if (staticChargeEffect != null && staticChargeEffect.getAmplifier() >= 0) {
                // Release static charge as lightning damage
                int amplifier = staticChargeEffect.getAmplifier();
                releaseStaticCharge(player, amplifier + 1, perk); // +1 because amplifier is 0-based
                
                // Remove the effect
                player.removeEffect(StaticChargeEffect.class);
            }
        });
    }

    private static void releaseStaticCharge(Player player, int chargeLevel, AffinityPerk.StaticChargePerk perk) {
        ServerLevel level = (ServerLevel) player.level();
        Vec3 playerPos = player.position();
        
        // Calculate damage based on charge level (1-4, where 4 is max)
        float damage = perk.damage * chargeLevel;
        
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
        
        ArsAffinity.LOGGER.info("Static Charge released! Player {} dealt {} lightning damage to {} entities (charge level: {})", 
            player.getName().getString(), damage, entities.size(), chargeLevel);
    }
}