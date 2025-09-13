package com.github.ars_affinity.event;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class PassiveIronveinEvents {
    
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if the effect being added is poison
        if (event.getEffectInstance().getEffect() == MobEffects.POISON) {
            // Check if player has the Ironvein perk
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_IRONVEIN, AffinityPerk.class, perk -> {
                // Remove the poison effect immediately
                player.removeEffect(MobEffects.POISON);
            });
        }
    }
}