package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MobPacificationEvents {

    // Cache for mob pacification perks per player
    private static final Map<UUID, Set<String>> playerMobPacificationCache = new HashMap<>();

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // Only handle when a mob is trying to target a player
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        LivingEntity mob = event.getEntity();
        EntityType<?> type = mob.getType();
        String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        
        if (shouldPacifyMob(player, mobId)) {
            event.setCanceled(true);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        playerMobPacificationCache.remove(event.getEntity().getUUID());
    }
    
    @SubscribeEvent
    public static void onTierChange(TierChangeEvent event) {
        if (event.hasTierChanged()) {
            playerMobPacificationCache.remove(event.getPlayer().getUUID());
        }
    }
    
    private static boolean shouldPacifyMob(Player player, String mobId) {
        Set<String> pacifiedMobs = getPassiveMobPerks(player);
        return pacifiedMobs.contains(mobId);
    }
    
    private static Set<String> getPassiveMobPerks(Player player) {
        UUID playerId = player.getUUID();
        
        if (!playerMobPacificationCache.containsKey(playerId)) {
            updatePlayerMobPacificationCache(player);
        }
        
        return playerMobPacificationCache.getOrDefault(playerId, new HashSet<>());
    }
    
    private static void updatePlayerMobPacificationCache(Player player) {
        UUID playerId = player.getUUID();
        Set<String> pacifiedMobs = new HashSet<>();
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            playerMobPacificationCache.put(playerId, pacifiedMobs);
            return;
        }
        
        // Check all schools for mob pacification perks
        AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_MOB_PACIFICATION, perk -> {
            if (perk instanceof AffinityPerk.EntityBasedPerk entityPerk) {
                if (entityPerk.entities != null) {
                    pacifiedMobs.addAll(entityPerk.entities);
                }
            }
        });
        
        playerMobPacificationCache.put(playerId, pacifiedMobs);
    }
} 