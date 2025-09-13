package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

public class MidAirPhasingEvents {

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        var projectile = event.getProjectile();
        var level = projectile.level();

        if (level.isClientSide()) {
            return;
        }

        var rayTraceResult = event.getRayTraceResult();
        if (!(rayTraceResult instanceof net.minecraft.world.phys.EntityHitResult entityHitResult)) {
            return;
        }

        var hitEntity = entityHitResult.getEntity();
        if (!(hitEntity instanceof Player player)) {
            return;
        }

        // Check if player is airborne (not on ground)
        if (player.onGround()) {
            return;
        }

        // Check if player has cooldown effect
        if (player.hasEffect(ModPotions.MID_AIR_PHASING_COOLDOWN_EFFECT)) {
            return;
        }

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_MID_AIR_PHASING, AffinityPerk.DurationBasedPerk.class, perk -> {
            // Projectiles pass through the player while airborne
            event.setCanceled(true);
            
            // Apply cooldown effect
            player.addEffect(new MobEffectInstance(ModPotions.MID_AIR_PHASING_COOLDOWN_EFFECT, perk.time, 0, false, true, true));
            
            ArsAffinity.LOGGER.info("Mid-Air Projectile Phasing activated! Player {} phased through projectile ({} tick cooldown)", 
                player.getName().getString(), perk.time);
        });
    }
}