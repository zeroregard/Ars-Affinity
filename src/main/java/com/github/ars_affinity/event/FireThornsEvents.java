package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Random;

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

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_FIRE_THORNS, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
            if (RANDOM.nextFloat() < amountPerk.amount) {
                // Set the attacker on fire
                attacker.setRemainingFireTicks(3 * 20);
                
                ArsAffinity.LOGGER.info("Fire Thorns activated! Player {} ignited attacker {} ({}% chance)", 
                    player.getName().getString(), attacker.getName().getString(), (int)(amountPerk.amount * 100));
            }
        });
    }
} 