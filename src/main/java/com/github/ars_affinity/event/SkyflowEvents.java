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

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) {
            return;
        }

        if (player.tickCount % 20 != 0) {
            return;
        }

        if (player.isInWaterRainOrBubble()) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    var perks = AffinityPerkManager.getPerksForCurrentLevel(SpellSchools.ELEMENTAL_WATER, waterTier);
                    for (AffinityPerk perk : perks) {
                        if (perk.perk == AffinityPerkType.PASSIVE_SKYFLOW) {
                            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                                if (amountPerk.amount > 0) {
                                    int amplifier = (int) (amountPerk.amount * 5) - 1;

                                    MobEffectInstance effect = new MobEffectInstance(
                                            ModPotions.SKYFLOW_EFFECT,
                                            80,
                                            amplifier,
                                            false,
                                            false,
                                            true
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
