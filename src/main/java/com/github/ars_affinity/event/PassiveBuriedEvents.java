package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveBuriedEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        boolean isDeepUnderground = player.getEyePosition().y() < ArsAffinityConfig.DEEP_UNDERGROUND_Y_THRESHOLD.get();
        
        if (isDeepUnderground) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int airTier = progress.getTier(SpellSchools.ELEMENTAL_AIR);
                if (airTier > 0) {
                    AffinityPerkHelper.applyHighestTierPerk(progress, airTier, SpellSchools.ELEMENTAL_AIR, AffinityPerkType.PASSIVE_BURIED, perk -> {
                        if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                            double currentRegen = event.getRegen();
                            double reduction = currentRegen * amountPerk.amount;
                            double newRegen = currentRegen - reduction;
                            
                            ArsAffinity.LOGGER.info("Player {} is deep underground (Y={}) and has air tier {} - PASSIVE_BURIED perk ({}%) reducing mana regen from {} to {}", 
                                player.getName().getString(), (int)player.getEyePosition().y(), airTier, (int)(amountPerk.amount * 100), currentRegen, newRegen);
                            
                            event.setRegen(newRegen);
                        }
                    });
                }
            }
        }
    }
} 