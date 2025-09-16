package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WetTicksProvider {
    
    public static final ResourceLocation IDENTIFIER = ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "wet_ticks");
    
    private static final Map<UUID, WetTicks> playerWetTicksCache = new HashMap<>();
    
    public static WetTicks getWetTicks(Player player) {
        UUID playerId = player.getUUID();
        
        return playerWetTicksCache.computeIfAbsent(playerId, id -> {
            WetTicks newWetTicks = new WetTicks();
            CompoundTag playerData = player.getPersistentData();
            if (playerData.contains(IDENTIFIER.toString())) {
                CompoundTag wetTicksData = playerData.getCompound(IDENTIFIER.toString());
                newWetTicks.deserializeNBT(wetTicksData);
            }
            return newWetTicks;
        });
    }
    
    public static void loadPlayerWetTicks(Player player) {
        // This will trigger the loading logic in getWetTicks
        getWetTicks(player);
    }
    
    public static void savePlayerWetTicks(Player player) {
        UUID playerId = player.getUUID();
        WetTicks wetTicks = playerWetTicksCache.get(playerId);
        
        if (wetTicks != null) {
            CompoundTag playerData = player.getPersistentData();
            CompoundTag wetTicksData = wetTicks.serializeNBT();
            playerData.put(IDENTIFIER.toString(), wetTicksData);
        }
    }
    
    public static void saveAllWetTicks() {
        // Note: We can't save all wet ticks without Player objects, but the cache will be cleared anyway
    }
    
    public static void clearCache() {
        playerWetTicksCache.clear();
    }
    
    public static int getCacheSize() {
        return playerWetTicksCache.size();
    }
}
