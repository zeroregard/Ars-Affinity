package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.IWrappedCaster;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SilencedEvents {
	@SubscribeEvent
	public static void onSpellCast(SpellCastEvent event) {
		if (!(event.context.getCaster() instanceof IWrappedCaster caster)) return;
		if (!(caster instanceof PlayerCaster pc)) return;
		var player = pc.player;
		if (player == null) return;
		if (player.hasEffect(ModPotions.SILENCED_EFFECT)) {
			event.setCanceled(true);
		}
	}
}

