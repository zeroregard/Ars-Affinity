package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveManipulationSicknessEvents {

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        // Check if player already has manipulation sickness and is trying to cast a manipulation spell
        if (player.hasEffect(ModPotions.MANIPULATION_SICKNESS_EFFECT) && containsManipulationGlyph(event.spell)) {
            event.setCanceled(true);
            ArsAffinity.LOGGER.info("Player {} attempted to cast manipulation spell while under manipulation sickness effect", 
                player.getName().getString());
            return;
        }

        // PASSIVE_MANIPULATION_SICKNESS removed - no longer needed in new system
        // All code commented out
    }

    private static boolean containsManipulationGlyph(com.hollingsworth.arsnouveau.api.spell.Spell spell) {
        return spell.unsafeList().stream()
            .anyMatch(part -> part != null && part.spellSchools.stream()
                .anyMatch(school -> school == SpellSchools.MANIPULATION));
    }

    private static void applyManipulationSickness(Player player, int duration, int hunger) {
        if (player == null) return;

        player.addEffect(new MobEffectInstance(ModPotions.MANIPULATION_SICKNESS_EFFECT, duration, 0, false, true, true));
        
        if (hunger > 0) {
            player.getFoodData().addExhaustion(hunger);
        }
    }
}
