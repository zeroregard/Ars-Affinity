package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.perk.AffinityPerk;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;

import java.util.List;

public class SwarmHelper {
    private static final SwarmHelper INSTANCE = new SwarmHelper();
    private static final double SWARM_RADIUS = 32.0; // Search radius for minions

    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        INSTANCE.executeAbilityInternal(player, perk);
    }

    private void executeAbilityInternal(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        // Check mana
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            return;
        }

        double currentMana = manaCap.getCurrentMana();
        if (currentMana < perk.manaCost) {
            return;
        }

        // Check cooldown
        if (isPlayerOnCooldown(player)) {
            return;
        }

        // Apply cooldown
        applyCooldown(player, perk.cooldown);

        // Execute swarm ability
        executeSwarm(player);

        // Consume mana
        consumeMana(player, perk, manaCap);

        // Visual and sound effects
        spawnParticles(player);
        playSounds(player);
    }

    private void executeSwarm(ServerPlayer player) {
        // TODO: Implement actual minion targeting logic
        // This is a placeholder for the core functionality
        // The actual implementation would need to:
        // 1. Find all minions within SWARM_RADIUS
        // 2. Get the player's current target (crosshair entity)
        // 3. Command all minions to attack that target
        // 4. Handle different types of minions (summons, golems, etc.)
        
        ArsAffinity.LOGGER.info("SWARM: Executing swarm ability for player {}", player.getName().getString());
        
        // Placeholder: Find entities in radius and log them
        AABB searchArea = new AABB(
            player.getX() - SWARM_RADIUS, player.getY() - SWARM_RADIUS, player.getZ() - SWARM_RADIUS,
            player.getX() + SWARM_RADIUS, player.getY() + SWARM_RADIUS, player.getZ() + SWARM_RADIUS
        );
        
        List<Entity> nearbyEntities = player.level().getEntities(player, searchArea);
        ArsAffinity.LOGGER.info("SWARM: Found {} entities in radius", nearbyEntities.size());
        
        // TODO: Filter for actual minions and command them to attack target
    }

    private boolean isPlayerOnCooldown(Player player) {
        Holder<MobEffect> effect = ModPotions.SWARM_COOLDOWN_EFFECT;
        return effect != null && player.hasEffect(effect);
    }

    private void applyCooldown(Player player, int cooldownTicks) {
        Holder<MobEffect> effect = ModPotions.SWARM_COOLDOWN_EFFECT;
        if (effect != null) {
            player.addEffect(new MobEffectInstance(effect, cooldownTicks, 0, false, true, true));
        }
    }

    private void consumeMana(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, IManaCap manaCap) {
        manaCap.removeMana(perk.manaCost);
    }

    private void spawnParticles(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            // Spawn particles around the player
            for (int i = 0; i < 20; i++) {
                double x = player.getX() + (Math.random() - 0.5) * 4;
                double y = player.getY() + Math.random() * 2;
                double z = player.getZ() + (Math.random() - 0.5) * 4;
                serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 1, 0, 0, 0, 0.05);
            }
        }
    }

    private void playSounds(ServerPlayer player) {
        var pos = player.position();
        player.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.VILLAGER_YES, SoundSource.PLAYERS, 0.7f, 1.3f);
    }
}