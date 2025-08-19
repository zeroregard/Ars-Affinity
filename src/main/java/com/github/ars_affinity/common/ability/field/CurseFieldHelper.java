package com.github.ars_affinity.common.ability.field;

import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.EntityTypeTags;

public class CurseFieldHelper extends AbstractFieldAbility {

	public static final int DEFAULT_HALF_EXTENT_X = 3; // 6 wide (x)
	public static final int DEFAULT_HALF_EXTENT_Y = 1; // 2 tall (y)
	public static final int DEFAULT_HALF_EXTENT_Z = 3; // 6 deep (z)
	public static final double DEFAULT_MANA_COST_PER_TICK = 1.0; // flat mana per tick
	public static final int DEFAULT_COOLDOWN_TICKS = 20 * 5; // 5 seconds

	public CurseFieldHelper(ServerPlayer player) {
		super(player, DEFAULT_HALF_EXTENT_X, DEFAULT_HALF_EXTENT_Y, DEFAULT_HALF_EXTENT_Z, DEFAULT_MANA_COST_PER_TICK, DEFAULT_COOLDOWN_TICKS);
	}

	public CurseFieldHelper(ServerPlayer player, double manaCostPerTick, int cooldownTicks) {
		super(player, DEFAULT_HALF_EXTENT_X, DEFAULT_HALF_EXTENT_Y, DEFAULT_HALF_EXTENT_Z, manaCostPerTick, cooldownTicks);
	}

	public static void toggleOrStart(ServerPlayer player, com.github.ars_affinity.perk.AffinityPerk.ActiveAbilityPerk perk) {
		if (player.hasEffect(com.github.ars_affinity.registry.ModPotions.CURSE_FIELD_COOLDOWN_EFFECT)) return;
		ActiveFieldRegistry.toggleOrStart(player, () -> new CurseFieldHelper(player, perk.manaCost, perk.cooldown));
	}

	@Override
	public void onTick() {
		for (LivingEntity e : getLivingEntitiesInField()) {
			if (e == player) continue;
			if (e.getType().is(EntityTypeTags.UNDEAD)) continue;
			e.addEffect(new MobEffectInstance(ModPotions.SILENCED_EFFECT, 20, 0, false, true, true));
			e.hurt(player.damageSources().magic(), 1.0f);
			player.heal(0.5f);
			var manaCap = CapabilityRegistry.getMana(player);
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