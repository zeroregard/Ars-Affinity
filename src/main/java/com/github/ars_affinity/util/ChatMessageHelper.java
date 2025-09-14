package com.github.ars_affinity.util;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * Helper class for sending chat messages related to affinity operations.
 * This is designed to be reusable for both commands and consumable items.
 */
public class ChatMessageHelper {
    
    private ChatMessageHelper() {}
    
    /**
     * Send a message to a player indicating that a specific school has been reset.
     * 
     * @param player The player to send the message to
     * @param school The school that was reset
     * @param pointsReset The number of points that were reset
     */
    public static void sendSchoolResetMessage(ServerPlayer player, SpellSchool school, int pointsReset) {
        String schoolName = formatSchoolName(school);
        Component message = Component.literal(String.format("§aReset %s affinity to 0 points (removed %d points)", 
            schoolName, pointsReset));
        player.sendSystemMessage(message);
        
        ArsAffinity.LOGGER.info("Player {} reset {} affinity (removed {} points)", 
            player.getName().getString(), schoolName, pointsReset);
    }
    
    /**
     * Send a message to a player indicating that all schools have been reset.
     * 
     * @param player The player to send the message to
     * @param totalPointsReset The total number of points that were reset across all schools
     */
    public static void sendAllSchoolsResetMessage(ServerPlayer player, int totalPointsReset) {
        Component message = Component.literal(String.format("§aReset all affinities to 0 points (removed %d total points)", 
            totalPointsReset));
        player.sendSystemMessage(message);
        
        ArsAffinity.LOGGER.info("Player {} reset all affinities (removed {} total points)", 
            player.getName().getString(), totalPointsReset);
    }
    
    /**
     * Send a message to a player indicating that a school reset failed.
     * 
     * @param player The player to send the message to
     * @param school The school that failed to reset
     * @param reason The reason for the failure
     */
    public static void sendSchoolResetFailureMessage(ServerPlayer player, SpellSchool school, String reason) {
        String schoolName = formatSchoolName(school);
        Component message = Component.literal(String.format("§cFailed to reset %s affinity: %s", 
            schoolName, reason));
        player.sendSystemMessage(message);
    }
    
    /**
     * Send a message to a player indicating that all schools reset failed.
     * 
     * @param player The player to send the message to
     * @param reason The reason for the failure
     */
    public static void sendAllSchoolsResetFailureMessage(ServerPlayer player, String reason) {
        Component message = Component.literal(String.format("§cFailed to reset all affinities: %s", reason));
        player.sendSystemMessage(message);
    }
    
    /**
     * Format a school name for display in chat messages.
     * 
     * @param school The school to format
     * @return A formatted school name
     */
    private static String formatSchoolName(SpellSchool school) {
        return school.getId().toString()
            .replace("ars_nouveau:", "")
            .replace("_", " ")
            .toLowerCase();
    }
}



