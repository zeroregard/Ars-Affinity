package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class PassiveFreeJumpEvents {
    
    private static final float VANILLA_JUMP_EXHAUSTION = 0.2F;
    
    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_FREE_JUMP, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
            // Vanilla jump exhaustion cost
            float refund = VANILLA_JUMP_EXHAUSTION * amountPerk.amount;
            player.getFoodData().addExhaustion(-refund);
            ArsAffinity.LOGGER.info("Player {} jumped - PASSIVE_FREE_JUMP refunded {} exhaustion", player.getName().getString(), refund);
        });
    }
}