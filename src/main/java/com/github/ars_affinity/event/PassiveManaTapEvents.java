package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveManaTapEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Post event) {
        if (!(event.caster instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Only trigger if damage was actually dealt
        if (event.damage <= 0) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // Check all schools for mana tap perks
            AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_MANA_TAP, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    double manaRestore = event.damage * amountPerk.amount;
                    
                    // Restore mana to the player
                    IManaCap playerMana = CapabilityRegistry.getMana(player);
                    if (playerMana != null) {
                        double currentMana = playerMana.getCurrentMana();
                        double maxMana = playerMana.getMaxMana();
                        double newMana = Math.min(currentMana + manaRestore, maxMana);
                        
                        if (newMana > currentMana) {
                            playerMana.setMana(newMana);
                            
                            ArsAffinity.LOGGER.info("Player {} dealt {} damage - PASSIVE_MANA_TAP restored {} mana ({}%)", 
                                player.getName().getString(), event.damage, manaRestore, (int)(amountPerk.amount * 100));
                        }
                    }
                }
            });
        }
    }
} 