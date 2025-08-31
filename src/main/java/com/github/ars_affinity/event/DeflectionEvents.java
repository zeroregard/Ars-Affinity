package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class DeflectionEvents {

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
        if (!(hitEntity instanceof net.minecraft.world.entity.player.Player player)) {
            return;
        }

        if (player.hasEffect(ModPotions.DEFLECTION_COOLDOWN_EFFECT)) {
            return;
        }

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_DEFLECTION, AffinityPerk.DurationBasedPerk.class, perk -> {
            // Process the deflection logic
            event.setCanceled(true);
            try {
                var motion = projectile.getDeltaMovement();
                var reversedMotion = motion.scale(-1.0);

                projectile.setDeltaMovement(reversedMotion);

                double x = reversedMotion.x;
                double z = reversedMotion.z;
                if (x != 0.0 || z != 0.0) {
                    projectile.setYRot((float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f);
                }
                
                // If it's an arrow, also update the arrow's rotation
                if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                    arrow.setBaseDamage(arrow.getBaseDamage() * 1.5); // Increase damage for reversed projectiles
                }
                
                // Send motion packet to sync velocity with client
                if (projectile instanceof net.minecraft.world.entity.Entity entity) {
                    net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket motionPacket = 
                        new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(entity);
                    level.getServer().getPlayerList().broadcast(null, entity.getX(), entity.getY(), entity.getZ(), 64.0, level.dimension(), motionPacket);
                }

                // Apply cooldown effect
                player.addEffect(new MobEffectInstance(ModPotions.DEFLECTION_COOLDOWN_EFFECT, perk.time, 0, false, true, true));

            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Deflection: Error reversing projectile", e);
            }
        });
    }
} 