package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModSounds;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

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
		return 0.75;
	}

	@Override
	protected void onHitTarget(ServerPlayer player, LivingEntity target, Vec3 startPos, Vec3 endPos, Vec3 dashDir, int index) {
		if (!player.isAlive() || !target.isAlive()) return;
		var level = player.level();
		if (level.isClientSide()) return;
		ArsAffinity.LOGGER.info("FIRE DASH: igniting target immediately name={}", target.getName().getString());
		igniteTargetFallback(player, target);
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
	protected void spawnParticles(ServerPlayer player, float dashLength) {
		int count = getTrailParticleCount();
		for (int i = 0; i < count; i++) {
			int delay = i;
			ArsAffinity.LOGGER.info("FIRE DASH: scheduled particle tick={} delay={}t", i, delay);
			scheduleTask(player, delay, () -> {
				if (!(player.level() instanceof ServerLevel serverLevel)) return;
				double px = player.getX();
				double py = player.getY();
				double pz = player.getZ();
				serverLevel.sendParticles(ParticleTypes.FLAME, px, py, pz, 2, 0.02, 0.02, 0.02, 0.02);
				serverLevel.sendParticles(ParticleTypes.SMOKE, px, py, pz, 1, 0.02, 0.02, 0.02, 0.01);
				BlockPos feet = BlockPos.containing(Mth.floor(px), Mth.floor(py - 0.1), Mth.floor(pz));
				placeMagicFireIfAir(serverLevel, feet);
			});
		}
	}

	@Override
	protected void playSounds(ServerPlayer player) {
		var pos = player.position();
		player.level().playSound(null, pos.x, pos.y, pos.z, ModSounds.DASH.get(), SoundSource.PLAYERS, 0.7f, 0.9f);
		for (int i = 0; i < 12; i++) {
			int delay = i;
			ArsAffinity.LOGGER.info("FIRE DASH: schedule foot fire tick={} delay={}t", i, delay);
			scheduleTask(player, delay, () -> {
				if (!(player.level() instanceof ServerLevel serverLevel)) return;
				BlockPos feet = BlockPos.containing(Mth.floor(player.getX()), Mth.floor(player.getY() - 0.1), Mth.floor(player.getZ()));
				ArsAffinity.LOGGER.info("FIRE DASH: attempt place fire at {} {} {}", feet.getX(), feet.getY(), feet.getZ());
				placeMagicFireIfAir(serverLevel, feet);
			});
		}
	}

	private void placeMagicFireIfAir(ServerLevel level, BlockPos pos) {
		var state = level.getBlockState(pos);
		ArsAffinity.LOGGER.info("FIRE DASH: block at {} {} {} is {}", pos.getX(), pos.getY(), pos.getZ(), state);
		if (state.isAir()) {
			boolean placed = level.setBlock(pos, BlockRegistry.MAGIC_FIRE.get().defaultBlockState(), 3);
			ArsAffinity.LOGGER.info("FIRE DASH: setBlock result={} at {} {} {}", placed, pos.getX(), pos.getY(), pos.getZ());
			if (!placed) {
				level.setBlockAndUpdate(pos, BlockRegistry.MAGIC_FIRE.get().defaultBlockState());
				ArsAffinity.LOGGER.info("FIRE DASH: fallback setBlockAndUpdate at {} {} {}", pos.getX(), pos.getY(), pos.getZ());
			}
			return;
		}
		BlockPos above = pos.above();
		var stateAbove = level.getBlockState(above);
		ArsAffinity.LOGGER.info("FIRE DASH: block above at {} {} {} is {}", above.getX(), above.getY(), above.getZ(), stateAbove);
		if (stateAbove.isAir()) {
			boolean placedAbove = level.setBlock(above, BlockRegistry.MAGIC_FIRE.get().defaultBlockState(), 3);
			ArsAffinity.LOGGER.info("FIRE DASH: setBlock result={} at above {} {} {}", placedAbove, above.getX(), above.getY(), above.getZ());
			if (!placedAbove) {
				level.setBlockAndUpdate(above, BlockRegistry.MAGIC_FIRE.get().defaultBlockState());
				ArsAffinity.LOGGER.info("FIRE DASH: fallback setBlockAndUpdate at above {} {} {}", above.getX(), above.getY(), above.getZ());
			}
		}
	}
}