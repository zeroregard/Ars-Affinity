package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.LivingCaster;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectSummonDecoy;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class GhostStepEvents {

    @SubscribeEvent(priority = net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Check if the dying entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if this is server side
        if (player.level().isClientSide()) {
            return;
        }

        ArsAffinity.LOGGER.info("Ghost Step: Player {} is dying, checking for Ghost Step perk", player.getName().getString());

        // Check if player already has cooldown
        if (player.hasEffect(ModPotions.GHOST_STEP_COOLDOWN_EFFECT)) {
            ArsAffinity.LOGGER.info("Ghost Step: Player {} has cooldown, skipping", player.getName().getString());
            return;
        }

        // Check if player has the ghost step perk
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_GHOST_STEP)) {
            event.setCanceled(true);
            ArsAffinity.LOGGER.info("Ghost Step: Player {} has Ghost Step perk, activating", player.getName().getString());
            
            float amount = AffinityPerkHelper.getPerkAmount(player, AffinityPerkType.PASSIVE_GHOST_STEP);
            int time = AffinityPerkHelper.getPerkTime(player, AffinityPerkType.PASSIVE_GHOST_STEP);
            int cooldown = AffinityPerkHelper.getPerkCooldown(player, AffinityPerkType.PASSIVE_GHOST_STEP);
            
            // Calculate healing amount based on percentage of max health
            float maxHealth = player.getMaxHealth();
            float healAmount = maxHealth * amount;
            
            ArsAffinity.LOGGER.info("Ghost Step: Setting health to {} (was {}), canceling event", healAmount, player.getHealth());
            
            // Set health first, then cancel the event (following TotemPerk pattern)
            player.setHealth(healAmount);
            
            ArsAffinity.LOGGER.info("Ghost Step: Event canceled: {}, player health: {}", event.isCanceled(), player.getHealth());

            castDecoyEffect(player, time);

            // Remove any projectiles stuck to the player before making them invisible
            removeStuckProjectiles(player);

            // Schedule delayed projectile removal to ensure all projectiles are cleaned up
            scheduleDelayedProjectileRemoval(player);

            // Apply invisibility effect
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, time)); // time is already in ticks

            // Apply cooldown effect
            player.addEffect(new MobEffectInstance(ModPotions.GHOST_STEP_COOLDOWN_EFFECT, cooldown, 0, false, true, true)); // cooldown is already in ticks

            ArsAffinity.LOGGER.info("Player {} activated Ghost Step - healed for {} health, invisible for {} seconds",
                    player.getName().getString(), healAmount, time);
        } else {
            ArsAffinity.LOGGER.info("Ghost Step: Player {} does not have Ghost Step perk", player.getName().getString());
        }
    }

    private static void removeStuckProjectiles(Player player) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }
        player.setArrowCount(0);
    }

    private static void scheduleDelayedProjectileRemoval(Player player) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }

        // Schedule the removal to happen after 1 tick to ensure all projectiles are properly cleaned up
        level.getServer().tell(new net.minecraft.server.TickTask(level.getServer().getTickCount() + 1, () -> {
            if (player.isAlive() && !player.level().isClientSide()) {
                removeStuckProjectiles(player);
            }
        }));
    }

    private static void castDecoyEffect(Player player, int durationSeconds) {
        try {
            Level level = player.level();

            SpellStats spellStats = new SpellStats.Builder()
                    .addDurationModifier(durationSeconds / 30.0)
                    .build();

            Spell emptySpell = new Spell();
            SpellContext spellContext = new SpellContext(level, emptySpell, player, new LivingCaster(player));

            EntityHitResult hitResult = new EntityHitResult(player);

            EffectSummonDecoy.INSTANCE.onResolve(hitResult, level, player, spellStats, spellContext, null);

            ArsAffinity.LOGGER.info("Player {} cast Decoy effect for {} seconds",
                    player.getName().getString(), durationSeconds);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Ghost Step: Error casting decoy effect", e);
        }
    }
}