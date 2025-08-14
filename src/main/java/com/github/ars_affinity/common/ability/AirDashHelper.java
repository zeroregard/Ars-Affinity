package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectColdSnap;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class AirDashHelper {
    
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        ArsAffinity.LOGGER.info("AIR DASH: Starting execution for player {} with perk: manaCost={}, cooldown={}, dashLength={}, dashDuration={}", 
            player.getName().getString(), perk.manaCost, perk.cooldown, perk.dashLength, perk.dashDuration);
        
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            ArsAffinity.LOGGER.info("AIR DASH: Player {} has no mana capability", player.getName().getString());
            return;
        }
        
        double currentMana = manaCap.getCurrentMana();
        double maxMana = manaCap.getMaxMana();
        double requiredMana = perk.manaCost * maxMana;
        
        if (currentMana < requiredMana) {
            ArsAffinity.LOGGER.info("AIR DASH: Player {} doesn't have enough mana. Required: {}, Current: {}", 
                player.getName().getString(), requiredMana, currentMana);
            return;
        }
        
        if (isPlayerOnCooldown(player)) {
            return;
        }
        
        applyCooldown(player, perk.cooldown);
        
        // Calculate dash parameters
        float dashLength = perk.dashLength;
        float dashDuration = perk.dashDuration;
        
        // Execute the dash
        performDash(player, dashLength, dashDuration);
        
        // Check for enemies in the dash path and apply effects
        damageEntitiesInDashPath(player, dashLength);
        
        // Spawn particle effects
        spawnParticleEffects(player);
        
        // Play sound effects
        playSoundEffects(player);
        
        // Consume mana
        consumeMana(player, perk);
    }
    
    private static boolean isPlayerOnCooldown(ServerPlayer player) {
        return player.hasEffect(ModPotions.AIR_DASH_COOLDOWN_EFFECT);
    }
    
    private static void applyCooldown(ServerPlayer player, int cooldownTicks) {
        player.addEffect(new MobEffectInstance(ModPotions.AIR_DASH_COOLDOWN_EFFECT, cooldownTicks, 0, false, false, false));
    }
    
    private static void performDash(ServerPlayer player, float dashLength, float dashDuration) {
        // Get the direction the player is facing
        Vec3 lookDirection = player.getLookAngle();
        
        // Calculate the dash velocity (distance / time)
        double dashSpeed = dashLength / dashDuration;
        Vec3 dashVelocity = lookDirection.scale(dashSpeed);
        
        // Apply the dash velocity to the player
        player.setDeltaMovement(dashVelocity);
        
        // Set the player's velocity to prevent immediate cancellation
        player.hurtMarked = true;
        
        // Schedule velocity reset after dash duration
        player.level().getServer().tell(new net.minecraft.server.TickTask(
            (int) (player.level().getServer().getTickCount() + (dashDuration * 20)), 
            () -> {
                if (player.isAlive()) {
                    // Reset velocity to normal after dash
                    Vec3 currentVel = player.getDeltaMovement();
                    Vec3 horizontalVel = new Vec3(currentVel.x, 0, currentVel.z);
                    if (horizontalVel.lengthSqr() > 0.01) {
                        player.setDeltaMovement(horizontalVel.scale(0.1)); // Reduce horizontal velocity
                    }
                }
            }
        ));
    }
    
    private static void damageEntitiesInDashPath(ServerPlayer player, float dashLength) {
        Vec3 startPos = player.position();
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPos = startPos.add(lookDirection.scale(dashLength));
        
        // Create AABB for the dash path
        AABB dashPath = new AABB(
            Math.min(startPos.x, endPos.x) - 0.5, startPos.y - 0.5, Math.min(startPos.z, endPos.z) - 0.5,
            Math.max(startPos.x, endPos.x) + 0.5, startPos.y + 1.5, Math.max(startPos.z, endPos.z) + 0.5
        );
        
        // Find entities in the dash path
        List<Entity> entitiesInPath = player.level().getEntities(player, dashPath, entity -> 
            entity instanceof LivingEntity && entity != player && !entity.isAlliedTo(player));
        
        ArsAffinity.LOGGER.info("AIR DASH: Found {} entities in dash path", entitiesInPath.size());
        
        // Apply effects to entities in the path
        for (Entity entity : entitiesInPath) {
            if (entity instanceof LivingEntity livingEntity) {
                applyEntityEffects(livingEntity);
            }
        }
    }
    
    private static void applyEntityEffects(LivingEntity entity) {
        // Create a spell with EffectColdSnap (as requested, using this instead of EffectHarm)
        Spell spell = new Spell();
        spell = spell.add(MethodTouch.INSTANCE);
        spell = spell.add(EffectColdSnap.INSTANCE);
        
        SpellContext context = SpellContext.fromEntity(spell, entity, entity.getMainHandItem());
        SpellResolver resolver = new SpellResolver(context);
        
        resolver.onResolveEffect(entity.level(), new EntityHitResult(entity));
        
        ArsAffinity.LOGGER.info("AIR DASH: Applied effects to entity {}", entity.getName().getString());
    }
    
    private static void spawnParticleEffects(ServerPlayer player) {
        Vec3 playerPos = player.position();
        
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Spawn air/wind particles along the dash path
            Vec3 lookDirection = player.getLookAngle();
            float dashLength = 10.0f; // Default dash length for particle effects
            
            for (int i = 0; i < 30; i++) {
                double progress = (double) i / 30.0;
                Vec3 particlePos = playerPos.add(lookDirection.scale(dashLength * progress));
                
                // Add some randomness to the particle positions
                double offsetX = (Math.random() - 0.5) * 0.5;
                double offsetY = (Math.random() - 0.5) * 0.5;
                double offsetZ = (Math.random() - 0.5) * 0.5;
                
                serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    particlePos.x + offsetX,
                    particlePos.y + offsetY,
                    particlePos.z + offsetZ,
                    1, 0, 0, 0, 0.05
                );
            }
            
            // Spawn some sparkle particles for visual effect
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 2.0;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * 2.0;
                
                serverLevel.sendParticles(
                    ParticleTypes.FIREWORK,
                    playerPos.x + offsetX,
                    playerPos.y + offsetY,
                    playerPos.z + offsetZ,
                    1, 0, 0, 0, 0.1
                );
            }
        }
    }
    
    private static void playSoundEffects(ServerPlayer player) {
        Vec3 playerPos = player.position();
        
        // Play whoosh sound for the dash
        player.level().playSound(
            null,
            playerPos.x,
            playerPos.y,
            playerPos.z,
            SoundEvents.ELYTRA_FLYING,
            SoundSource.PLAYERS,
            0.8f,
            1.2f
        );
        
        // Play a second sound for impact
        player.level().getServer().tell(new net.minecraft.server.TickTask(
            player.level().getServer().getTickCount() + 6, 
            () -> {
                if (player.isAlive()) {
                    player.level().playSound(
                        null,
                        playerPos.x,
                        playerPos.y,
                        playerPos.z,
                        SoundEvents.PLAYER_ATTACK_SWEEP,
                        SoundSource.PLAYERS,
                        0.6f,
                        1.0f
                    );
                }
            }
        ));
    }
    
    private static void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap != null) {
            double currentMana = manaCap.getCurrentMana();
            double maxMana = manaCap.getMaxMana();
            double requiredMana = perk.manaCost * maxMana;
            manaCap.removeMana((int)requiredMana);
            
            ArsAffinity.LOGGER.info("AIR DASH: Consumed {} mana from player {}", requiredMana, player.getName().getString());
        }
    }
}