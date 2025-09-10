package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.event.EventQueue;
import com.hollingsworth.arsnouveau.api.event.ITimedEvent;
import com.hollingsworth.arsnouveau.client.particle.ParticleUtil;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

import java.util.Random;

public class PassiveUnstableSummoningEvents {
    
    private static final Random RANDOM = new Random();
    
    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.context.getCaster() instanceof PlayerCaster playerCaster)) {
            return;
        }
        var player = playerCaster.player;
        if (event.world.isClientSide()) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_UNSTABLE_SUMMONING, AffinityPerk.UnstableSummoningPerk.class, unstablePerk -> {
            if (RANDOM.nextFloat() < unstablePerk.chance) {
                LivingEntity originalEntity = event.summon.getLivingEntity();
                if (originalEntity != null && !unstablePerk.entities.isEmpty()) {
                    Level level = originalEntity.level();
                    double x = originalEntity.getX();
                    double y = originalEntity.getY();
                    double z = originalEntity.getZ();
                    
                    originalEntity.discard();
                    
                    String randomEntityId = unstablePerk.entities.get(RANDOM.nextInt(unstablePerk.entities.size()));
                    java.util.Optional<EntityType<?>> entityTypeOpt = EntityType.byString(randomEntityId);
                    
                    if (entityTypeOpt.isPresent()) {
                        EntityType<?> entityType = entityTypeOpt.get();
                        Entity newEntity = entityType.create(level);
                        if (newEntity instanceof LivingEntity livingEntity) {
                            livingEntity.setPos(x, y, z);
                            livingEntity.addTag("unstable_summon");
                            livingEntity.setCustomName(net.minecraft.network.chat.Component.literal("Unstable Summon"));
                            level.addFreshEntity(livingEntity);

                            if (level instanceof ServerLevel) {
                                UnstableSummonTimer timer = new UnstableSummonTimer(livingEntity, 20 * 60);
                                EventQueue.getServerInstance().addEvent(timer);
                            }
                            
                            ArsAffinity.LOGGER.info("Player {} triggered UNSTABLE_SUMMONING perk ({}%) - transformed summon into {}", 
                                player.getName().getString(), (int)(unstablePerk.chance * 100), randomEntityId);
                        }
                    } else {
                        ArsAffinity.LOGGER.warn("Invalid entity type for UNSTABLE_SUMMONING perk: {}", randomEntityId);
                    }
                }
            }
        });
        
    }
    
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity().getTags().contains("unstable_summon")) {
            event.getDrops().clear();
        }
    }

    @SubscribeEvent
    public static void onLivingExperience(LivingExperienceDropEvent event) {
        if (event.getEntity().getTags().contains("unstable_summon")) {
            event.setDroppedExperience(0);
        }
    }
    
    private static class UnstableSummonTimer implements ITimedEvent {
        private final LivingEntity entity;
        private int ticksLeft;
        
        public UnstableSummonTimer(LivingEntity entity, int duration) {
            this.entity = entity;
            this.ticksLeft = duration;
        }
        
        @Override
        public void tick(boolean serverSide) {
            if (!serverSide) return;
            
            ticksLeft--;
            if (ticksLeft <= 0) {
                if (!entity.isRemoved()) {
                    Level level = entity.level();
                    if (level instanceof ServerLevel serverLevel) {
                        ParticleUtil.spawnPoof(serverLevel, entity.blockPosition());
                    }
                    entity.discard();
                }
            }
        }
        
        @Override
        public boolean isExpired() {
            return ticksLeft <= 0 || entity.isRemoved();
        }
    }
}