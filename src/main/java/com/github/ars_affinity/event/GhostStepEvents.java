package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class GhostStepEvents {

    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        // Check if the dying entity is a player
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Check if this is server side
        if (player.level().isClientSide()) {
            return;
        }

        // Check if player already has cooldown
        if (player.hasEffect(ModPotions.GHOST_STEP_COOLDOWN_EFFECT)) {
            return;
        }

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        int abjurationTier = progress.getTier(SpellSchools.ABJURATION);
        if (abjurationTier <= 0) {
            return;
        }

        final boolean[] hasGhostStepPerk = {false};
        AffinityPerkHelper.applyHighestTierPerk(progress, abjurationTier, SpellSchools.ABJURATION, AffinityPerkType.PASSIVE_GHOST_STEP, perk -> {
            if (perk instanceof AffinityPerk.GhostStepPerk ghostStepPerk) {
                hasGhostStepPerk[0] = true;
                
                // Cancel the death event
                event.setCanceled(true);
                
                // Calculate healing amount based on percentage of max health
                float maxHealth = player.getMaxHealth();
                float healAmount = maxHealth * ghostStepPerk.amount;
                player.setHealth(healAmount);
                
                // Apply invisibility effect
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ghostStepPerk.time * 20)); // Convert seconds to ticks
                
                // Create decoy at player's location
                createDecoy(player, ghostStepPerk.time);
                
                // Apply cooldown effect
                player.addEffect(new MobEffectInstance(ModPotions.GHOST_STEP_COOLDOWN_EFFECT, ghostStepPerk.cooldown * 20)); // Convert seconds to ticks
                
                ArsAffinity.LOGGER.info("Player {} activated Ghost Step - healed for {} health, invisible for {} seconds", 
                    player.getName().getString(), healAmount, ghostStepPerk.time);
            }
        });
    }

    private static void createDecoy(Player player, int durationSeconds) {
        try {
            Level level = player.level();
            
            // Create a zombie as the decoy (placeholder implementation)
            // In a real implementation, you might want to create a custom entity that looks like the player
            var decoy = EntityType.ZOMBIE.create(level);
            if (decoy != null) {
                decoy.setPos(player.getX(), player.getY(), player.getZ());
                decoy.setCustomName(player.getDisplayName());
                decoy.setCustomNameVisible(true);
                
                // Make the decoy persist for the specified duration
                level.addFreshEntity(decoy);
                
                // Schedule removal of the decoy after duration
                level.getServer().tell(new net.minecraft.server.TickTask(durationSeconds * 20, () -> {
                    if (decoy.isAlive() && !decoy.isRemoved()) {
                        decoy.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                    }
                }));
            }
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Ghost Step: Error creating decoy", e);
        }
    }
}