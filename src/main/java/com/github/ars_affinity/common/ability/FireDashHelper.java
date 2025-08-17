package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModSounds;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public class FireDashHelper extends AbstractDashAbility {
	private static final FireDashHelper INSTANCE = new FireDashHelper();

	public static void triggerAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
		INSTANCE.executeAbility(player, perk);
	}

	@Override
	protected Holder<MobEffect> getCooldownEffect() {
		return ModPotions.FIRE_DASH_COOLDOWN_EFFECT;
	}

	@Override
	protected double getDashSpeedMultiplier() {
		return 0.75; // Slower than air dash
	}

	@Override
	protected void onHitTarget(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		int delay = Math.min(6, index) * 2;
		scheduleTask(player, delay, () -> {
			if (!player.isAlive() || !target.isAlive()) return;
			var level = player.level();
			if (level.isClientSide()) return;
			try {
				Vec3 from = computeSideOffset(startPos, endPos, dashDir, target, index);
				Vec3 to = target.getEyePosition();
				Vec3 dir = to.subtract(from).normalize();
				SmallFireball fireball = new SmallFireball(level, from.x, from.y, from.z, dir);
				fireball.setOwner(player);
				level.addFreshEntity(fireball);
			} catch (Throwable t) {
				igniteTargetFallback(player, target);
			}
		});
	}

	private void igniteTargetFallback(ServerPlayer player, LivingEntity target) {
		IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
		int maxMana = manaCap != null ? manaCap.getMaxMana() : 100;
		int seconds = Math.max(3, Math.min(15, maxMana / 100));
		target.setRemainingFireTicks(seconds * 20);
	}

	@Override
	protected void spawnTrailParticle(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.FLAME, x, y, z, 2, 0.02, 0.02, 0.02, 0.02);
	}

	@Override
	protected void spawnBurstParticles(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.FLAME, x, y, z, 8, 0.2, 0.2, 0.2, 0.02);
		level.sendParticles(ParticleTypes.SMOKE, x, y, z, 4, 0.2, 0.2, 0.2, 0.01);
	}

	@Override
	protected void playSounds(ServerPlayer player) {
		var pos = player.position();
		player.level().playSound(null, pos.x, pos.y, pos.z, ModSounds.DASH.get(), SoundSource.PLAYERS, 0.7f, 0.9f);
	}
}