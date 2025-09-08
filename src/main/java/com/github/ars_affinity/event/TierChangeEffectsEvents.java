package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.ParticleEffectPacket;
import com.github.ars_affinity.registry.ModSounds;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class TierChangeEffectsEvents {
    
    // Map of schools to their corresponding tier change sounds
    private static final Map<SpellSchool, SoundEvent> SCHOOL_SOUNDS = new HashMap<>();
    private static final Map<Integer, SoundEvent> TIER_SOUNDS = new HashMap<>();
    
    // Map of schools to their corresponding colors
    private static final Map<SpellSchool, String> SCHOOL_COLORS = new HashMap<>();
    
    static {
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_FIRE, ModSounds.TIER_CHANGE_FIRE.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_WATER, ModSounds.TIER_CHANGE_WATER.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_EARTH, ModSounds.TIER_CHANGE_EARTH.get());
        SCHOOL_SOUNDS.put(SpellSchools.ELEMENTAL_AIR, ModSounds.TIER_CHANGE_AIR.get());
        SCHOOL_SOUNDS.put(SpellSchools.ABJURATION, ModSounds.TIER_CHANGE_ABJURATION.get());
        SCHOOL_SOUNDS.put(SpellSchools.CONJURATION, ModSounds.TIER_CHANGE_CONJURATION.get());
        SCHOOL_SOUNDS.put(SpellSchools.NECROMANCY, ModSounds.TIER_CHANGE_NECROMANCY.get());
        SCHOOL_SOUNDS.put(SpellSchools.MANIPULATION, ModSounds.TIER_CHANGE_MANIPULATION.get());
    }

    static {
        TIER_SOUNDS.put(1, ModSounds.TIER_CHANGE_ONE.get());
        TIER_SOUNDS.put(2, ModSounds.TIER_CHANGE_TWO.get());
        TIER_SOUNDS.put(3, ModSounds.TIER_CHANGE_THREE.get());
    }
    
    static {
        // Define colors for each school using Minecraft color codes
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_FIRE, "§c");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_WATER, "§9");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_EARTH, "§a");
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_AIR, "§e");
        SCHOOL_COLORS.put(SpellSchools.ABJURATION, "§d");
        SCHOOL_COLORS.put(SpellSchools.CONJURATION, "§b");
        SCHOOL_COLORS.put(SpellSchools.NECROMANCY, "§8"); 
        SCHOOL_COLORS.put(SpellSchools.MANIPULATION, "§6"); 
    }
    
    @SubscribeEvent
    public static void onTierChange(TierChangeEvent event) {
        ArsAffinity.LOGGER.info("=== TIER CHANGE EVENT START ===");
        ArsAffinity.LOGGER.info("TierChangeEvent received: hasChanged={}, player={}, school={}, oldTier={}, newTier={}", 
            event.hasTierChanged(), event.getPlayer().getName().getString(), event.getSchool().getId(), 
            event.getOldTier(), event.getNewTier());
        ArsAffinity.LOGGER.info("Player position: ({}, {}, {})", 
            event.getPlayer().getX(), event.getPlayer().getY(), event.getPlayer().getZ());
        ArsAffinity.LOGGER.info("Player level isClientSide: {}", event.getPlayer().level().isClientSide());
            
        if (!event.hasTierChanged()) {
            ArsAffinity.LOGGER.info("TierChangeEvent: No tier change detected, skipping");
            ArsAffinity.LOGGER.info("=== TIER CHANGE EVENT END (NO CHANGE) ===");
            return;
        }
        
        Player player = event.getPlayer();
        SpellSchool school = event.getSchool();
        int oldTier = event.getOldTier();
        int newTier = event.getNewTier();
        
        // Only handle server-side events
        if (player.level().isClientSide()) {
            ArsAffinity.LOGGER.info("TierChangeEvent: Client-side event, skipping");
            ArsAffinity.LOGGER.info("=== TIER CHANGE EVENT END (CLIENT SIDE) ===");
            return;
        }
        
        ArsAffinity.LOGGER.info("TierChangeEvent: Server-side event, processing tier change from {} to {}", oldTier, newTier);

        if (newTier > oldTier) {
            ArsAffinity.LOGGER.info("TierChangeEvent: Tier increased, spawning effects");
            playTierChangeSound(player, school, newTier);
            sendTierChangeMessage(player, school, newTier);
            spawnTierChangeParticles(player, school, newTier);
        } else {
            ArsAffinity.LOGGER.info("TierChangeEvent: Tier did not increase, skipping effects");
        }
        
        SchoolAffinityProgressHelper.getAffinityProgress(player).rebuildPerkIndex();
        ArsAffinity.LOGGER.info("=== TIER CHANGE EVENT END ===");
    }
    
    private static void playTierChangeSound(Player player, SpellSchool school, int tier) {
        SoundEvent tierSound = TIER_SOUNDS.get(tier);
        if (tierSound == null) {
            ArsAffinity.LOGGER.warn("No tier sound found for tier: " + tier);
            return;
        }

        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                tierSound,
                SoundSource.BLOCKS,
                1.0f,
                1.0f
        );

        SoundEvent schoolSound = SCHOOL_SOUNDS.get(school);
        if (schoolSound == null) {
            ArsAffinity.LOGGER.warn("No sound found for school: " + school.getId());
            return;
        }
        
        // Play sound at player location for all nearby players to hear
        player.level().playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                schoolSound,
                SoundSource.BLOCKS,
                1.0f, // Volume
                1.0f  // Pitch
        );
    }
    
    private static void sendTierChangeMessage(Player player, SpellSchool school, int newTier) {
        // Get the school color, default to white if not found
        String schoolColor = SCHOOL_COLORS.getOrDefault(school, "§f");
        
        // Create the colored school name component
        Component schoolName = Component.literal(schoolColor + school.getTextComponent().getString() + "§r");
        Component tierText = Component.literal("Tier " + newTier);
        
        // Create the full message: "Your affinity towards [Colored School] changed to [Tier]"
        Component message = Component.translatable(
            "ars_affinity.tier_change.message",
            schoolName,
            tierText
        );
        
        // Send the message to the player
        player.sendSystemMessage(message);
    }
    
    private static void spawnTierChangeParticles(Player player, SpellSchool school, int tier) {
        ArsAffinity.LOGGER.info("=== SPAWNING TIER CHANGE PARTICLES ===");
        ArsAffinity.LOGGER.info("spawnTierChangeParticles called for player {} school {} tier {}", 
            player.getName().getString(), school.getId(), tier);
        ArsAffinity.LOGGER.info("Player position: ({}, {}, {})", 
            player.getX(), player.getY(), player.getZ());
        ArsAffinity.LOGGER.info("Player level: {}, isClientSide: {}", 
            player.level().dimension().location(), player.level().isClientSide());
            
        // Calculate particle count based on tier (more particles for higher tiers)
        int particleCount = 8 + (tier * 5); // 13, 18, 23 particles for tiers 1, 2, 3
        
        ArsAffinity.LOGGER.info("Calculated particle count: {} (base 8 + tier {} * 5)", particleCount, tier);
        ArsAffinity.LOGGER.info("Creating ParticleEffectPacket with playerId={}, schoolId={}, particleCount={}", 
            player.getId(), school.getId().toString(), particleCount);
        
        // Send particle effect packet to all clients
        ParticleEffectPacket packet = new ParticleEffectPacket(
            player.getId(),
            school.getId().toString(),
            particleCount
        );
        
        ArsAffinity.LOGGER.info("ParticleEffectPacket created successfully");
        ArsAffinity.LOGGER.info("Sending particle packet to nearby clients at position: {}", player.blockPosition());
        
        try {
            Networking.sendToNearbyClient(player.level(), player.blockPosition(), packet);
            ArsAffinity.LOGGER.info("Particle packet sent successfully to nearby clients");
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to send particle packet: {}", e.getMessage(), e);
        }
        
        ArsAffinity.LOGGER.info("Spawning {} spiral particles for {} tier change to tier {}", 
            particleCount, school.getId(), tier);
        ArsAffinity.LOGGER.info("=== PARTICLE SPAWNING COMPLETE ===");
    }
}