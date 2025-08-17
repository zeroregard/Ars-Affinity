package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import com.github.ars_affinity.registry.ModSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.util.Mth;

import java.util.List;


public class AirDashHelper {
    
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        ArsAffinity.LOGGER.info("AIR DASH: Starting execution for player {} with perk: manaCost={}, cooldown={}, dashLength={}, dashDuration={}", 
            "air dash was triggered");
        
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            ArsAffinity.LOGGER.info("AIR DASH: Player {} has no mana capability", player.getName().getString());
            return;
        }
        
        double currentMana = manaCap.getCurrentMana();
        double requiredMana = perk.manaCost;
        
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
        player.addEffect(new MobEffectInstance(ModPotions.AIR_DASH_COOLDOWN_EFFECT, cooldownTicks, 0, false, true, true));
    }
    
    private static void performDash(ServerPlayer player, float dashLength, float dashDuration) {
        Vec3 lookDirection = player.getLookAngle();
        double dashSpeed = dashLength / dashDuration;
        Vec3 dashVelocity = lookDirection.scale(dashSpeed);
        player.setDeltaMovement(dashVelocity);
        player.hurtMarked = true;
        player.level().getServer().tell(new net.minecraft.server.TickTask(
            (int) (player.level().getServer().getTickCount() + (dashDuration * 20)), 
            () -> {
                if (player.isAlive()) {
                    Vec3 currentVel = player.getDeltaMovement();
                    Vec3 horizontalVel = new Vec3(currentVel.x, currentVel.y, currentVel.z);
                    if (horizontalVel.lengthSqr() > 0.01) {
                        player.setDeltaMovement(horizontalVel.scale(0.1));
                    }
                }
            }
        ));
    }
    
    private static void damageEntitiesInDashPath(ServerPlayer player, float dashLength) {
        Vec3 startPos = player.position();
        Vec3 lookDirection = player.getLookAngle();
        Vec3 endPos = startPos.add(lookDirection.scale(dashLength));
        double radius = 3.0;

        AABB queryBox = new AABB(
            Math.min(startPos.x, endPos.x), Math.min(startPos.y, endPos.y), Math.min(startPos.z, endPos.z),
            Math.max(startPos.x, endPos.x), Math.max(startPos.y, endPos.y), Math.max(startPos.z, endPos.z)
        ).inflate(radius + 0.5);

        List<Entity> candidates = player.level().getEntities(player, queryBox, entity ->
            entity instanceof LivingEntity && entity != player && !entity.isAlliedTo(player));

        int hits = 0;
		int index = 0;
		for (Entity entity : candidates) {
			if (entity instanceof LivingEntity livingEntity) {
				if (isEntityWithinCapsule(livingEntity, startPos, endPos, radius)) {
					scheduleWindCharge(player, livingEntity, startPos, endPos, lookDirection, index);
					hits++;
					index++;
				}
			}
		}
 
		ArsAffinity.LOGGER.info("AIR DASH: Found {} entities in dash path ({} within radius)", candidates.size(), hits);
    }

    private static boolean isEntityWithinCapsule(LivingEntity entity, Vec3 start, Vec3 end, double radius) {
        Vec3 center = entity.getBoundingBox().getCenter();
        double effectiveRadius = radius + (entity.getBbWidth() * 0.5);
        double distance = distancePointToSegment(center, start, end);
        return distance <= effectiveRadius;
    }

    private static double distancePointToSegment(Vec3 point, Vec3 a, Vec3 b) {
        Vec3 ab = b.subtract(a);
        Vec3 ap = point.subtract(a);
        double abLenSqr = ab.lengthSqr();
        if (abLenSqr <= 1.0E-7) {
            return point.distanceTo(a);
        }
        double t = ap.dot(ab) / abLenSqr;
        t = Mth.clamp(t, 0.0, 1.0);
        Vec3 closest = a.add(ab.scale(t));
        return point.distanceTo(closest);
    }
    
    private static void scheduleWindCharge(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		var server = player.level().getServer();
		if (server == null) return;
		int delay = Math.min(6, index) * 2;
		server.tell(new net.minecraft.server.TickTask(server.getTickCount() + delay, () -> {
			if (!player.isAlive() || !target.isAlive()) return;
			spawnWindCharge(player, target, startPos, endPos, dashDir, index);
		}));
	}

	private static void spawnWindCharge(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		var level = player.level();
		if (level.isClientSide()) return;
		Vec3 segment = endPos.subtract(startPos);
		double lenSqr = segment.lengthSqr();
		double t = 0.0;
		if (lenSqr > 1.0E-7) {
			t = Mth.clamp(target.getBoundingBox().getCenter().subtract(startPos).dot(segment) / lenSqr, 0.0, 1.0);
		}
		Vec3 base = startPos.add(segment.scale(t));
		Vec3 lateral = dashDir.cross(new Vec3(0, 1, 0));
		if (lateral.lengthSqr() < 1.0E-6) {
			lateral = dashDir.cross(new Vec3(1, 0, 0));
		}
		lateral = lateral.normalize().scale(((index & 1) == 0 ? 1.0 : -1.0) * 0.5);
		Vec3 from = base.add(lateral).add(dashDir.normalize().scale(0.25));
		Vec3 to = target.getEyePosition();
		Vec3 dir = to.subtract(from).normalize();
		WindCharge wind = new WindCharge(level, from.x, from.y, from.z, dir);
		wind.setOwner(player);
		wind.setDeltaMovement(dir.scale(3.0));
		level.addFreshEntity(wind);
	}
    
    private static void spawnParticleEffects(ServerPlayer player) {
        Vec3 playerPos = player.position();
        
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            Vec3 lookDirection = player.getLookAngle();
            float dashLength = 10.0f;
            
            for (int i = 0; i < 30; i++) {
                double progress = (double) i / 30.0;
                Vec3 particlePos = playerPos.add(lookDirection.scale(dashLength * progress));
                
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
        player.level().playSound(
            null,
            playerPos.x,
            playerPos.y,
            playerPos.z,
            ModSounds.DASH.get(),
            SoundSource.PLAYERS,
            0.7f,
            1.3f
        );
    }
    
    private static void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap != null) {
            int manaToConsume = (int)perk.manaCost;
            manaCap.removeMana(manaToConsume);
            
        }
    }
}