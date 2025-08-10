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
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ProjectileReversalEvents {

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

        if (player.hasEffect(ModPotions.PROJECTILE_REVERSAL_COOLDOWN_EFFECT)) {
            return;
        }

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        int manipulationTier = progress.getTier(com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION);
        if (manipulationTier <= 0) {
            return;
        }

        final boolean[] hasPerk = {false};
        AffinityPerkHelper.applyHighestTierPerk(progress, manipulationTier, com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION, AffinityPerkType.PASSIVE_PROJECTILE_REVERSAL, perk -> {
            if (perk instanceof AffinityPerk.DurationBasedPerk) {
                hasPerk[0] = true;
            }
        });

        if (!hasPerk[0]) {
            return;
        }

        event.setCanceled(true);
        try {
            // Reverse the projectile's direction and velocity
            var motion = projectile.getDeltaMovement();
            var reversedMotion = motion.scale(-1.0); // Reverse direction
            
            // Set the reversed velocity
            projectile.setDeltaMovement(reversedMotion);
            
            // Update the projectile's rotation to face the new direction
            double x = reversedMotion.x;
            double z = reversedMotion.z;
            if (x != 0.0 || z != 0.0) {
                projectile.setYRot((float)(Math.atan2(z, x) * 180.0 / Math.PI) - 90.0f);
            }
            
            // If it's an arrow, also update the arrow's rotation
            if (projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
                arrow.setBaseDamage(arrow.getBaseDamage() * 1.5); // Increase damage for reversed projectiles
            }

            // Apply cooldown to the player
            AffinityPerkHelper.applyHighestTierPerk(progress, manipulationTier, com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION, AffinityPerkType.PASSIVE_PROJECTILE_REVERSAL, perk -> {
                if (perk instanceof AffinityPerk.DurationBasedPerk reversalPerk) {
                    player.addEffect(new MobEffectInstance(ModPotions.PROJECTILE_REVERSAL_COOLDOWN_EFFECT, reversalPerk.time));
                }
            });

        } catch (Exception e) {
            ArsAffinity.LOGGER.error("ProjectileReversal: Error reversing projectile", e);
        }
    }
}