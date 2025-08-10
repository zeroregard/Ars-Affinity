package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.server.level.ServerPlayer;

public class IceBlastHelper {
    
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, int requiredMana, int currentMana) {
        // Check if player is on cooldown
        if (player.hasEffect(ModPotions.ICE_BLAST_COOLDOWN_EFFECT)) {
            return;
        }

        // Apply cooldown
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModPotions.ICE_BLAST_COOLDOWN_EFFECT, perk.cooldown));

        // Calculate scaling based on mana consumed
        float manaScaling = (float) requiredMana / currentMana;
        float scaledDamage = perk.damage * manaScaling;
        int scaledFreezeTime = (int) (perk.freezeTime * manaScaling);
        float scaledRadius = perk.radius * manaScaling;

        // Get entities in range
        net.minecraft.world.phys.Vec3 playerPos = player.position();
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
            playerPos.x - scaledRadius, playerPos.y - scaledRadius, playerPos.z - scaledRadius,
            playerPos.x + scaledRadius, playerPos.y + scaledRadius, playerPos.z + scaledRadius
        );

        java.util.List<net.minecraft.world.entity.Entity> entities = player.level().getEntities(player, area, entity -> 
            entity instanceof net.minecraft.world.entity.LivingEntity && entity != player && entity.isAlive());

        // Apply effects to entities
        for (net.minecraft.world.entity.Entity entity : entities) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                // Apply damage
                livingEntity.hurt(player.damageSources().playerAttack(player), scaledDamage);
                
                // Apply freezing effect
                livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, scaledFreezeTime, 2));
                livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN, scaledFreezeTime, 2));
                
                // Visual effects
                if (livingEntity.level().isClientSide) {
                    livingEntity.level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, 
                        livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 0, 0, 0);
                }
            }
        }

        // Visual and sound effects
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // Particle effects around the player
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * scaledRadius;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            player.level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, x, player.getY() + 1, z, 0, 0.1, 0);
        }

        ArsAffinity.LOGGER.info("Player {} used ICE BLAST ability with {} mana cost, {} damage, {} freeze time, {} radius", 
            player.getName().getString(), requiredMana, scaledDamage, scaledFreezeTime, scaledRadius);
    }
} 