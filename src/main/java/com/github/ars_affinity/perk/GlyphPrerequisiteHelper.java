package com.github.ars_affinity.perk;

import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.common.capability.IPlayerCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class GlyphPrerequisiteHelper {
    
    private static ResourceLocation parseResourceLocation(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        
        String[] parts = id.split(":", 2);
        if (parts.length == 2) {
            return ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
        }
        
        return ResourceLocation.fromNamespaceAndPath("minecraft", id);
    }
    
    /**
     * Check if a player has unlocked a specific glyph.
     * @param player The player to check
     * @param glyphId The glyph ID (e.g., "ars_nouveau:glyph_blink")
     * @return true if the player has unlocked the glyph
     */
    public static boolean hasUnlockedGlyph(Player player, String glyphId) {
        if (player == null || glyphId == null || glyphId.isEmpty()) {
            return true;
        }
        
        try {
            ResourceLocation glyphLocation = parseResourceLocation(glyphId);
            if (glyphLocation == null) {
                return false;
            }
            
            IPlayerCap playerCap = CapabilityRegistry.getPlayerDataCap(player);
            
            if (playerCap == null) {
                return false;
            }
            
            AbstractSpellPart glyph = GlyphRegistry.getSpellpartMap().get(glyphLocation);
            if (glyph == null) {
                return false;
            }
            
            return playerCap.knowsGlyph(glyph);
            
        } catch (Exception e) {
            com.github.ars_affinity.ArsAffinity.LOGGER.warn("Error checking glyph prerequisite {}: {}", glyphId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the display name of a glyph for tooltip purposes.
     * @param glyphId The glyph ID
     * @return The display name of the glyph
     */
    public static String getGlyphDisplayName(String glyphId) {
        if (glyphId == null || glyphId.isEmpty()) {
            return "";
        }
        
        try {
            ResourceLocation glyphLocation = parseResourceLocation(glyphId);
            if (glyphLocation != null) {
                AbstractSpellPart glyph = GlyphRegistry.getSpellpartMap().get(glyphLocation);
                
                if (glyph != null) {
                    return glyph.getName();
                }
            }
            
            String[] parts = glyphId.split(":");
            if (parts.length >= 2) {
                String glyphName = parts[1].replace("glyph_", "");
                if (!glyphName.isEmpty()) {
                    return glyphName.substring(0, 1).toUpperCase() + glyphName.substring(1);
                }
            }
            
            return glyphId;
            
        } catch (Exception e) {
            com.github.ars_affinity.ArsAffinity.LOGGER.warn("Error getting glyph display name for {}: {}", glyphId, e.getMessage());
            return glyphId;
        }
    }
}
