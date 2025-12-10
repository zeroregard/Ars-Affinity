package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import com.github.ars_affinity.helper.MathUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;

public class SwapAbilityHelper {
    
    private static final double MAX_DISTANCE = 100.0;
    
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        ArsAffinity.LOGGER.debug("SWAP ABILITY: Starting execution for player {} with perk: manaCost={}, cooldown={}", 
            player.getName().getString(), perk.manaCost, perk.cooldown);
        
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: Player {} has no mana capability", player.getName().getString());
            return;
        }
        
        double currentMana = manaCap.getCurrentMana();
        double requiredMana = perk.manaCost;
        
        if (currentMana < requiredMana) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: Player {} doesn't have enough mana. Required: {}, Current: {}", 
                player.getName().getString(), requiredMana, currentMana);
            return;
        }
        
        if (isPlayerOnCooldown(player)) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: Player {} is on cooldown", player.getName().getString());
            return;
        }
        
        ArsAffinity.LOGGER.debug("SWAP ABILITY: Attempting to find entity with range {}", MAX_DISTANCE);
        var entityHitResult = MathUtils.getLookedAtEntity(player, MAX_DISTANCE);
        ArsAffinity.LOGGER.debug("SWAP ABILITY: getLookedAtEntity returned: {}", entityHitResult);
        if (entityHitResult == null) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: No valid target found for player {}", player.getName().getString());
            
            return;
        }
        
        if (!(entityHitResult.getEntity() instanceof LivingEntity targetEntity)) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: Target is not a living entity: {}", entityHitResult.getEntity().getName().getString());
            return;
        }
        
        if (targetEntity == player) {
            ArsAffinity.LOGGER.debug("SWAP ABILITY: Player {} cannot swap with themselves", player.getName().getString());
            return;
        }
        

        
        applyCooldown(player, perk.cooldown);
        performSwap(player, targetEntity);
        consumeMana(player, perk);
        
        ArsAffinity.LOGGER.debug("SWAP ABILITY: Successfully swapped positions between player {} and entity {}", 
            player.getName().getString(), targetEntity.getName().getString());
    }
    
    private static void performSwap(ServerPlayer player, LivingEntity target) {
        Vec3 playerPos = player.position();
        Vec3 targetPos = target.position();
        
        float playerYRot = player.getYRot();
        float playerXRot = player.getXRot();
        float targetYRot = target.getYRot();
        float targetXRot = target.getXRot();
        
        // Spawn ender particles at original positions before teleporting
        spawnEnderParticles(player.level(), playerPos);
        spawnEnderParticles(player.level(), targetPos);
        
        player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
        target.teleportTo(playerPos.x, playerPos.y, playerPos.z);
        
        player.setYRot(targetYRot);
        player.setXRot(targetXRot);
        target.setYRot(playerYRot);
        target.setXRot(playerXRot);
        
        // Play enderman teleport sound at both locations
        player.level().playSound(null, playerPos.x, playerPos.y, playerPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        player.level().playSound(null, targetPos.x, targetPos.y, targetPos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.0f);
        
        // Spawn ender particles at new positions after teleporting
        spawnEnderParticles(player.level(), player.position());
        spawnEnderParticles(player.level(), target.position());
    }
    
    private static void spawnEnderParticles(net.minecraft.world.level.Level level, Vec3 pos) {
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double x = pos.x + (level.random.nextDouble() - 0.5) * 0.5;
                double y = pos.y + level.random.nextDouble() * 1.0;
                double z = pos.z + (level.random.nextDouble() - 0.5) * 0.5;
                serverLevel.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }
    
    private static void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap != null) {
            int manaToConsume = (int)perk.manaCost;
            manaCap.removeMana(manaToConsume);
        }
    }
    
    private static boolean isPlayerOnCooldown(ServerPlayer player) {
        return player.hasEffect(ModPotions.SWAP_COOLDOWN_EFFECT);
    }
    
    private static void applyCooldown(ServerPlayer player, int cooldownTicks) {
        player.addEffect(new MobEffectInstance(ModPotions.SWAP_COOLDOWN_EFFECT, cooldownTicks, 0, false, true, true));
    }
} 