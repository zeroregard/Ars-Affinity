package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Random;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveUnstableSummoningEvents {
    
    private static final Random RANDOM = new Random();
    
    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.shooter instanceof Player player)) return;
        if (event.world.isClientSide()) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            int conjurationTier = progress.getTier(SpellSchools.CONJURATION);
            if (conjurationTier > 0) {
                AffinityPerkHelper.applyHighestTierPerk(progress, conjurationTier, SpellSchools.CONJURATION, AffinityPerkType.PASSIVE_UNSTABLE_SUMMONING, perk -> {
                    if (perk instanceof AffinityPerk.UnstableSummoningPerk unstablePerk) {
                        // Check if the perk should trigger based on chance
                        if (RANDOM.nextFloat() < unstablePerk.chance) {
                            LivingEntity originalEntity = event.summon.getLivingEntity();
                            if (originalEntity != null && !unstablePerk.entities.isEmpty()) {
                                // Get the original entity's position and level
                                Level level = originalEntity.level();
                                double x = originalEntity.getX();
                                double y = originalEntity.getY();
                                double z = originalEntity.getZ();
                                
                                // Remove the original entity
                                originalEntity.discard();
                                
                                // Select a random entity from the configurable list
                                String randomEntityId = unstablePerk.entities.get(RANDOM.nextInt(unstablePerk.entities.size()));
                                java.util.Optional<EntityType<?>> entityTypeOpt = EntityType.byString(randomEntityId);
                                
                                if (entityTypeOpt.isPresent()) {
                                    EntityType<?> entityType = entityTypeOpt.get();
                                    // Create the new entity
                                    Entity newEntity = entityType.create(level);
                                    if (newEntity instanceof LivingEntity livingEntity) {
                                        livingEntity.setPos(x, y, z);
                                        
                                        // Prevent XP and item drops for mobs
                                        if (livingEntity instanceof net.minecraft.world.entity.Mob mob) {
                                            mob.setPersistenceRequired();
                                        }
                                        livingEntity.setCustomName(net.minecraft.network.chat.Component.literal("Unstable Summon"));
                                        
                                        // Spawn the entity
                                        level.addFreshEntity(livingEntity);
                                        
                                        ArsAffinity.LOGGER.info("Player {} triggered UNSTABLE_SUMMONING perk ({}%) - transformed summon into {}", 
                                            player.getName().getString(), (int)(unstablePerk.chance * 100), randomEntityId);
                                    }
                                } else {
                                    ArsAffinity.LOGGER.warn("Invalid entity type for UNSTABLE_SUMMONING perk: {}", randomEntityId);
                                }
                            }
                        }
                    }
                });
            }
        }
    }
}