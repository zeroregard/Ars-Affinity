package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerAffinityDataProvider {
    
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "player_affinity_data");
    
    private static final Map<UUID, PlayerAffinityData> playerDataCache = new HashMap<>();
    
    public static PlayerAffinityData getPlayerAffinityData(Player player) {
        UUID playerId = player.getUUID();
        
        return playerDataCache.computeIfAbsent(playerId, id -> {
            PlayerAffinityData newData = new PlayerAffinityData();
            newData.setPlayer(player);
            
            // Load from player persistent data
            CompoundTag playerData = player.getPersistentData();
            String dataKey = "ars_affinity:player_affinity_data";
            
            if (playerData.contains(dataKey)) {
                CompoundTag affinityData = playerData.getCompound(dataKey);
                if (affinityData != null && !affinityData.isEmpty()) {
                    try {
                        newData.deserializeNBT(player.level().registryAccess(), affinityData);
                        ArsAffinity.LOGGER.info("Loaded player affinity data for player {}: {} school points", 
                            player.getName().getString(), 
                            newData.getAllSchoolPoints().size());
                    } catch (Exception e) {
                        ArsAffinity.LOGGER.error("Failed to deserialize player affinity data for player {}: {}", 
                            player.getName().getString(), e.getMessage(), e);
                    }
                } else {
                    ArsAffinity.LOGGER.warn("Empty affinity data found for player {}", player.getName().getString());
                }
            } else {
                ArsAffinity.LOGGER.info("No existing affinity data found for player {}, creating new data", 
                    player.getName().getString());
            }
            
            return newData;
        });
    }
    
    public static void loadPlayerData(Player player) {
        getPlayerAffinityData(player);
    }
    
    public static void savePlayerData(Player player) {
        UUID playerId = player.getUUID();
        PlayerAffinityData data = playerDataCache.get(playerId);
        
        if (data != null) {
            CompoundTag playerData = player.getPersistentData();
            String dataKey = "ars_affinity:player_affinity_data";
            
            try {
                CompoundTag affinityData = data.serializeNBT(player.level().registryAccess());
                playerData.put(dataKey, affinityData);
                
                ArsAffinity.LOGGER.info("Saved player affinity data for player {}: {} school points", 
                    player.getName().getString(),
                    data.getAllSchoolPoints().size());
                    
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to serialize player affinity data for player {}: {}", 
                    player.getName().getString(), e.getMessage(), e);
            }
        } else {
            ArsAffinity.LOGGER.warn("No data found in cache for player {} during save", player.getName().getString());
        }
    }
    
    public static void saveAllData() {
        ArsAffinity.LOGGER.info("Saving all player data (cache size: {})", playerDataCache.size());
    }
    
    public static void clearCache() {
        ArsAffinity.LOGGER.info("Clearing player data cache (size: {})", playerDataCache.size());
        playerDataCache.clear();
    }
    
    public static int getCacheSize() {
        return playerDataCache.size();
    }
}
