package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.common.network.Networking;
import com.github.ars_affinity.common.network.ParticleEffectPacket;
import com.github.ars_affinity.registry.ModSounds;
import com.github.ars_affinity.util.SchoolColors;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.common.util.PortUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class SchoolAffinityPointAllocatedEvents {
    
    // Map of schools to their corresponding point allocation sounds
    private static final Map<SpellSchool, SoundEvent> SCHOOL_SOUNDS = new HashMap<>();
    
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
    
    @SubscribeEvent
    public static void onPointAllocated(SchoolAffinityPointAllocatedEvent event) {
        if (!event.hasPointsGained()) {
            return;
        }
        
        Player player = event.getPlayer();
        SpellSchool school = event.getSchool();
        int pointsGained = event.getPointsGained();
        
        // Only handle server-side events
        if (player.level().isClientSide()) {
            return;
        }
        
        sendPointAllocatedMessage(player, school, pointsGained);
        playPointAllocatedSound(player, school);
        spawnPointAllocatedParticles(player, school, pointsGained);
    }
    
    private static void sendPointAllocatedMessage(Player player, SpellSchool school, int pointsGained) {
        // Get the school's color from our utility
        int hexColor = SchoolColors.getHexColor(school);
        
        // Convert hex color to ChatFormatting
        ChatFormatting color = getChatFormattingFromHex(hexColor);
        
        // Create the message with colored school name
        String schoolDisplayName = getSchoolDisplayName(school);
        Component schoolName = Component.literal(schoolDisplayName)
            .withStyle(color);
        
        String messageText = String.format("Gained %d %s affinity point%s!", 
            pointsGained, 
            schoolDisplayName, 
            pointsGained == 1 ? "" : "s");
        
        Component message = Component.literal("Gained " + pointsGained + " ")
            .append(schoolName)
            .append(Component.literal(" affinity point" + (pointsGained == 1 ? "" : "s") + "!"));
        
        // Send the message to the player
        PortUtil.sendMessage(player, message);
        
        ArsAffinity.LOGGER.debug("Sent point allocation message to player {}: {} points in {} school", 
            player.getName().getString(), pointsGained, school.getId());
    }
    
    private static ChatFormatting getChatFormattingFromHex(int hexColor) {
        // Convert hex colors to appropriate ChatFormatting
        // Using the colors from SchoolColors utility
        return switch (hexColor) {
            case 0xFFf06666 -> ChatFormatting.RED;      // Fire
            case 0xFF82a2ed -> ChatFormatting.BLUE;     // Water  
            case 0xFF62e296 -> ChatFormatting.GREEN;    // Earth
            case 0xFFd4cf5a -> ChatFormatting.YELLOW;   // Air
            case 0xFFFF8800 -> ChatFormatting.GOLD;     // Manipulation
            case 0xFFeb7cce -> ChatFormatting.LIGHT_PURPLE; // Abjuration
            case 0xFF6d6d6d -> ChatFormatting.DARK_GRAY; // Necromancy
            case 0xFF6ae3ce -> ChatFormatting.AQUA;     // Conjuration
            default -> ChatFormatting.WHITE;
        };
    }
    
    /**
     * Gets the display name for a spell school for chat messages.
     * Necromancy is displayed as "anima" instead of "necromancy".
     */
    private static String getSchoolDisplayName(SpellSchool school) {
        if (school == SpellSchools.NECROMANCY) {
            return "anima";
        }
        return school.getId().toString().replaceAll("_", " ");
    }
    
    private static void playPointAllocatedSound(Player player, SpellSchool school) {
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

    private static void spawnPointAllocatedParticles(Player player, SpellSchool school, int pointsGained) {
        ArsAffinity.LOGGER.debug("=== SPAWNING POINT ALLOCATED PARTICLES ===");
        ArsAffinity.LOGGER.debug("spawnPointAllocatedParticles called for player {} school {} points {}", 
            player.getName().getString(), school.getId(), pointsGained);
        ArsAffinity.LOGGER.debug("Player position: ({}, {}, {})", 
            player.getX(), player.getY(), player.getZ());
        ArsAffinity.LOGGER.debug("Player level: {}, isClientSide: {}", 
            player.level().dimension().location(), player.level().isClientSide());
            
        // Calculate particle count based on points gained (reduced count and speed)
        int particleCount = 3 + (pointsGained * 2); // 5, 7, 9 particles for 1, 2, 3 points (half of original)
        
        ArsAffinity.LOGGER.debug("Calculated particle count: {} (base 5 + points {} * 3)", particleCount, pointsGained);
        ArsAffinity.LOGGER.debug("Creating ParticleEffectPacket with playerId={}, schoolId={}, particleCount={}", 
            player.getId(), school.getId().toString(), particleCount);
        
        // Send particle effect packet to all clients
        ParticleEffectPacket packet = new ParticleEffectPacket(
            player.getId(),
            school.getId().toString(),
            particleCount
        );
        
        ArsAffinity.LOGGER.debug("ParticleEffectPacket created successfully");
        ArsAffinity.LOGGER.debug("Sending particle packet to nearby clients at position: {}", player.blockPosition());
        
        try {
            Networking.sendToNearbyClient(player.level(), player.blockPosition(), packet);
            ArsAffinity.LOGGER.debug("Particle packet sent successfully to nearby clients");
            
            // Schedule position updates every 3 ticks for 60 ticks (3 seconds)
            ParticleUpdateScheduler.startPositionUpdates(player, school.getId().toString());
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to send particle packet: {}", e.getMessage(), e);
        }
        
        ArsAffinity.LOGGER.debug("Spawning {} spiral particles for {} point allocation (+{} points)", 
            particleCount, school.getId(), pointsGained);
        ArsAffinity.LOGGER.debug("=== PARTICLE SPAWNING COMPLETE ===");
    }
    
}