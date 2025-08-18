package com.github.ars_affinity.common.ability.field;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveFieldRegistry {
	private static final Map<UUID, AbstractFieldAbility> ACTIVE = new ConcurrentHashMap<>();

	public static void toggleOrStart(ServerPlayer player, java.util.function.Supplier<AbstractFieldAbility> supplier) {
		UUID id = player.getUUID();
		if (ACTIVE.containsKey(id)) {
			stop(player);
			return;
		}
		ACTIVE.put(id, supplier.get());
	}

	public static void stop(ServerPlayer player) {
		UUID id = player.getUUID();
		AbstractFieldAbility ability = ACTIVE.remove(id);
		if (ability != null) {
			ability.onRelease();
		}
	}

	public static void tick(ServerPlayer player) {
		UUID id = player.getUUID();
		AbstractFieldAbility ability = ACTIVE.get(id);
		if (ability == null) return;
		boolean keep = ability.tick();
		if (!keep) {
			ACTIVE.remove(id);
			ability.onRelease();
		}
	}
}