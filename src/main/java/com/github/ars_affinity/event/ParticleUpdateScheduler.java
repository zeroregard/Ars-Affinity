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

import java.lang.ref.WeakReference;

public class ParticleUpdateScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ConcurrentHashMap<String, ScheduledFuture> activeUpdates = new ConcurrentHashMap<>();

    public static void startPositionUpdates(Player player, String schoolId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return; // this should only ever be run for ServerPlayers (which only exist serverside)
        String key = serverPlayer.getId() + "_" + schoolId;
        
        // DONT HOLD A HARD REFERENCE TO A PLAYER OBJECT OFF-THREAD! #memoryleaks
        // WeakReference#get will return the referenced object if it still exists, or null if it has been garbage collected.
        // WeakReference allows garbage collection, preventing memory leaks
        WeakReference<ServerPlayer> playerRef = new WeakReference<>(serverPlayer);
        
        // Schedule position updates every 150ms (3 ticks at 20 TPS) for 3 seconds (60 ticks)
        // take the returned Future to allow cancellation of the indefinite, repeating task
        ScheduledFuture task = scheduler.scheduleAtFixedRate(() -> {
            if (activeUpdates.get(key) == null) {
                throw new ExecutionException("Task " + key + " was removed from update list without cancelling"); // This will also cancel the task
            }
            ServerPlayer player = playerRef.get();
            if (player == null || player.hasDisconnected() || player.isRemoved() || !player.isAlive()) {
                ParticleUpdateScheduler.stopPositionUpdates(key);
                return; // Player is gone, stop updates
            }
            
            MinecraftServer server = player.getServer();
            if (server == null) {
                ParticleUpdateScheduler.stopPositionUpdates(key);
                return;
            } // player doesnt belong to a server. this should probably throw but for now just cancel
            
            server.execute(() -> {

                ServerPlayer player = playerRef.get(); // if the player no longer exists by the time the server gets around to executing this, cancel the recurring task also
                if (player == null || player.hasDisconnected() || player.isRemoved() || !player.isAlive()) {
                    ParticleUpdateScheduler.stopPositionUpdates(key);
                    return;
                }

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
            });
        }, 150, 150, TimeUnit.MILLISECONDS); // Start after 150ms, repeat every 150ms

        // store the new task, accepting the old stored task if there was one
        ScheduledFuture oldTask = activeUpdates.put(key, task); 
        if (oldTask != null) {
            // Cancel any existing updates for this player/school combination
            oldTask.cancel();
        }

        // Stop updates after 3 seconds (60 ticks)
        scheduler.schedule(() -> {
            ParticleUpdateScheduler.stopPositionUpdates(key);
        }, 3000, TimeUnit.MILLISECONDS);
    }
    
    public static void stopPositionUpdates(Player player, String schoolId) {
        String key = player.getId() + "_" + schoolId;
        stopPositionUpdates(key);
    }
    // required to be able to cancel the task if the player or network id is no longer in use
    public static void stopPositionUpdates(String key) {
        ScheduledFuture task = activeUpdates.remove(key);
        if (task != null) {
            task.cancel();
        }
    }
}
