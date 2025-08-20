package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.perk.AffinityPerk;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import net.minecraft.core.Holder;

import java.util.List;

public abstract class AbstractDashAbility {
	public final void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
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

		performDash(player, dashLength, dashDuration, getDashSpeedMultiplier());
		targetEntitiesInDashPath(player, dashLength, (living, idx, start, end, dir) -> onHitTarget(player, living, start, end, dir, idx));
		spawnParticles(player, dashLength);
		playSounds(player);
		consumeMana(player, perk, manaCap);
	}

	protected double getRequiredManaCost(AffinityPerk.ActiveAbilityPerk perk, IManaCap manaCap) {
		return perk.manaCost; // Match Air Dash semantics (flat number)
	}

	protected float getDashLength(AffinityPerk.ActiveAbilityPerk perk) {
		return perk.dashLength;
	}

	protected float getDashDuration(AffinityPerk.ActiveAbilityPerk perk) {
		return perk.dashDuration;
	}

	protected double getDashSpeedMultiplier() {
		return 1.0;
	}

	protected boolean isPlayerOnCooldown(Player player) {
		Holder<MobEffect> effect = getCooldownEffect();
		return effect != null && player.hasEffect(effect);
	}

	protected void applyCooldown(Player player, int cooldownTicks) {
		Holder<MobEffect> effect = getCooldownEffect();
		if (effect != null) {
			player.addEffect(new MobEffectInstance(effect, cooldownTicks, 0, false, true, true));
		}
	}

	private void performDash(ServerPlayer player, float dashLength, float dashDuration, double speedMultiplier) {
		Vec3 lookDirection = player.getLookAngle();
		double dashSpeed = (dashLength / dashDuration) * speedMultiplier;
		Vec3 dashVelocity = lookDirection.scale(dashSpeed);
		player.setDeltaMovement(dashVelocity);
		player.hurtMarked = true;
		var server = player.level().getServer();
		if (server != null) {
			server.tell(new net.minecraft.server.TickTask((int)(server.getTickCount() + (dashDuration * 20)), () -> {
				if (player.isAlive()) {
					Vec3 currentVel = player.getDeltaMovement();
					Vec3 horizontalVel = new Vec3(currentVel.x, currentVel.y, currentVel.z);
					if (horizontalVel.lengthSqr() > 0.01) {
						player.setDeltaMovement(horizontalVel.scale(0.1));
					}
				}
			}));
		}
	}

	@FunctionalInterface
	private interface TargetConsumer {
		void accept(LivingEntity target, int index, Vec3 start, Vec3 end, Vec3 dashDir);
	}

	private void targetEntitiesInDashPath(ServerPlayer player, float dashLength, TargetConsumer consumer) {
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

		int index = 0;
		for (Entity entity : candidates) {
			if (entity instanceof LivingEntity livingEntity) {
				if (isEntityWithinCapsule(livingEntity, startPos, endPos, radius)) {
					consumer.accept(livingEntity, index, startPos, endPos, lookDirection);
					index++;
				}
			}
		}
	}

	private boolean isEntityWithinCapsule(LivingEntity entity, Vec3 start, Vec3 end, double radius) {
		Vec3 center = entity.getBoundingBox().getCenter();
		double effectiveRadius = radius + (entity.getBbWidth() * 0.5);
		double distance = distancePointToSegment(center, start, end);
		return distance <= effectiveRadius;
	}

	private double distancePointToSegment(Vec3 point, Vec3 a, Vec3 b) {
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

	protected void scheduleTask(ServerPlayer player, int delayTicks, Runnable task) {
		var server = player.level().getServer();
		if (server == null) return;
		server.tell(new net.minecraft.server.TickTask(server.getTickCount() + delayTicks, task));
	}

	protected Vec3 computeSideOffset(Vec3 startPos, Vec3 endPos, Vec3 dashDir, LivingEntity target, int index) {
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
		return base.add(lateral).add(dashDir.normalize().scale(0.25));
	}

	protected void spawnParticles(ServerPlayer player, float dashLength) {
		if (!(player.level() instanceof ServerLevel serverLevel)) return;
		Vec3 playerPos = player.position();
		Vec3 lookDirection = player.getLookAngle();
		int count = getTrailParticleCount();
		for (int i = 0; i < count; i++) {
			double progress = (double)i / (double)count;
			Vec3 p = playerPos.add(lookDirection.scale(dashLength * progress));
			spawnTrailParticle(serverLevel, p.x, p.y, p.z);
		}
		spawnBurstParticles(serverLevel, playerPos.x, playerPos.y, playerPos.z);
	}

	protected int getTrailParticleCount() { return 30; }

	protected abstract void spawnTrailParticle(ServerLevel level, double x, double y, double z);

	protected abstract void spawnBurstParticles(ServerLevel level, double x, double y, double z);

	protected abstract void playSounds(ServerPlayer player);

	protected abstract Holder<MobEffect> getCooldownEffect();

	protected abstract void onHitTarget(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index);

	protected void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, IManaCap manaCap) {
		manaCap.removeMana((int)perk.manaCost);
	}
}