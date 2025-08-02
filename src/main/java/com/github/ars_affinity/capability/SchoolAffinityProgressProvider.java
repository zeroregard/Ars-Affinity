package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SchoolAffinityProgressProvider {
    
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "school_affinity_progress");
    
    private static final Map<UUID, SchoolAffinityProgress> playerProgressCache = new HashMap<>();
    
    public static SchoolAffinityProgress getAffinityProgress(Player player) {
        UUID playerId = player.getUUID();
        
        return playerProgressCache.computeIfAbsent(playerId, id -> {
            SchoolAffinityProgress newProgress = new SchoolAffinityProgress();
            newProgress.setPlayer(player); // Set player reference for cache invalidation
            CompoundTag playerData = player.getPersistentData();
            if (playerData.contains(IDENTIFIER.toString())) {
                CompoundTag affinityData = playerData.getCompound(IDENTIFIER.toString());
                newProgress.deserializeNBT(null, affinityData);
            }
            return newProgress;
        });
    }
    
    public static void loadPlayerProgress(Player player) {
        // This will trigger the loading logic in getAffinityProgress
        getAffinityProgress(player);
    }
    
    public static void savePlayerProgress(Player player) {
        UUID playerId = player.getUUID();
        SchoolAffinityProgress progress = playerProgressCache.get(playerId);
        
        if (progress != null) {
            CompoundTag playerData = player.getPersistentData();
            CompoundTag affinityData = progress.serializeNBT(null);
            playerData.put(IDENTIFIER.toString(), affinityData);
        }
    }
    
    public static void saveAllProgress() {
        // Note: We can't save all progress without Player objects, but the cache will be cleared anyway
    }
    
    public static void clearCache() {
        playerProgressCache.clear();
    }
    
    public static int getCacheSize() {
        return playerProgressCache.size();
    }
} 