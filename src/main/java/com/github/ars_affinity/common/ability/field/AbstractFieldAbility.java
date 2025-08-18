package com.github.ars_affinity.common.ability.field;

import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public abstract class AbstractFieldAbility {
	protected final ServerPlayer player;
	protected final int halfExtent; // radius in blocks in each direction (1 => 3x3x3)
	protected final double manaPercentPerTick; // 0.001 == 0.1%
	protected final int cooldownTicks;

	protected AbstractFieldAbility(ServerPlayer player, int halfExtent, double manaPercentPerTick, int cooldownTicks) {
		this.player = player;
		this.halfExtent = halfExtent;
		this.manaPercentPerTick = manaPercentPerTick;
		this.cooldownTicks = cooldownTicks;
	}

	public ServerPlayer getPlayer() {
		return player;
	}

	public AABB getFieldAABB() {
		Vec3 p = player.position();
		return new AABB(
			p.x - halfExtent, p.y - halfExtent, p.z - halfExtent,
			p.x + halfExtent, p.y + halfExtent, p.z + halfExtent
		);
	}

	public boolean isEntityInsideField(LivingEntity entity) {
		return getFieldAABB().intersects(entity.getBoundingBox());
	}

	protected boolean tryConsumeManaTick() {
		IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
		if (manaCap == null) return false;
		double maxMana = manaCap.getMaxMana();
		double cost = Math.max(1.0, Math.floor(maxMana * manaPercentPerTick));
		if (manaCap.getCurrentMana() < cost) return false;
		manaCap.removeMana((int) cost);
		return true;
	}

	public final boolean tick() {
		// Returns false if ability should stop (e.g., cannot pay cost)
		if (!(player.level() instanceof ServerLevel)) return true;
		if (!tryConsumeManaTick()) return false;
		onTick();
		renderParticles();
		return true;
	}

	public abstract void onTick();

	public abstract void onRelease();

	protected abstract void renderParticles();

	protected List<LivingEntity> getLivingEntitiesInField() {
		AABB box = getFieldAABB();
		return player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e.isAlive());
	}
}

