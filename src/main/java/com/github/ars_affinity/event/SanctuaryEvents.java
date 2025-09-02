package com.github.ars_affinity.event;

import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class SanctuaryEvents {

	@SubscribeEvent
	public static void onLivingHurt(LivingIncomingDamageEvent event) {
		LivingEntity target = event.getEntity();
		if (target.level().isClientSide()) return;
		if (target.hasEffect(ModPotions.SANCTUARY_EFFECT)) {
			event.setCanceled(true);
		}
	}
}