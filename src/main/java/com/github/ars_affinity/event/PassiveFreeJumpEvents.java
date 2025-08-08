package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveFreeJumpEvents {
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            int airTier = progress.getTier(SpellSchools.ELEMENTAL_AIR);
            if (airTier > 0) {
                AffinityPerkHelper.applyHighestTierPerk(progress, airTier, SpellSchools.ELEMENTAL_AIR, AffinityPerkType.PASSIVE_FREE_JUMP, perk -> {
                    if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                        // Vanilla jump exhaustion cost is 0.2F
                        float refund = 0.2F * amountPerk.amount;
                        player.getFoodData().addExhaustion(-refund);
                        ArsAffinity.LOGGER.info("Player {} jumped - PASSIVE_FREE_JUMP refunded {} exhaustion", player.getName().getString(), refund);
                    }
                });
            }
        }
    }
}