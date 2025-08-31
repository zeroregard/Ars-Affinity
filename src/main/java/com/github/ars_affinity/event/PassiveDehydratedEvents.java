package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveDehydratedEvents {

    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        boolean isInNether = player.level().dimension().location().getPath().equals("the_nether");
        boolean isOnFire = player.isOnFire();

        if (isInNether || isOnFire) {
            // Get player's affinity progress
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int waterTier = progress.getTier(SpellSchools.ELEMENTAL_WATER);
                if (waterTier > 0) {
                    // Apply perks for water school
                    AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_DEHYDRATED, perk -> {
                        if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                            double currentRegen = event.getRegen();
                            double reduction = currentRegen * amountPerk.amount;
                            double newRegen = currentRegen - reduction;

                            String condition = isInNether ? "Nether" : "on fire";
                            ArsAffinity.LOGGER.info("Player {} is in {} and has water tier {} - PASSIVE_DEHYDRATED perk ({}%) reducing mana regen from {} to {}",
                                player.getName().getString(), condition, waterTier, (int)(amountPerk.amount * 100), currentRegen, newRegen);

                            event.setRegen(newRegen);
                        }
                    });
                }
            }
        }
    }
} 