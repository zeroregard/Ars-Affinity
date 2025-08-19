package com.github.ars_affinity.common.ability.field;

import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class SanctuaryHelper extends AbstractFieldAbility {

	public static final int DEFAULT_HALF_EXTENT_X = 1;
	public static final int DEFAULT_HALF_EXTENT_Y = 1;
	public static final int DEFAULT_HALF_EXTENT_Z = 1;
	public static final double DEFAULT_MANA_COST_PER_TICK = 1.0; // flat mana per tick
	public static final int DEFAULT_COOLDOWN_TICKS = 20 * 5; // 5 seconds

	public SanctuaryHelper(ServerPlayer player) {
		super(player, DEFAULT_HALF_EXTENT_X, DEFAULT_HALF_EXTENT_Y, DEFAULT_HALF_EXTENT_Z, DEFAULT_MANA_COST_PER_TICK, DEFAULT_COOLDOWN_TICKS);
	}

	public SanctuaryHelper(ServerPlayer player, double manaCostPerTick, int cooldownTicks) {
		super(player, DEFAULT_HALF_EXTENT_X, DEFAULT_HALF_EXTENT_Y, DEFAULT_HALF_EXTENT_Z, manaCostPerTick, cooldownTicks);
	}

	public static void toggleOrStart(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
		if (player.hasEffect(ModPotions.SANCTUARY_COOLDOWN_EFFECT)) return;
		ActiveFieldRegistry.toggleOrStart(player, () -> new SanctuaryHelper(player, perk.manaCost, perk.cooldown));
	}

	@Override
	public void onTick() {
		for (LivingEntity e : getLivingEntitiesInField()) {
			// Apply sanctuary every tick so it persists
			e.addEffect(new MobEffectInstance(ModPotions.SANCTUARY_EFFECT, 10, 0, false, true, true));
		}
	}

	@Override
	public void onRelease() {
		player.addEffect(new MobEffectInstance(ModPotions.SANCTUARY_COOLDOWN_EFFECT, cooldownTicks, 0, false, true, true));
	}

	@Override
	protected void renderParticles() {
		if (!(player.level() instanceof ServerLevel sl)) return;
		var box = getFieldAABB();
		for (int i = 0; i < 12; i++) {
			double x = box.minX + player.getRandom().nextDouble() * (box.maxX - box.minX);
			double y = box.minY + player.getRandom().nextDouble() * (box.maxY - box.minY);
			double z = box.minZ + player.getRandom().nextDouble() * (box.maxZ - box.minZ);
			sl.sendParticles(ParticleTypes.GLOW, x, y, z, 1, 0, 0, 0, 0.0);
			sl.sendParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0, 0, 0, 0.0);
		}
	}
}

