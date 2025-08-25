package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.potion.SwarmCooldownEffect;
import com.github.ars_affinity.potion.SwarmingEffect;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.helper.MathUtils;
import com.hollingsworth.arsnouveau.api.entity.ISummon;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SwarmHelper {
    private static final SwarmHelper INSTANCE = new SwarmHelper();

    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        INSTANCE.executeAbilityInternal(player, perk);
    }

    private void executeAbilityInternal(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            return;
        }

        double currentMana = manaCap.getCurrentMana();
        if (currentMana < perk.manaCost) {
            return;
        }

        if (isPlayerOnCooldown(player)) {
            return;
        }

        applyCooldown(player, perk.cooldown);

        executeSwarm(player);

        consumeMana(player, perk, manaCap);

        spawnParticles(player);
        playSounds(player);
    }

    private void executeSwarm(ServerPlayer player) {
        ArsAffinity.LOGGER.info("SWARM: Executing swarm ability for player {}", player.getName().getString());
        
        double targetRange = ArsAffinityConfig.SWARM_DEFAULT_TARGET_RANGE.get();
        ArsAffinity.LOGGER.info("SWARM: Looking for targets within {} blocks", targetRange);
        
        EntityHitResult entityHitResult = MathUtils.getLookedAtEntity(player, targetRange);
        if (entityHitResult == null) {
            ArsAffinity.LOGGER.info("SWARM: No valid target found for player {} within {} blocks", player.getName().getString(), targetRange);
            ArsAffinity.LOGGER.info("SWARM: Player position: ({}, {}, {})", player.getX(), player.getY(), player.getZ());
            ArsAffinity.LOGGER.info("SWARM: Player looking direction: ({}, {}, {})", player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z);
            return;
        }
        
        Entity targetEntity = entityHitResult.getEntity();
        ArsAffinity.LOGGER.info("SWARM: Found target entity: {} (type: {}) at ({}, {}, {})", 
            targetEntity.getName().getString(), targetEntity.getType().toString(), 
            targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
        
        if (!(targetEntity instanceof LivingEntity)) {
            ArsAffinity.LOGGER.info("SWARM: Target is not a living entity: {} (class: {})", 
                targetEntity.getName().getString(), targetEntity.getClass().getSimpleName());
            return;
        }
        
        LivingEntity livingTarget = (LivingEntity) targetEntity;
        if (livingTarget == player) {
            ArsAffinity.LOGGER.info("SWARM: Player {} cannot command minions to attack themselves", player.getName().getString());
            return;
        }
        
        ArsAffinity.LOGGER.info("SWARM: Valid target confirmed: {} (health: {}/{}, alive: {})", 
            livingTarget.getName().getString(), livingTarget.getHealth(), livingTarget.getMaxHealth(), livingTarget.isAlive());
        
        List<Entity> nearbyEntities = findPlayerSummons(player);
        ArsAffinity.LOGGER.info("SWARM: Found {} summoned entities for player {}", nearbyEntities.size(), player.getName().getString());
        
        if (nearbyEntities.isEmpty()) {
            ArsAffinity.LOGGER.info("SWARM: No summoned entities found for player {}", player.getName().getString());
            return;
        }
        
        // Log details about each summon
        for (Entity summon : nearbyEntities) {
            ArsAffinity.LOGGER.info("SWARM: Summon {} (type: {}) at ({}, {}, {}) - alive: {}", 
                summon.getName().getString(), summon.getType().toString(),
                summon.getX(), summon.getY(), summon.getZ(), summon.isAlive());
        }
        
        commandSummonsToAttack(nearbyEntities, livingTarget, player);
        
        ArsAffinity.LOGGER.info("SWARM: Successfully commanded {} summoned entities to attack {}", 
            nearbyEntities.size(), livingTarget.getName().getString());
    }

    private List<Entity> findPlayerSummons(ServerPlayer player) {
        UUID playerUUID = player.getUUID();
        double swarmRadius = ArsAffinityConfig.SWARM_DEFAULT_RADIUS.get();
        AABB searchArea = new AABB(
            player.getX() - swarmRadius, player.getY() - swarmRadius, player.getZ() - swarmRadius,
            player.getX() + swarmRadius, player.getY() + swarmRadius, player.getZ() + swarmRadius
        );
        
        ArsAffinity.LOGGER.info("SWARM: Searching for summons in radius {} blocks around player {} at ({}, {}, {})", 
            swarmRadius, player.getName().getString(), player.getX(), player.getY(), player.getZ());
        
        // Get all entities in the area first
        List<Entity> allEntities = player.level().getEntities(player, searchArea, entity -> true);
        ArsAffinity.LOGGER.info("SWARM: Found {} total entities in search area", allEntities.size());
        
        // Filter for valid summons
        List<Entity> validSummons = new ArrayList<>();
        for (Entity entity : allEntities) {
            ArsAffinity.LOGGER.debug("SWARM: Checking entity {} (type: {}, class: {})", 
                entity.getName().getString(), entity.getType().toString(), entity.getClass().getSimpleName());
            
            if (entity instanceof ISummon summon) {
                ArsAffinity.LOGGER.debug("SWARM: Entity {} implements ISummon", entity.getName().getString());
                
                if (playerUUID.equals(summon.getOwnerUUID())) {
                    ArsAffinity.LOGGER.debug("SWARM: Entity {} belongs to player", entity.getName().getString());
                    
                    if (entity instanceof LivingEntity livingEntity) {
                        ArsAffinity.LOGGER.debug("SWARM: Entity {} is LivingEntity, alive: {}", 
                            entity.getName().getString(), livingEntity.isAlive());
                        
                        if (livingEntity.isAlive()) {
                            ArsAffinity.LOGGER.info("SWARM: Entity {} is a valid summon", entity.getName().getString());
                            validSummons.add(entity);
                        } else {
                            ArsAffinity.LOGGER.debug("SWARM: Entity {} is not alive", entity.getName().getString());
                        }
                    } else {
                        ArsAffinity.LOGGER.debug("SWARM: Entity {} is not a LivingEntity", entity.getName().getString());
                    }
                } else {
                    ArsAffinity.LOGGER.debug("SWARM: Entity {} belongs to different player: {} vs {}", 
                        entity.getName().getString(), summon.getOwnerUUID(), playerUUID);
                }
            } else {
                ArsAffinity.LOGGER.debug("SWARM: Entity {} does not implement ISummon", entity.getName().getString());
            }
        }
        
        ArsAffinity.LOGGER.info("SWARM: Found {} valid summons out of {} total entities", validSummons.size(), allEntities.size());
        return validSummons;
    }

    private void commandSummonsToAttack(List<Entity> summons, LivingEntity target, ServerPlayer player) {
        ArsAffinity.LOGGER.info("SWARM: Commanding {} summons to attack {}", summons.size(), target.getName().getString());
        
        for (Entity entity : summons) {
            ArsAffinity.LOGGER.info("SWARM: Processing summon {} (type: {}, class: {})", 
                entity.getName().getString(), entity.getType().toString(), entity.getClass().getSimpleName());
            
            if (entity instanceof Mob mob && entity instanceof ISummon summon) {
                ArsAffinity.LOGGER.info("SWARM: Summon {} is valid Mob and ISummon", entity.getName().getString());
                
                if (summon.getOwnerUUID().equals(player.getUUID())) {
                    ArsAffinity.LOGGER.info("SWARM: Summon {} belongs to player, setting target and effects", entity.getName().getString());
                    
                    mob.setTarget(target);
                    mob.setAggressive(true);
                    mob.addEffect(new MobEffectInstance(ModPotions.SWARMING_EFFECT, 200, 0, false, true, true));
                    
                    ArsAffinity.LOGGER.info("SWARM: Successfully commanded summon {} to attack {} (target set: {}, aggressive: {})", 
                        entity.getName().getString(), 
                        target.getName().getString(),
                        mob.getTarget() != null ? mob.getTarget().getName().getString() : "null",
                        mob.isAggressive());
                } else {
                    ArsAffinity.LOGGER.warn("SWARM: Summon {} has different owner UUID: {} vs player: {}", 
                        entity.getName().getString(), summon.getOwnerUUID(), player.getUUID());
                }
            } else {
                ArsAffinity.LOGGER.warn("SWARM: Entity {} is not a valid summon (Mob: {}, ISummon: {})", 
                    entity.getName().getString(), 
                    entity instanceof Mob, 
                    entity instanceof ISummon);
            }
        }
    }

    private boolean isPlayerOnCooldown(Player player) {
        DeferredHolder<MobEffect, SwarmCooldownEffect> effect = ModPotions.SWARM_COOLDOWN_EFFECT;
        return effect != null && player.hasEffect(effect);
    }

    private void applyCooldown(Player player, int cooldownTicks) {
        DeferredHolder<MobEffect, SwarmCooldownEffect> effect = ModPotions.SWARM_COOLDOWN_EFFECT;
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, cooldownTicks, 0, false, true, true));
        }
    }

    private void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, IManaCap manaCap) {
        manaCap.removeMana(perk.manaCost);
    }

    private void spawnParticles(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double x = player.getX() + (Math.random() - 0.5) * 4;
                double y = player.getY() + Math.random() * 2;
                double z = player.getZ() + (Math.random() - 0.5) * 4;
                serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0, 0, 0.05);
            }
        }
    }

    private void playSounds(ServerPlayer player) {
        var pos = player.position();
        player.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.RAID_HORN, SoundSource.PLAYERS, 0.7f, 0.8f);
    }
}