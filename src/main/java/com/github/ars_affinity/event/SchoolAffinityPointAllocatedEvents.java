package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
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
        int totalPoints = event.getTotalPoints();
        
        // Only handle server-side events
        if (player.level().isClientSide()) {
            return;
        }
        
        playPointAllocatedSound(player, school);
        sendPointAllocatedMessage(player, school, pointsGained, totalPoints);
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
    
    private static void sendPointAllocatedMessage(Player player, SpellSchool school, int pointsGained, int totalPoints) {
        // Create the message components
        Component schoolName = school.getTextComponent();
        Component pointsText = Component.literal("+" + pointsGained + " points");
        Component totalText = Component.literal("(" + totalPoints + " total)");
        
        // Create the full message: "Your affinity in %s has increased by +X points (Y total)"
        Component message = Component.translatable(
            "ars_affinity.point_allocated.message",
            schoolName,
            pointsText,
            totalText
        );
        
        // Send the message to the player
        player.sendSystemMessage(message);
    }
}