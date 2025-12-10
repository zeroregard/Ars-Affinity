package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.SyncPlayerAffinityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
                        ArsAffinity.LOGGER.debug("Loaded player affinity data for player {}: {} school points", 
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
                ArsAffinity.LOGGER.debug("No existing affinity data found for player {}, creating new data", 
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
                
                ArsAffinity.LOGGER.debug("Saved player affinity data for player {}: {} school points", 
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
    
    public static void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerAffinityData data = getPlayerAffinityData(player);
            if (data != null) {
                SyncPlayerAffinityDataPacket syncPacket = 
                    new SyncPlayerAffinityDataPacket(data, player);
                Networking.sendToPlayerClient(syncPacket, serverPlayer);
                ArsAffinity.LOGGER.debug("Synced affinity data to client for player {}", player.getName().getString());
            }
        }
    }
    
    public static void saveAllData() {
        ArsAffinity.LOGGER.debug("Saving all player data (cache size: {})", playerDataCache.size());
    }
    
    public static void clearCache() {
        ArsAffinity.LOGGER.debug("Clearing player data cache (size: {})", playerDataCache.size());
        playerDataCache.clear();
    }
    
    public static int getCacheSize() {
        return playerDataCache.size();
    }
    
    public static void removePlayerData(UUID playerId) {
        PlayerAffinityData removed = playerDataCache.remove(playerId);
        if (removed != null) {
            removed.clearPlayer();
            ArsAffinity.LOGGER.debug("Removed player affinity data from cache for UUID: {}", playerId);
        }
    }
    
    public static void removePlayerData(Player player) {
        removePlayerData(player.getUUID());
    }
}
