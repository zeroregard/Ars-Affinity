package com.github.ars_affinity.util;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * Utility class to check if glyphs are blacklisted from affinity progress tracking.
 */
public class GlyphBlacklistHelper {
    
    /**
     * Checks if a glyph is blacklisted from affinity progress tracking.
     * 
     * @param glyph The glyph to check
     * @return true if the glyph is blacklisted, false otherwise
     */
    public static boolean isGlyphBlacklisted(AbstractSpellPart glyph) {
        if (glyph == null) {
            return false;
        }
        
        try {
            // Get the registry name of the glyph
            ResourceLocation registryName = glyph.getRegistryName();
            if (registryName == null) {
                ArsAffinity.LOGGER.debug("Glyph has no registry name, cannot check blacklist");
                return false;
            }
            
            // Convert to string format for comparison
            String glyphId = registryName.toString();
            
            // Check against the blacklist
            List<? extends String> blacklist = ArsAffinityConfig.GLYPH_BLACKLIST.get();
            if (blacklist == null || blacklist.isEmpty()) {
                return false;
            }
            
            boolean isBlacklisted = blacklist.contains(glyphId);
            
            if (isBlacklisted) {
                ArsAffinity.LOGGER.debug("Glyph {} is blacklisted from affinity tracking", glyphId);
            }
            
            return isBlacklisted;
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error checking if glyph is blacklisted: {}", e.getMessage());
            // If there's any error checking the blacklist, default to not blacklisted
            return false;
        }
    }
    
    /**
     * Checks if a glyph ID string is blacklisted.
     * 
     * @param glyphId The glyph ID to check (format: "modid:glyph_name")
     * @return true if the glyph ID is blacklisted, false otherwise
     */
    public static boolean isGlyphIdBlacklisted(String glyphId) {
        if (glyphId == null || glyphId.isEmpty()) {
            return false;
        }
        
        try {
            List<? extends String> blacklist = ArsAffinityConfig.GLYPH_BLACKLIST.get();
            if (blacklist == null || blacklist.isEmpty()) {
                return false;
            }
            
            boolean isBlacklisted = blacklist.contains(glyphId);
            
            if (isBlacklisted) {
                ArsAffinity.LOGGER.debug("Glyph ID {} is blacklisted from affinity tracking", glyphId);
            }
            
            return isBlacklisted;
            
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error checking if glyph ID {} is blacklisted: {}", glyphId, e.getMessage());
            // If there's any error checking the blacklist, default to not blacklisted
            return false;
        }
    }
    
    /**
     * Logs the current blacklist configuration for debugging purposes.
     */
    public static void logBlacklistConfiguration() {
        try {
            List<? extends String> blacklist = ArsAffinityConfig.GLYPH_BLACKLIST.get();
            if (blacklist == null || blacklist.isEmpty()) {
                ArsAffinity.LOGGER.debug("Glyph blacklist is empty - no glyphs are blacklisted");
            } else {
                ArsAffinity.LOGGER.debug("Glyph blacklist contains {} entries:", blacklist.size());
                for (String glyphId : blacklist) {
                    ArsAffinity.LOGGER.debug("  - {}", glyphId);
                }
            }
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Error logging blacklist configuration: {}", e.getMessage());
        }
    }
}