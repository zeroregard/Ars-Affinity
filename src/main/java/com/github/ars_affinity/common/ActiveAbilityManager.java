package com.github.ars_affinity.common;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IMana;
import com.hollingsworth.arsnouveau.api.mana.ManaCapability;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class ActiveAbilityManager {
    
    // Map of school to active ability perk type
    private static final Map<SpellSchools, AffinityPerkType> SCHOOL_ABILITY_MAP = new HashMap<>();
    
    static {
        SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_WATER, AffinityPerkType.ACTIVE_ICE_BLAST);
        SCHOOL_ABILITY_MAP.put(SpellSchools.MANIPULATION, AffinityPerkType.ACTIVE_MANIPULATION_ABILITY);
        // Add more schools and their abilities here as they are implemented
    }
    
    public static void triggerActiveAbility(ServerPlayer player) {
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        // Find the first school with an active ability
        AffinityPerk activePerk = null;
        SpellSchools activeSchool = null;
        
        for (Map.Entry<SpellSchools, AffinityPerkType> entry : SCHOOL_ABILITY_MAP.entrySet()) {
            SpellSchools school = entry.getKey();
            AffinityPerkType perkType = entry.getValue();
            
            int tier = progress.getTier(school);
            if (tier > 0) {
                // Check if player has this perk
                final AffinityPerk[] foundPerk = {null};
                AffinityPerkHelper.applyHighestTierPerk(progress, tier, school, perkType, perk -> {
                    foundPerk[0] = perk;
                });
                
                if (foundPerk[0] != null) {
                    activePerk = foundPerk[0];
                    activeSchool = school;
                    break;
                }
            }
        }
        
        if (activePerk == null || !(activePerk instanceof AffinityPerk.ActiveAbilityPerk)) {
            return;
        }

        AffinityPerk.ActiveAbilityPerk abilityPerk = (AffinityPerk.ActiveAbilityPerk) activePerk;
        
        // Check if player is on cooldown
        if (player.hasEffect(ModPotions.ACTIVE_ABILITY_COOLDOWN_EFFECT)) {
            return;
        }

        // Check mana cost
        IMana mana = ManaCapability.getMana(player).orElse(null);
        if (mana == null) {
            return;
        }

        float manaCost = abilityPerk.manaCost;
        int currentMana = mana.getCurrentMana();
        int requiredMana = (int) (currentMana * manaCost);
        
        if (currentMana < requiredMana) {
            return;
        }

        // Consume mana
        mana.removeMana(requiredMana);

        // Apply cooldown
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModPotions.ACTIVE_ABILITY_COOLDOWN_EFFECT, abilityPerk.cooldown));

        // Execute the specific ability based on the perk type
        switch (abilityPerk.perk) {
            case ACTIVE_ICE_BLAST:
                executeIceBlast(player, abilityPerk, requiredMana, currentMana);
                break;
            case ACTIVE_MANIPULATION_ABILITY:
                executeManipulationAbility(player, abilityPerk, requiredMana, currentMana);
                break;
            default:
                ArsAffinity.LOGGER.warn("Unknown active ability perk type: {}", abilityPerk.perk);
                break;
        }
    }
    
    private static void executeIceBlast(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, int requiredMana, int currentMana) {
        // Calculate scaling based on mana consumed
        float manaScaling = (float) requiredMana / currentMana;
        float scaledDamage = perk.damage * manaScaling;
        int scaledFreezeTime = (int) (perk.freezeTime * manaScaling);
        float scaledRadius = perk.radius * manaScaling;

        // Get entities in range
        net.minecraft.world.phys.Vec3 playerPos = player.position();
        net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(
            playerPos.x - scaledRadius, playerPos.y - scaledRadius, playerPos.z - scaledRadius,
            playerPos.x + scaledRadius, playerPos.y + scaledRadius, playerPos.z + scaledRadius
        );

        java.util.List<net.minecraft.world.entity.Entity> entities = player.level().getEntities(player, area, entity -> 
            entity instanceof net.minecraft.world.entity.LivingEntity && entity != player && entity.isAlive());

        // Apply effects to entities
        for (net.minecraft.world.entity.Entity entity : entities) {
            if (entity instanceof net.minecraft.world.entity.LivingEntity livingEntity) {
                // Apply damage
                livingEntity.hurt(player.damageSources().playerAttack(player), scaledDamage);
                
                // Apply freezing effect
                livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, scaledFreezeTime, 2));
                livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN, scaledFreezeTime, 2));
                
                // Visual effects
                if (livingEntity.level().isClientSide) {
                    livingEntity.level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, 
                        livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ(), 0, 0, 0);
                }
            }
        }

        // Visual and sound effects
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // Particle effects around the player
        for (int i = 0; i < 20; i++) {
            double angle = Math.random() * 2 * Math.PI;
            double radius = Math.random() * scaledRadius;
            double x = player.getX() + Math.cos(angle) * radius;
            double z = player.getZ() + Math.sin(angle) * radius;
            player.level().addParticle(net.minecraft.core.particles.ParticleTypes.SNOWFLAKE, x, player.getY() + 1, z, 0, 0.1, 0);
        }

        ArsAffinity.LOGGER.info("Player {} used ICE BLAST ability with {} mana cost, {} damage, {} freeze time, {} radius", 
            player.getName().getString(), requiredMana, scaledDamage, scaledFreezeTime, scaledRadius);
    }
    
    private static void executeManipulationAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk, int requiredMana, int currentMana) {
        // Placeholder manipulation ability - does nothing for now
        // Just log that it was used
        
        ArsAffinity.LOGGER.info("Player {} used MANIPULATION ABILITY with {} mana cost (placeholder ability)", 
            player.getName().getString(), requiredMana);
        
        // Could add placeholder effects here like particles or sounds
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
            net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_WEAK, net.minecraft.sounds.SoundSource.PLAYERS, 0.5F, 1.0F);
    }
}