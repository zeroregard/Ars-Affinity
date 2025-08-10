package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
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
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

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
                

                castDecoyEffect(player, ghostStepPerk.time);
                
                // Remove any projectiles stuck to the player before making them invisible
                removeStuckProjectiles(player);
                
                // Schedule delayed projectile removal to ensure all projectiles are cleaned up
                scheduleDelayedProjectileRemoval(player);
                
                // Apply invisibility effect
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, ghostStepPerk.time * 20)); // Convert seconds to ticks
                
                // Apply cooldown effect
                player.addEffect(new MobEffectInstance(ModPotions.GHOST_STEP_COOLDOWN_EFFECT, ghostStepPerk.cooldown * 20)); // Convert seconds to ticks
                
                ArsAffinity.LOGGER.info("Player {} activated Ghost Step - healed for {} health, invisible for {} seconds", 
                    player.getName().getString(), healAmount, ghostStepPerk.time);
            }
        });
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