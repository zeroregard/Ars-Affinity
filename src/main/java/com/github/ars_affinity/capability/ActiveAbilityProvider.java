package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ActiveAbilityProvider {
    
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "active_ability_data");
    
    private static final Map<UUID, ActiveAbilityData> playerDataCache = new HashMap<>();
    
    public static ActiveAbilityData getActiveAbilityData(Player player) {
        UUID playerId = player.getUUID();
        
        return playerDataCache.computeIfAbsent(playerId, id -> {
            ActiveAbilityData newData = new ActiveAbilityData();
            
            // Load from player persistent data
            CompoundTag playerData = player.getPersistentData();
            String dataKey = "ars_affinity:active_ability_data";
            
            if (playerData.contains(dataKey)) {
                CompoundTag abilityData = playerData.getCompound(dataKey);
                if (abilityData != null && !abilityData.isEmpty()) {
                    try {
                        newData.deserializeNBT(player.level().registryAccess(), abilityData);
                        ArsAffinity.LOGGER.debug("Loaded active ability data for player {}: {}", 
                            player.getName().getString(), 
                            newData.getActiveAbilityType());
                    } catch (Exception e) {
                        ArsAffinity.LOGGER.error("Failed to deserialize active ability data for player {}: {}", 
                            player.getName().getString(), e.getMessage(), e);
                    }
                } else {
                    ArsAffinity.LOGGER.debug("Empty active ability data found for player {}", player.getName().getString());
                }
            } else {
                ArsAffinity.LOGGER.debug("No existing active ability data found for player {}, creating new data", 
                    player.getName().getString());
            }
            
            return newData;
        });
    }
    
    public static void loadPlayerData(Player player) {
        getActiveAbilityData(player);
    }
    
    public static void savePlayerData(Player player) {
        UUID playerId = player.getUUID();
        ActiveAbilityData data = playerDataCache.get(playerId);
        
        if (data != null && data.isDirty()) {
            CompoundTag playerData = player.getPersistentData();
            String dataKey = "ars_affinity:active_ability_data";
            
            try {
                CompoundTag abilityData = data.serializeNBT(player.level().registryAccess());
                playerData.put(dataKey, abilityData);
                data.setDirty(false);
                
                ArsAffinity.LOGGER.debug("Saved active ability data for player {}: {}", 
                    player.getName().getString(),
                    data.getActiveAbilityType());
                    
            } catch (Exception e) {
                ArsAffinity.LOGGER.error("Failed to serialize active ability data for player {}: {}", 
                    player.getName().getString(), e.getMessage(), e);
            }
        }
    }
    
    public static void saveAllData() {
        ArsAffinity.LOGGER.debug("Saving all active ability data (cache size: {})", playerDataCache.size());
    }
    
    public static void clearCache() {
        ArsAffinity.LOGGER.debug("Clearing active ability data cache (size: {})", playerDataCache.size());
        playerDataCache.clear();
    }
    
    public static int getCacheSize() {
        return playerDataCache.size();
    }
}
