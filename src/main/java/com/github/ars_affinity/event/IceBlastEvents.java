package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IMana;
import com.hollingsworth.arsnouveau.api.mana.ManaCapability;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class IceBlastEvents {
    
    public static void triggerIceBlast(ServerPlayer player) {
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        int waterTier = progress.getTier(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER);
        if (waterTier <= 0) {
            return;
        }

        // Check if player is on cooldown
        if (player.hasEffect(ModPotions.ICE_BLAST_COOLDOWN_EFFECT)) {
            return;
        }

        // Get the ICE BLAST perk
        final AffinityPerk.ActiveAbilityPerk[] iceBlastPerk = {null};
        AffinityPerkHelper.applyHighestTierPerk(progress, waterTier, 
            com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER, 
            AffinityPerkType.ACTIVE_ICE_BLAST, perk -> {
                if (perk instanceof AffinityPerk.ActiveAbilityPerk activePerk) {
                    iceBlastPerk[0] = activePerk;
                }
            });

        if (iceBlastPerk[0] == null) {
            return;
        }

        AffinityPerk.ActiveAbilityPerk perk = iceBlastPerk[0];

        // Check mana cost
        IMana mana = ManaCapability.getMana(player).orElse(null);
        if (mana == null) {
            return;
        }

        float manaCost = perk.manaCost;
        int currentMana = mana.getCurrentMana();
        int requiredMana = (int) (currentMana * manaCost);
        
        if (currentMana < requiredMana) {
            return;
        }

        // Consume mana
        mana.removeMana(requiredMana);

        // Calculate scaling based on mana consumed
        float manaScaling = (float) requiredMana / currentMana;
        float scaledDamage = perk.damage * manaScaling;
        int scaledFreezeTime = (int) (perk.freezeTime * manaScaling);
        float scaledRadius = perk.radius * manaScaling;

        // Get entities in range
        Vec3 playerPos = player.position();
        AABB area = new AABB(
            playerPos.x - scaledRadius, playerPos.y - scaledRadius, playerPos.z - scaledRadius,
            playerPos.x + scaledRadius, playerPos.y + scaledRadius, playerPos.z + scaledRadius
        );

        List<Entity> entities = player.level().getEntities(player, area, entity -> 
            entity instanceof LivingEntity && entity != player && entity.isAlive());

        // Apply effects to entities
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                // Apply damage
                livingEntity.hurt(player.damageSources().playerAttack(player), scaledDamage);
                
                // Apply freezing effect
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, scaledFreezeTime, 2));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, scaledFreezeTime, 2));
                
                // Visual effects
                if (livingEntity.level().isClientSide) {
                    livingEntity.level().addParticle(ParticleTypes.SNOWFLAKE, 
                        livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 0, 0, 0);
                }
            }
        }

        // Apply cooldown
        player.addEffect(new MobEffectInstance(ModPotions.ICE_BLAST_COOLDOWN_EFFECT, perk.cooldown));

        // Visual and sound effects
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // Particle effects around the player
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * scaledRadius;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            player.level().addParticle(ParticleTypes.SNOWFLAKE, x, player.getY() + 1, z, 0, 0.1, 0);
        }

        ArsAffinity.LOGGER.info("Player {} used ICE BLAST ability with {} mana cost, {} damage, {} freeze time, {} radius", 
            player.getName().getString(), requiredMana, scaledDamage, scaledFreezeTime, scaledRadius);
    }
}