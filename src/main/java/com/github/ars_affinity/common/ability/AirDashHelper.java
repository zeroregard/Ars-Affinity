package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModSounds;
import com.github.ars_affinity.perk.AffinityPerk;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.phys.Vec3;

public class AirDashHelper extends AbstractDashAbility {
	private static final AirDashHelper INSTANCE = new AirDashHelper();

	public static void triggerAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
		INSTANCE.executeAbility(player, perk);
	}

	@Override
	protected Holder<MobEffect> getCooldownEffect() {
		return ModPotions.AIR_DASH_COOLDOWN_EFFECT;
	}

	@Override
	protected void onHitTarget(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		int delay = Math.min(6, index) * 2;
		scheduleTask(player, delay, () -> {
			if (!player.isAlive() || !target.isAlive()) return;
			var level = player.level();
			if (level.isClientSide()) return;
			Vec3 from = computeSideOffset(startPos, endPos, dashDir, target, index);
			Vec3 to = target.getEyePosition();
			Vec3 dir = to.subtract(from).normalize();
			WindCharge wind = new WindCharge(level, from.x, from.y, from.z, dir);
			wind.setOwner(player);
			wind.setDeltaMovement(dir.scale(3.0));
			level.addFreshEntity(wind);
		});
	}

	@Override
	protected void spawnTrailParticle(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0, 0, 0.05);
	}

	@Override
	protected void spawnBurstParticles(ServerLevel level, double x, double y, double z) {
		level.sendParticles(ParticleTypes.FIREWORK, x, y, z, 1, 0, 0, 0, 0.1);
	}

	@Override
	protected void playSounds(ServerPlayer player) {
		var pos = player.position();
		player.level().playSound(null, pos.x, pos.y, pos.z, ModSounds.DASH.get(), SoundSource.PLAYERS, 0.7f, 1.3f);
	}
}