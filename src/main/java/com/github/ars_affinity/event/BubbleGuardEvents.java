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
public class BubbleGuardEvents {

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

        if (player.hasEffect(ModPotions.BUBBLE_GUARD_COOLDOWN_EFFECT)) {
            return;
        }

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        int waterTier = progress.getTier(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER);
        if (waterTier <= 0) {
            return;
        }


        final boolean[] hasPerk = {false};
        AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_BUBBLE_GUARD, perk -> {
            if (perk instanceof AffinityPerk.DurationBasedPerk) {
                hasPerk[0] = true;
            }
        });

        if (!hasPerk[0]) {
            return;
        }


        event.setCanceled(true);
        try {
            var bubble = new com.hollingsworth.arsnouveau.common.entity.BubbleEntity(player.level(), 100, 0.0f);
            bubble.setPos(projectile.getX(), projectile.getY(), projectile.getZ());
            bubble.setOwner(player);
            player.level().addFreshEntity(bubble);

            boolean captured = bubble.tryCapturing(projectile);


            if (captured) {
                // For projectiles with natural gravity (like arrows), let them fall naturally
                // For projectiles without gravity (like fireballs, spell projectiles), remove them when bubble bursts
                if (projectile.isNoGravity() || !(projectile instanceof net.minecraft.world.entity.projectile.AbstractArrow)) {
                    // Schedule removal when bubble bursts (100 ticks = 5 seconds)
                    projectile.level().getServer().tell(new net.minecraft.server.TickTask(100, () -> {
                        if (projectile.isAlive() && !projectile.isRemoved()) {
                            projectile.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                        }
                    }));
                }
            }

            // Apply cooldown to the player
            AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER, AffinityPerkType.PASSIVE_BUBBLE_GUARD, perk -> {
                if (perk instanceof AffinityPerk.DurationBasedPerk bubbleGuardPerk) {
                    player.addEffect(new MobEffectInstance(ModPotions.BUBBLE_GUARD_COOLDOWN_EFFECT, bubbleGuardPerk.time));
                }
            });

        } catch (Exception e) {
            ArsAffinity.LOGGER.error("BubbleGuard: Error creating bubble for projectile", e);
        }
    }
}
