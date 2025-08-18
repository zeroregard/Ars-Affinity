package com.github.ars_affinity.common.ability.field;

import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public class CurseFieldHelper extends AbstractFieldAbility {

	public static final int DEFAULT_HALf_EXTENT = 1; // 3x3x3
	public static final double DEFAULT_MANA_PERCENT_PER_TICK = 0.001; // 0.1%
	public static final int DEFAULT_COOLDOWN_TICKS = 20 * 5; // 5 seconds

	public CurseFieldHelper(ServerPlayer player) {
		super(player, DEFAULT_HALf_EXTENT, DEFAULT_MANA_PERCENT_PER_TICK, DEFAULT_COOLDOWN_TICKS);
	}

	@Override
	public void onTick() {
		for (LivingEntity e : getLivingEntitiesInField()) {
			if (e == player) continue;
			// Apply silenced debuff
			e.addEffect(new MobEffectInstance(ModPotions.SILENCED_EFFECT, 20, 0, false, true, true));
			// Damage over time and lifesteal/mana steal placeholders
			e.hurt(player.damageSources().magic(), 1.0f);
			player.heal(0.5f);
			var manaCap = com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry.getMana(player);
			if (manaCap != null) {
				manaCap.addMana(1);
			}
		}
	}

	@Override
	public void onRelease() {
		player.addEffect(new MobEffectInstance(ModPotions.CURSE_FIELD_COOLDOWN_EFFECT, cooldownTicks, 0, false, true, true));
	}

	@Override
	protected void renderParticles() {
		if (!(player.level() instanceof ServerLevel sl)) return;
		var box = getFieldAABB();
		for (int i = 0; i < 10; i++) {
			double x = box.minX + player.getRandom().nextDouble() * (box.maxX - box.minX);
			double y = box.minY + player.getRandom().nextDouble() * (box.maxY - box.minY);
			double z = box.minZ + player.getRandom().nextDouble() * (box.maxZ - box.minZ);
			sl.sendParticles(ParticleTypes.SMOKE, x, y, z, 1, 0, 0, 0, 0.0);
			sl.sendParticles(ParticleTypes.SOUL, x, y, z, 1, 0, 0, 0, 0.0);
		}
	}
}

