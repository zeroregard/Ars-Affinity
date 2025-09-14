package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

public class MidAirPhasingEvents {

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Check if player is airborne and has mid-air phasing perk
        if (player.onGround()) return;
        if (player.hasEffect(ModPotions.MID_AIR_PHASING_COOLDOWN_EFFECT)) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_MID_AIR_PHASING, AffinityPerk.DurationBasedPerk.class, perk -> {
            // The actual phasing logic is now handled in the ProjectileCollisionMixin
            // This event handler is kept for potential future enhancements like visual effects
            // or other airborne-related logic
            
            // Note: Cooldown is now applied in the mixin when phasing actually occurs
            // This prevents unnecessary cooldown application when no projectiles are around
        });
    }
}