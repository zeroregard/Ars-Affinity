package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;

import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class SkyflowEvents {

    /**
     * Applies SkyflowEffect (mana regeneration boost) when players are in water or rain.
     * Only runs once per second (every 20 ticks) to be efficient.
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Only run on server side and at the end of the tick
        if (player.level().isClientSide()) {
            return;
        }

        // Only check every 20 ticks (once per second) for efficiency
        if (player.tickCount % 20 != 0) {
            return;
        }

        // Check if player is in water or rain
        if (player.isInWaterRainOrBubble()) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    // Get the SKYFLOW perk for mana regeneration boost when wet
                    var perks = AffinityPerkManager.getPerksForCurrentLevel(SpellSchools.ELEMENTAL_WATER, waterTier);
                    for (AffinityPerk perk : perks) {
                        if (perk.perk == AffinityPerkType.PASSIVE_SKYFLOW) {
                            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                                if (amountPerk.amount > 0) {
                                    int amplifier = (int) (amountPerk.amount * 5) - 1; // Convert amount to amplifier

                                    MobEffectInstance effect = new MobEffectInstance(
                                            ModPotions.SKYFLOW_EFFECT,
                                            80, // 4 seconds
                                            amplifier,
                                            false, // ambient
                                            false, // visible particles
                                            true   // icon
                                    );

                                    player.addEffect(effect);

                                    ArsAffinity.LOGGER.debug(
                                            "Applied skyflow effect to player {}: amplifier {}, duration 4s",
                                            player.getName().getString(),
                                            amplifier
                                    );
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
