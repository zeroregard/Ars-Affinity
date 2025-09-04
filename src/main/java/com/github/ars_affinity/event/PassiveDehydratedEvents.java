package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveDehydratedEvents {

    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        boolean isInNether = player.level().dimension().location().getPath().equals("the_nether");
        boolean isOnFire = player.isOnFire();

        if (isInNether || isOnFire) {
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_DEHYDRATED, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
                double currentRegen = event.getRegen();
                double reduction = currentRegen * amountPerk.amount;
                double newRegen = currentRegen - reduction;

                String condition = isInNether ? "Nether" : "on fire";
                ArsAffinity.LOGGER.info("Player {} is in {} - PASSIVE_DEHYDRATED perk ({}%) reducing mana regen from {} to {}",
                    player.getName().getString(), condition, (int)(amountPerk.amount * 100), currentRegen, newRegen);

                event.setRegen(newRegen);
            });
        }
    }
} 