package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.ParticleEffectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ParticleUpdateScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ConcurrentHashMap<String, Boolean> activeUpdates = new ConcurrentHashMap<>();
    
    public static void startPositionUpdates(Player player, String schoolId) {
        String key = player.getId() + "_" + schoolId;
        
        // Cancel any existing updates for this player/school combination
        activeUpdates.put(key, true);
        
        // Schedule position updates every 150ms (3 ticks at 20 TPS) for 3 seconds (60 ticks)
        scheduler.scheduleAtFixedRate(() -> {
            if (!activeUpdates.getOrDefault(key, false)) {
                return; // Update was cancelled
            }
            
            if (player.isRemoved() || !player.isAlive()) {
                activeUpdates.remove(key);
                return; // Player is gone, stop updates
            }
            
            MinecraftServer server = player.getServer();
            if (server != null) {
                server.execute(() -> {
                    if (player.isAlive() && !player.isRemoved()) {
                        var pos = player.position();
                        var updatePacket = new ParticleEffectPacket(
                            player.getId(),
                            schoolId,
                            pos.x,
                            pos.y,
                            pos.z
                        );
                        Networking.sendToNearbyClient(player.level(), player.blockPosition(), updatePacket);
                        ArsAffinity.LOGGER.debug("Sent position update for player {}: ({}, {}, {})", 
                            player.getName().getString(), pos.x, pos.y, pos.z);
                    }
                });
            }
        }, 150, 150, TimeUnit.MILLISECONDS); // Start after 150ms, repeat every 150ms
        
        // Stop updates after 3 seconds (60 ticks)
        scheduler.schedule(() -> {
            activeUpdates.remove(key);
        }, 3000, TimeUnit.MILLISECONDS);
    }
    
    public static void stopPositionUpdates(Player player, String schoolId) {
        String key = player.getId() + "_" + schoolId;
        activeUpdates.remove(key);
    }
}