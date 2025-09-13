package com.github.ars_affinity.event;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

public class PassiveStonefootEvents {
    
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if the effect being added is slowness
        if (event.getEffectInstance().getEffect() == MobEffects.MOVEMENT_SLOWDOWN) {
            // Check if player has the Stonefoot perk
            AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_STONEFOOT, AffinityPerk.class, perk -> {
                // Remove the slowness effect immediately
                player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            });
        }
    }
}