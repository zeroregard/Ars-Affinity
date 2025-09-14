package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModSounds;
import com.github.ars_affinity.perk.AffinityPerk;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class LightningDashHelper extends AbstractDashAbility {
	private static final LightningDashHelper INSTANCE = new LightningDashHelper();
	private static final int MAX_VISUAL_TELEPORTS = 4;
	private static final int TELEPORT_TICKS = 4; // 4 ticks out of 20 for teleporting
	private static final int BUILDUP_TICKS = 8; // 8 ticks for build up
	private static final int RECOVERY_TICKS = 8; // 8 ticks for recovery

	public static void triggerAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
		INSTANCE.executeAbility(player, perk);
	}

	@Override
	protected Holder<MobEffect> getCooldownEffect() {
		return ModPotions.LIGHTNING_DASH_COOLDOWN_EFFECT;
	}

	@Override
	protected void onHitTarget(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		// This will be handled by the teleportation mechanics instead
	}

	@Override
	public void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
		// Override to implement custom lightning dash mechanics
		IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
		if (manaCap == null) {
			return;
		}

		double currentMana = manaCap.getCurrentMana();
		double requiredMana = getRequiredManaCost(perk, manaCap);
		if (currentMana < requiredMana) {
			return;
		}

		if (isPlayerOnCooldown(player)) {
			return;
		}

		applyCooldown(player, perk.cooldown);

		float dashLength = getDashLength(perk);
		float dashDuration = getDashDuration(perk);

		// Find targets in the dash path
		List<LivingEntity> targets = findTargetsInDashPath(player, dashLength);
		
		// Limit to maximum 4 targets for visual teleportation
		List<LivingEntity> visualTargets = targets.stream()
			.limit(MAX_VISUAL_TELEPORTS)
			.toList();

		// Execute lightning dash with teleportation
		executeLightningDash(player, dashLength, dashDuration, visualTargets, targets);
		spawnParticles(player, dashLength);
		playSounds(player);
		consumeMana(player, perk, manaCap);
	}

	private List<LivingEntity> findTargetsInDashPath(ServerPlayer player, float dashLength) {
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

		List<LivingEntity> targets = new ArrayList<>();
		for (Entity entity : candidates) {
			if (entity instanceof LivingEntity livingEntity) {
				if (isEntityWithinCapsule(livingEntity, startPos, endPos, radius)) {
					targets.add(livingEntity);
				}
			}
		}

		// Sort by distance from player to ensure we teleport through them in order
		targets.sort((a, b) -> {
			double distA = a.getBoundingBox().getCenter().distanceTo(startPos);
			double distB = b.getBoundingBox().getCenter().distanceTo(startPos);
			return Double.compare(distA, distB);
		});

		return targets;
	}

	private void executeLightningDash(ServerPlayer player, float dashLength, float dashDuration, 
									 List<LivingEntity> visualTargets, List<LivingEntity> allTargets) {
		Vec3 lookDirection = player.getLookAngle();
		
		// Calculate timing
		int totalTicks = (int)(dashDuration * 20);
		int teleportStartTick = BUILDUP_TICKS;
		int teleportEndTick = teleportStartTick + TELEPORT_TICKS;
		
		// Build up phase - prepare for teleportation
		for (int i = 0; i < BUILDUP_TICKS; i++) {
			scheduleTask(player, i, () -> {
				if (!player.isAlive()) return;
				// Spawn buildup particles
				spawnBuildupParticles(player);
			});
		}

		// Teleportation phase - go through targets
		if (!visualTargets.isEmpty()) {
			int ticksPerTarget = TELEPORT_TICKS / visualTargets.size();
			for (int i = 0; i < visualTargets.size(); i++) {
				LivingEntity target = visualTargets.get(i);
				int teleportTick = teleportStartTick + (i * ticksPerTarget);
				
				scheduleTask(player, teleportTick, () -> {
					if (!player.isAlive() || !target.isAlive()) return;
					
					// Teleport to target position
					Vec3 targetPos = target.getBoundingBox().getCenter();
					player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
					
					// Damage the target
					target.hurt(player.damageSources().magic(), 6.0f);
					
					// Spawn lightning particles
					spawnLightningParticles(player, target);
				});
			}
		}

		// Recovery phase - final movement
		for (int i = teleportEndTick; i < totalTicks; i++) {
			scheduleTask(player, i, () -> {
				if (!player.isAlive()) return;
				// Continue forward movement
				Vec3 currentVel = player.getDeltaMovement();
				if (currentVel.lengthSqr() < 0.01) {
					Vec3 dashVelocity = lookDirection.scale(dashLength / dashDuration * 0.3);
					player.setDeltaMovement(dashVelocity);
					player.hurtMarked = true;
				}
			});
		}

		// Damage all targets (not just visual ones)
		for (LivingEntity target : allTargets) {
			if (target != player) {
				scheduleTask(player, teleportStartTick, () -> {
					if (!player.isAlive() || !target.isAlive()) return;
					target.hurt(player.damageSources().magic(), 6.0f);
				});
			}
		}

		// Stop movement at the end
		scheduleTask(player, totalTicks, () -> {
			if (player.isAlive()) {
				Vec3 currentVel = player.getDeltaMovement();
				Vec3 horizontalVel = new Vec3(currentVel.x, 0, currentVel.z);
				if (horizontalVel.lengthSqr() > 0.01) {
					player.setDeltaMovement(horizontalVel.scale(0.1));
				}
			}
		});
	}

	private void spawnBuildupParticles(ServerPlayer player) {
		if (!(player.level() instanceof ServerLevel serverLevel)) return;
		Vec3 pos = player.position();
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, pos.x, pos.y + 1, pos.z, 5, 0.5, 0.5, 0.5, 0.1);
	}

	private void spawnLightningParticles(ServerPlayer player, LivingEntity target) {
		if (!(player.level() instanceof ServerLevel serverLevel)) return;
		Vec3 targetPos = target.getBoundingBox().getCenter();
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, targetPos.x, targetPos.y, targetPos.z, 10, 1.0, 1.0, 1.0, 0.2);
		serverLevel.sendParticles(ParticleTypes.CRIT, targetPos.x, targetPos.y, targetPos.z, 5, 0.5, 0.5, 0.5, 0.1);
	}

	@Override
	protected void spawnTrailParticle(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 1, 0.1, 0.1, 0.1, 0.05);
	}

	@Override
	protected void spawnBurstParticles(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 15, 1.0, 1.0, 1.0, 0.2);
		level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 1, 0, 0, 0, 0.1);
	}

	@Override
	protected void playSounds(ServerPlayer player) {
		var pos = player.position();
		player.level().playSound(null, pos.x, pos.y, pos.z, ModSounds.DASH.get(), SoundSource.PLAYERS, 0.7f, 2.0f); // Higher pitch for lightning
	}

	// Helper method to check if entity is within capsule (copied from AbstractDashAbility)
	private boolean isEntityWithinCapsule(LivingEntity entity, Vec3 start, Vec3 end, double radius) {
		Vec3 center = entity.getBoundingBox().getCenter();
		double effectiveRadius = radius + (entity.getBbWidth() * 0.5);
		double distance = distancePointToSegment(center, start, end);
		return distance <= effectiveRadius;
	}

	// Helper method for distance calculation (copied from AbstractDashAbility)
	private double distancePointToSegment(Vec3 point, Vec3 a, Vec3 b) {
		Vec3 ab = b.subtract(a);
		Vec3 ap = point.subtract(a);
		double abLenSqr = ab.lengthSqr();
		if (abLenSqr <= 1.0E-7) {
			return point.distanceTo(a);
		}
		double t = ap.dot(ab) / abLenSqr;
		t = net.minecraft.util.Mth.clamp(t, 0.0, 1.0);
		Vec3 closest = a.add(ab.scale(t));
		return point.distanceTo(closest);
	}
}