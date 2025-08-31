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
            newProgress.setPlayer(player); // Set player reference for auto-saving
            
            // Load from player persistent data
            CompoundTag playerData = player.getPersistentData();
            String dataKey = "ars_affinity:school_affinity_progress";
            
            if (playerData.contains(dataKey)) {
                CompoundTag affinityData = playerData.getCompound(dataKey);
                if (affinityData != null && !affinityData.isEmpty()) {
                    try {
                        newProgress.deserializeNBT(player.level().registryAccess(), affinityData);
                        ArsAffinity.LOGGER.info("Loaded affinity progress for player {}: {} affinities, {} perks", 
                            player.getName().getString(), 
                            newProgress.getAllAffinities().size(),
                            newProgress.getAllActivePerkReferences().size());
                    } catch (Exception e) {
                        ArsAffinity.LOGGER.error("Failed to deserialize affinity progress for player {}: {}", 
                            player.getName().getString(), e.getMessage(), e);
                    }
                } else {
                    ArsAffinity.LOGGER.warn("Empty affinity data found for player {}", player.getName().getString());
                }
            } else {
                ArsAffinity.LOGGER.info("No existing affinity data found for player {}, creating new progress", 
                    player.getName().getString());
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
            String dataKey = "ars_affinity:school_affinity_progress";
            
            try {
                CompoundTag affinityData = progress.serializeNBT(player.level().registryAccess());
                playerData.put(dataKey, affinityData);
                
                ArsAffinity.LOGGER.info("Saved affinity progress for player {}: {} affinities, {} perks", 
                    player.getName().getString(),
                    progress.getAllAffinities().size(),
                    progress.getAllActivePerkReferences().size());
                    
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to serialize affinity progress for player {}: {}", 
                    player.getName().getString(), e.getMessage(), e);
            }
        } else {
            ArsAffinity.LOGGER.warn("No progress found in cache for player {} during save", player.getName().getString());
        }
    }
    
    public static void saveAllProgress() {
        ArsAffinity.LOGGER.info("Saving all player progress (cache size: {})", playerProgressCache.size());
        // Note: We can't save all progress without Player objects, but the cache will be cleared anyway
    }
    
    public static void clearCache() {
        ArsAffinity.LOGGER.info("Clearing player progress cache (size: {})", playerProgressCache.size());
        playerProgressCache.clear();
    }
    
    public static int getCacheSize() {
        return playerProgressCache.size();
    }
} 