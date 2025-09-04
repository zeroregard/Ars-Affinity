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
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.HashMap;
import java.util.Map;

public class TierChangeEffectsEvents {
    
    // Map of schools to their corresponding tier change sounds
    private static final Map<SpellSchool, SoundEvent> SCHOOL_SOUNDS = new HashMap<>();
    private static final Map<Integer, SoundEvent> TIER_SOUNDS = new HashMap<>();
    
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
    
    @SubscribeEvent
    public static void onTierChange(TierChangeEvent event) {
        if (!event.hasTierChanged()) {
            return;
        }
        
        Player player = event.getPlayer();
        SpellSchool school = event.getSchool();
        int newTier = event.getNewTier();
        
        // Only handle server-side events
        if (player.level().isClientSide()) {
            return;
        }
        
        // Play sound effect for nearby players
        playTierChangeSound(player, school, newTier);
        
        // Send chat message to the player
        sendTierChangeMessage(player, school, newTier);
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
        // Create the message components
        Component schoolName = school.getTextComponent();
        Component tierText = Component.literal("Tier " + newTier);
        
        // Create the full message: "Your affinity towards [School] changed to [Tier]"
        Component message = Component.translatable(
            "ars_affinity.tier_change.message",
            schoolName,
            tierText
        );
        
        // Send the message to the player
        player.sendSystemMessage(message);
    }
}