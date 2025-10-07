package com.github.ars_affinity.client.sound;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public class LoopingSoundManager {
    private static final Map<String, PlayerLoopingSound> activeSounds = new HashMap<>();
    
    public static void startLoopingSound(Player player, String soundId) {
        String key = player.getId() + "_" + soundId;
        
        if (activeSounds.containsKey(key)) {
            return;
        }
        
        SoundEvent soundEvent = getSoundEvent(soundId);
        if (soundEvent == null) {
            ArsAffinity.LOGGER.warn("Unknown sound ID: {}", soundId);
            return;
        }
        
        PlayerLoopingSound sound = new PlayerLoopingSound(player, soundEvent);
        activeSounds.put(key, sound);
        Minecraft.getInstance().getSoundManager().play(sound);
        
        ArsAffinity.LOGGER.info("Started looping sound {} for player {}", soundId, player.getName().getString());
    }
    
    public static void stopLoopingSound(Player player, String soundId) {
        String key = player.getId() + "_" + soundId;
        
        PlayerLoopingSound sound = activeSounds.remove(key);
        if (sound != null) {
            sound.stopSound();
            ArsAffinity.LOGGER.info("Stopped looping sound {} for player {}", soundId, player.getName().getString());
        }
    }
    
    public static void stopAllSounds(Player player) {
        activeSounds.entrySet().removeIf(entry -> {
            if (entry.getKey().startsWith(player.getId() + "_")) {
                entry.getValue().stopSound();
                return true;
            }
            return false;
        });
    }
    
    private static SoundEvent getSoundEvent(String soundId) {
        return switch (soundId) {
            case "curse_field" -> ModSounds.CURSE_FIELD.get();
            case "sanctuary" -> ModSounds.SANCTUARY.get();
            default -> null;
        };
    }
    
    private static class PlayerLoopingSound extends AbstractTickableSoundInstance {
        private final Player player;
        private boolean stopped = false;
        
        public PlayerLoopingSound(Player player, SoundEvent soundEvent) {
            super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.5f;
            this.pitch = 1.0f;
            this.attenuation = Attenuation.LINEAR;
            updatePosition();
        }
        
        @Override
        public void tick() {
            if (stopped || !player.isAlive()) {
                stopSound();
                return;
            }
            updatePosition();
        }
        
        private void updatePosition() {
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }
        
        @Override
        public boolean canStartSilent() {
            return true;
        }
        
        public void stopSound() {
            this.stopped = true;
            this.looping = false;
            this.volume = 0;
        }
    }
}
