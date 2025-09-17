package com.github.ars_affinity.capability;

import net.minecraft.world.entity.player.Player;

public class PlayerAffinityDataHelper {
    
    public static PlayerAffinityData getPlayerAffinityData(Player player) {
        return PlayerAffinityDataProvider.getPlayerAffinityData(player);
    }
    
    public static PlayerAffinityData getPlayerAffinityDataOrThrow(Player player) {
        return getPlayerAffinityData(player);
    }
    
    public static void loadPlayerData(Player player) {
        PlayerAffinityDataProvider.loadPlayerData(player);
    }
    
    public static void savePlayerData(Player player) {
        PlayerAffinityDataProvider.savePlayerData(player);
    }
}
