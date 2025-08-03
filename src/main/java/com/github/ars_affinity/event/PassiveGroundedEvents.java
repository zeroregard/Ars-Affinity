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
public class PassiveGroundedEvents {
    
    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        boolean isNotOnGround = !player.onGround();
        
        if (isNotOnGround) {
            var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (progress != null) {
                int earthTier = progress.getTier(SpellSchools.ELEMENTAL_EARTH);
                if (earthTier > 0) {
                    AffinityPerkHelper.applyHighestTierPerk(progress, earthTier, SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.PASSIVE_GROUNDED, perk -> {
                        if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                            double currentRegen = event.getRegen();
                            double reduction = currentRegen * amountPerk.amount;
                            double newRegen = currentRegen - reduction;
                            
                            ArsAffinity.LOGGER.info("Player {} is not on ground - PASSIVE_GROUNDED perk ({}%) reducing mana regen from {} to {}", 
                                player.getName().getString(), (int)(amountPerk.amount * 100), currentRegen, newRegen);
                            
                            event.setRegen(newRegen);
                        }
                    });
                }
            }
        }
    }
} 