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
import java.util.HashSet;
import java.util.Set;

// Class to represent a single link in the lightning chain
class ChainLink {
	public final LivingEntity target;
	public final Vec3 position;
	public final int chainIndex;
	public final double damage;
	
	public ChainLink(LivingEntity target, int chainIndex, double damage) {
		this.target = target;
		this.position = target.getBoundingBox().getCenter();
		this.chainIndex = chainIndex;
		this.damage = damage;
	}
}

public class LightningDashHelper extends AbstractDashAbility {
	private static final LightningDashHelper INSTANCE = new LightningDashHelper();
	private static final int MAX_CHAIN_LENGTH = 6; // Maximum number of enemies in chain
	private static final double CHAIN_RANGE = 5.0; // Maximum distance between chain targets
	private static final int TELEPORT_TICKS = 4; // 4 ticks out of 20 for teleporting
	private static final int BUILDUP_TICKS = 8; // 8 ticks for build up
	private static final int RECOVERY_TICKS = 8; // 8 ticks for recovery
	private static final double BASE_DAMAGE = 6.0;
	private static final double CHAIN_DAMAGE_REDUCTION = 0.8; // Each chain link does 80% of previous damage

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
		// Override to implement custom chain lightning mechanics
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

		// Find all potential targets in the dash path
		List<LivingEntity> allTargets = findTargetsInDashPath(player, dashLength);
		
		// Create chain lightning path
		List<ChainLink> chainPath = createChainLightningPath(player, allTargets);

		// Execute chain lightning dash
		executeChainLightningDash(player, dashLength, dashDuration, chainPath);
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

	private List<ChainLink> createChainLightningPath(ServerPlayer player, List<LivingEntity> allTargets) {
		List<ChainLink> chainPath = new ArrayList<>();
		Set<LivingEntity> usedTargets = new HashSet<>();
		
		if (allTargets.isEmpty()) {
			return chainPath;
		}

		// Find the closest target to start the chain
		Vec3 playerPos = player.position();
		LivingEntity closestTarget = allTargets.stream()
			.min((a, b) -> Double.compare(
				a.getBoundingBox().getCenter().distanceTo(playerPos),
				b.getBoundingBox().getCenter().distanceTo(playerPos)
			))
			.orElse(null);

		if (closestTarget == null) {
			return chainPath;
		}

		// Start the chain with the closest target
		ChainLink currentLink = new ChainLink(closestTarget, 0, BASE_DAMAGE);
		chainPath.add(currentLink);
		usedTargets.add(closestTarget);

		// Build the chain by finding the next closest valid target
		while (chainPath.size() < MAX_CHAIN_LENGTH) {
			ChainLink nextLink = findNextChainTarget(currentLink, allTargets, usedTargets);
			if (nextLink == null) {
				break; // No more valid targets in range
			}
			
			chainPath.add(nextLink);
			usedTargets.add(nextLink.target);
			currentLink = nextLink;
		}

		return chainPath;
	}

	private ChainLink findNextChainTarget(ChainLink currentLink, List<LivingEntity> allTargets, Set<LivingEntity> usedTargets) {
		Vec3 currentPos = currentLink.position;
		double currentDamage = currentLink.damage * CHAIN_DAMAGE_REDUCTION;
		int nextIndex = currentLink.chainIndex + 1;

		// Find the closest unused target within chain range
		LivingEntity nextTarget = allTargets.stream()
			.filter(target -> !usedTargets.contains(target))
			.filter(target -> target.getBoundingBox().getCenter().distanceTo(currentPos) <= CHAIN_RANGE)
			.min((a, b) -> Double.compare(
				a.getBoundingBox().getCenter().distanceTo(currentPos),
				b.getBoundingBox().getCenter().distanceTo(currentPos)
			))
			.orElse(null);

		if (nextTarget == null) {
			return null;
		}

		return new ChainLink(nextTarget, nextIndex, currentDamage);
	}

	private void executeChainLightningDash(ServerPlayer player, float dashLength, float dashDuration, List<ChainLink> chainPath) {
		Vec3 lookDirection = player.getLookAngle();
		
		// Calculate timing
		int totalTicks = (int)(dashDuration * 20);
		int teleportStartTick = BUILDUP_TICKS;
		int teleportEndTick = teleportStartTick + TELEPORT_TICKS;
		
		// Build up phase - prepare for chain lightning
		for (int i = 0; i < BUILDUP_TICKS; i++) {
			scheduleTask(player, i, () -> {
				if (!player.isAlive()) return;
				// Spawn buildup particles
				spawnBuildupParticles(player);
			});
		}

		// Chain lightning phase - teleport through chain targets
		if (!chainPath.isEmpty()) {
			int ticksPerTarget = Math.max(1, TELEPORT_TICKS / chainPath.size());
			final List<ChainLink> finalChainPath = chainPath; // Make effectively final
			
			for (int i = 0; i < chainPath.size(); i++) {
				final ChainLink chainLink = chainPath.get(i);
				final int finalI = i; // Make effectively final
				int teleportTick = teleportStartTick + (i * ticksPerTarget);
				
				scheduleTask(player, teleportTick, () -> {
					if (!player.isAlive() || !chainLink.target.isAlive()) return;
					
					// Teleport to target position
					Vec3 targetPos = chainLink.position;
					player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
					
					// Damage the target with scaling damage
					chainLink.target.hurt(player.damageSources().magic(), (float)chainLink.damage);
					
					// Spawn chain lightning particles
					spawnChainLightningParticles(player, chainLink);
					
					// Create lightning arc to next target (if exists)
					if (finalI < finalChainPath.size() - 1) {
						ChainLink nextLink = finalChainPath.get(finalI + 1);
						spawnLightningArc(player, chainLink, nextLink);
					}
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

	private void spawnChainLightningParticles(ServerPlayer player, ChainLink chainLink) {
		if (!(player.level() instanceof ServerLevel serverLevel)) return;
		Vec3 targetPos = chainLink.position;
		
		// Scale particle intensity based on chain position (earlier = more intense)
		int particleCount = (int)(15 * (1.0 - (chainLink.chainIndex * 0.15)));
		particleCount = Math.max(5, particleCount);
		
		serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, targetPos.x, targetPos.y, targetPos.z, particleCount, 1.0, 1.0, 1.0, 0.2);
		serverLevel.sendParticles(ParticleTypes.CRIT, targetPos.x, targetPos.y, targetPos.z, particleCount / 2, 0.5, 0.5, 0.5, 0.1);
		
		// Add explosion effect for first target
		if (chainLink.chainIndex == 0) {
			serverLevel.sendParticles(ParticleTypes.FIREWORK, targetPos.x, targetPos.y, targetPos.z, 1, 0, 0, 0, 0.1);
		}
	}

	private void spawnLightningArc(ServerPlayer player, ChainLink from, ChainLink to) {
		if (!(player.level() instanceof ServerLevel serverLevel)) return;
		
		Vec3 fromPos = from.position;
		Vec3 toPos = to.position;
		Vec3 direction = toPos.subtract(fromPos);
		double distance = direction.length();
		
		if (distance < 0.1) return; // Avoid division by zero
		
		direction = direction.normalize();
		
		// Create a jagged lightning arc by adding random offsets
		int arcPoints = Math.max(3, (int)(distance * 2));
		for (int i = 0; i < arcPoints; i++) {
			double progress = (double)i / (double)(arcPoints - 1);
			Vec3 basePos = fromPos.add(direction.scale(distance * progress));
			
			// Add random offset for jagged effect
			double offsetX = (Math.random() - 0.5) * 0.5;
			double offsetY = (Math.random() - 0.5) * 0.5;
			double offsetZ = (Math.random() - 0.5) * 0.5;
			
			Vec3 arcPos = basePos.add(offsetX, offsetY, offsetZ);
			
			// Spawn electric spark along the arc
			serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, arcPos.x, arcPos.y, arcPos.z, 1, 0.1, 0.1, 0.1, 0.05);
		}
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