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
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Random;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FireThornsEvents {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Get the attacker
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? 
            (LivingEntity) event.getSource().getEntity() : null;
        
        if (attacker == null) return;

        // Get player's affinity progress
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;

        int fireTier = progress.getTier(SpellSchools.ELEMENTAL_FIRE);
        if (fireTier > 0) {
            AffinityPerkHelper.applyPerks(progress, fireTier, SpellSchools.ELEMENTAL_FIRE, perk -> {
                if (perk.perk == AffinityPerkType.PASSIVE_FIRE_THORNS && perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    if (RANDOM.nextFloat() < amountPerk.amount) {
                        // Set the attacker on fire
                        attacker.setRemainingFireTicks(3 * 20);
                        
                        ArsAffinity.LOGGER.info("Fire Thorns activated! Player {} ignited attacker {} ({}% chance)", 
                            player.getName().getString(), attacker.getName().getString(), (int)(amountPerk.amount * 100));
                    }
                }
            });
        }
    }
} 