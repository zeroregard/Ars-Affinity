package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.api.spell.Spell;
import com.hollingsworth.arsnouveau.api.spell.SpellContext;
import com.hollingsworth.arsnouveau.api.spell.SpellResolver;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectColdSnap;
import com.hollingsworth.arsnouveau.common.spell.method.MethodTouch;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public class IceBlastHelper {
    
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        ArsAffinity.LOGGER.info("ICE BLAST: Starting execution for player {} with perk: damage={}, freezeTime={}, radius={}, cooldown={}", 
            player.getName().getString(), perk.damage, perk.freezeTime, perk.radius, perk.cooldown);
        
        if (isPlayerOnCooldown(player)) {
            return;
        }
        
        applyCooldown(player, perk.cooldown);
        
        float manaScaling = calculateManaScaling(player, perk.damage);
        float scaledDamage = perk.damage * manaScaling;
        int scaledFreezeTime = (int) (perk.freezeTime * manaScaling);
        float scaledRadius = perk.radius * manaScaling;
        
        
        
        damageEntitiesInRange(player, scaledDamage, scaledFreezeTime, scaledRadius);
        placeSnowAroundPlayer(player);
        extinguishPlayerFire(player);
        extinguishAreaFires(player);
        convertLavaToObsidian(player);
        freezeAreaWater(player);
        spawnParticleEffects(player);
        playSoundEffects(player);
        
        
    }
    
    private static boolean isPlayerOnCooldown(ServerPlayer player) {
        return player.hasEffect(ModPotions.ICE_BLAST_COOLDOWN_EFFECT);
    }
    
    private static void applyCooldown(ServerPlayer player, int cooldownTicks) {

        player.addEffect(new MobEffectInstance(ModPotions.ICE_BLAST_COOLDOWN_EFFECT, cooldownTicks, 0, false, false, false));
    }
    
    private static float calculateManaScaling(ServerPlayer player, float baseValue) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) return 0.0f;
        
        int currentMana = (int) manaCap.getCurrentMana();
        int maxMana = manaCap.getMaxMana();
        
        if (maxMana <= 0) return 0.0f;
        
        float manaPercentage = (float) currentMana / maxMana;
        return baseValue * manaPercentage;
    }
    
    private static void damageEntitiesInRange(ServerPlayer player, float damage, int freezeTime, float radius) {
        Vec3 playerPos = player.position();
        
        
        AABB scanArea = new AABB(
            playerPos.x - 2, playerPos.y - 2, playerPos.z - 2,
            playerPos.x + 2, playerPos.y + 2, playerPos.z + 2
        );
        
        List<Entity> entitiesInRange = player.level().getEntities(player, scanArea, entity -> 
            entity instanceof LivingEntity && entity != player && !entity.isAlliedTo(player));
        

        
        int affectedCount = 0;
        for (Entity entity : entitiesInRange) {
            if (entity instanceof LivingEntity livingEntity) {
                
                
                applyEntityEffects(livingEntity, damage, freezeTime);
                affectedCount++;
            }
        }
        

    }
    
    private static void applyEntityEffects(LivingEntity entity, float damage, int freezeTime) {
        applyFreezingEffect(entity, freezeTime);
        applySlownessEffect(entity, 200);
        applySpellDamage(entity, damage);
    }
    
    
    private static void applyFreezingEffect(LivingEntity entity, int duration) {
        entity.setTicksFrozen(entity.getTicksFrozen() + duration);
    }
    
    private static void applySlownessEffect(LivingEntity entity, int duration) {
        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 0, false, false, false));
    }
    
    private static void applySpellDamage(LivingEntity entity, float damage) {
        Spell spell = new Spell();
        spell = spell.add(MethodTouch.INSTANCE);
        spell = spell.add(EffectColdSnap.INSTANCE);
        
        SpellContext context = SpellContext.fromEntity(spell, entity, entity.getMainHandItem());
        SpellResolver resolver = new SpellResolver(context);
        
        resolver.onResolveEffect(entity.level(), new EntityHitResult(entity));
    }
    
    private static void placeSnowAroundPlayer(ServerPlayer player) {
        Vec3 playerPos = player.position();

        
        int snowPlaced = 0;
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos snowPos = new BlockPos(
                    (int) playerPos.x + x,
                    (int) playerPos.y,
                    (int) playerPos.z + z
                );
                
                if (canPlaceSnow(player.level(), snowPos)) {
                    BlockState snowState = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1);
                    player.level().setBlockAndUpdate(snowPos, snowState);
                    snowPlaced++;
                }
            }
        }
        

    }
    
    private static boolean canPlaceSnow(net.minecraft.world.level.Level level, BlockPos pos) {
        BlockState currentState = level.getBlockState(pos);
        BlockState belowState = level.getBlockState(pos.below());
        
        return currentState.isAir() && belowState.isSolidRender(level, pos.below());
    }
    
    private static void spawnParticleEffects(ServerPlayer player) {
        Vec3 playerPos = player.position();

        
        if (player.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            // Spawn snowflake particles
            for (int i = 0; i < 20; i++) {
                double offsetX = (Math.random() - 0.5) * 4.0;
                double offsetY = Math.random() * 2.0;
                double offsetZ = (Math.random() - 0.5) * 4.0;
                
                serverLevel.sendParticles(
                    ParticleTypes.SNOWFLAKE,
                    playerPos.x + offsetX,
                    playerPos.y + offsetY,
                    playerPos.z + offsetZ,
                    1, 0, 0, 0, 0.1
                );
            }
            
            // Spawn explosion particles for impact effect
            for (int i = 0; i < 15; i++) {
                double offsetX = (Math.random() - 0.5) * 3.0;
                double offsetY = Math.random() * 1.5;
                double offsetZ = (Math.random() - 0.5) * 3.0;
                
                serverLevel.sendParticles(
                    ParticleTypes.EXPLOSION,
                    playerPos.x + offsetX,
                    playerPos.y + offsetY,
                    playerPos.z + offsetZ,
                    1, 0, 0, 0, 0.05
                );
            }
        }
    }
    
    private static void playSoundEffects(ServerPlayer player) {
        Vec3 playerPos = player.position();

        
        player.level().playSound(
            null,
            BlockPos.containing(playerPos),
            SoundEvents.GLASS_FALL,
            SoundSource.BLOCKS,
            0.8f,
            0.8f
        );
    }
    
    private static void extinguishPlayerFire(ServerPlayer player) {
        if (player.isOnFire()) {
    
            player.clearFire();
        }
    }
    
    private static void extinguishAreaFires(ServerPlayer player) {
        Vec3 playerPos = player.position();

        
        int firesExtinguished = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos firePos = new BlockPos(
                        (int) playerPos.x + x,
                        (int) playerPos.y + y,
                        (int) playerPos.z + z
                    );
                    
                    if (player.level().getBlockState(firePos).is(Blocks.FIRE)) {
                        player.level().removeBlock(firePos, false);
                        firesExtinguished++;
                    }
                }
            }
        }
        

    }
    
    private static void convertLavaToObsidian(ServerPlayer player) {
        Vec3 playerPos = player.position();

        
        int lavaConverted = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos lavaPos = new BlockPos(
                        (int) playerPos.x + x,
                        (int) playerPos.y + y,
                        (int) playerPos.z + z
                    );
                    
                    if (player.level().getBlockState(lavaPos).is(Blocks.LAVA)) {
                        player.level().setBlockAndUpdate(lavaPos, Blocks.OBSIDIAN.defaultBlockState());
                        lavaConverted++;
                    }
                }
            }
        }
        

    }

    private static void freezeAreaWater(ServerPlayer player) {
        Vec3 playerPos = player.position();


        int waterFrozen = 0;
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    BlockPos waterPos = new BlockPos(
                        (int) playerPos.x + x,
                        (int) playerPos.y + y,
                        (int) playerPos.z + z
                    );

                    if (player.level().getBlockState(waterPos).is(Blocks.WATER)) {
                        player.level().setBlockAndUpdate(waterPos, Blocks.ICE.defaultBlockState());
                        waterFrozen++;
                    }
                }
            }
        }


    }
} 