package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
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
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveManipulationSicknessEvents {

    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            int manipulationTier = progress.getTier(SpellSchools.MANIPULATION);
            if (manipulationTier > 0) {
                AffinityPerkHelper.applyHighestTierPerk(progress, manipulationTier, SpellSchools.MANIPULATION, AffinityPerkType.PASSIVE_MANIPULATION_SICKNESS, perk -> {
                    if (perk instanceof AffinityPerk.ManipulationSicknessPerk sicknessPerk) {
                        if (containsManipulationGlyph(event.spell)) {
                            applyManipulationSickness(player, sicknessPerk.duration, sicknessPerk.hunger);
                            ArsAffinity.LOGGER.info("Player {} cast manipulation spell with PASSIVE_MANIPULATION_SICKNESS perk for {} seconds with {} hunger",
                                player.getName().getString(), sicknessPerk.duration / 20, sicknessPerk.hunger);
                        }
                    }
                });
            }
        }
    }

    private static boolean containsManipulationGlyph(com.hollingsworth.arsnouveau.api.spell.Spell spell) {
        return spell.unsafeList().stream()
            .anyMatch(part -> part != null && part.spellSchools.stream()
                .anyMatch(school -> school == SpellSchools.MANIPULATION));
    }

    private static void applyManipulationSickness(Player player, int duration, int hunger) {
        if (player == null) return;

        player.addEffect(new MobEffectInstance(ModPotions.MANIPULATION_SICKNESS_EFFECT, duration));
        
        if (hunger > 0) {
            player.getFoodData().addExhaustion(hunger);
        }
    }
}
