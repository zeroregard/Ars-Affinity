package com.github.ars_affinity.event;

import com.github.ars_affinity.common.ability.field.ActiveFieldRegistry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.server.level.ServerPlayer;

public class FieldAbilityTicker {
	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer sp)) return;
		ActiveFieldRegistry.tick(sp);
	}
}