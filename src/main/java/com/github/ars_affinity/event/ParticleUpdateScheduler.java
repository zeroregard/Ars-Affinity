package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.ParticleEffectPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.lang.ref.WeakReference;

public class ParticleUpdateScheduler {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> activeUpdates = new ConcurrentHashMap<>();
    private static volatile boolean isShutdown = false;

    public static void startPositionUpdates(Player player, String schoolId) {
        if (isShutdown)
            return;
        if (!(player instanceof ServerPlayer serverPlayer))
            return; // this should only ever be run for ServerPlayers (which only exist serverside)
        String key = serverPlayer.getId() + "_" + schoolId;

        // DONT HOLD A HARD REFERENCE TO A PLAYER OBJECT OFF-THREAD! #memoryleaks
        // WeakReference#get will return the referenced object if it still exists, or
        // null if it has been garbage collected.
        // WeakReference allows garbage collection, preventing memory leaks
        WeakReference<ServerPlayer> playerRef = new WeakReference<>(serverPlayer);

        // Schedule position updates every 150ms (3 ticks at 20 TPS) for 3 seconds (60
        // ticks)
        // take the returned Future to allow cancellation of the indefinite, repeating
        // task
        ScheduledFuture<?> task = scheduler.scheduleAtFixedRate(() -> {
            if (activeUpdates.get(key) == null) {
                throw new RuntimeException("Task " + key + " was removed from update list without cancelling"); // This
                                                                                                                // will
                                                                                                                // also
                                                                                                                // cancel
                                                                                                                // the
                                                                                                                // task
            }
            ServerPlayer refPlayer = playerRef.get();
            if (refPlayer == null || refPlayer.hasDisconnected() || refPlayer.isRemoved() || !refPlayer.isAlive()) {
                ParticleUpdateScheduler.stopPositionUpdates(key);
                return; // Player is gone, stop updates
            }

            MinecraftServer server = refPlayer.getServer();
            if (server == null) {
                ParticleUpdateScheduler.stopPositionUpdates(key);
                return;
            } // player doesnt belong to a server.
              // this should probably throw but for now just cancel the task

            server.execute(() -> {
                ServerPlayer execPlayer = playerRef.get(); // if the player no longer exists by the time the server gets
                                                           // around to executing this, cancel the recurring task also
                if (execPlayer == null || execPlayer.hasDisconnected() || execPlayer.isRemoved()
                        || !execPlayer.isAlive()) {
                    ParticleUpdateScheduler.stopPositionUpdates(key);
                    return;
                }

                var pos = execPlayer.position();
                var updatePacket = new ParticleEffectPacket(
                        execPlayer.getId(),
                        schoolId,
                        pos.x,
                        pos.y,
                        pos.z);
                Networking.sendToNearbyClient(execPlayer.level(), execPlayer.blockPosition(), updatePacket);
                ArsAffinity.LOGGER.debug("Sent position update for player {}: ({}, {}, {})",
                        execPlayer.getName().getString(), pos.x, pos.y, pos.z);
            });
        }, 150, 150, TimeUnit.MILLISECONDS); // Start after 150ms, repeat every 150ms

        // store the new task, accepting the old stored task if there was one
        ScheduledFuture<?> oldTask = activeUpdates.put(key, task);
        if (oldTask != null) {
            // Cancel any existing updates for this player/school combination
            oldTask.cancel(false);
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

    // required to be able to cancel the task if the player or network id is no
    // longer in use
    public static void stopPositionUpdates(String key) {
        ScheduledFuture<?> task = activeUpdates.remove(key);
        if (task != null) {
            task.cancel(false);
        }
    }

    public static void shutdown() {
        if (isShutdown)
            return;
        isShutdown = true;

        for (ScheduledFuture<?> task : activeUpdates.values()) {
            task.cancel(false);
        }
        activeUpdates.clear();

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                ArsAffinity.LOGGER.warn("ParticleUpdateScheduler did not terminate within 5 seconds, forcing shutdown");
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(2, TimeUnit.SECONDS)) {
                    ArsAffinity.LOGGER.error("ParticleUpdateScheduler did not terminate after force shutdown");
                }
            }
        } catch (InterruptedException e) {
            ArsAffinity.LOGGER.warn("Interrupted while shutting down ParticleUpdateScheduler, forcing shutdown");
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        ;
    }
}
