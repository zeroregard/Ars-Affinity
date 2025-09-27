package com.github.ars_affinity.util;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.client.particle.ParticleColor;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized utility class for school colors used throughout Ars Affinity.
 * All school colors are defined here to avoid duplication.
 */
public class SchoolColors {
    
    private static final Map<SpellSchool, ParticleColor> PARTICLE_COLORS = new HashMap<>();
    private static final Map<SpellSchool, Integer> HEX_COLORS = new HashMap<>();
    
    static {
        // Initialize particle colors (RGB format)
        // Fire - No color (white) - spritesheet already colored
        PARTICLE_COLORS.put(SpellSchools.ELEMENTAL_FIRE, new ParticleColor(255, 255, 255));
        
        // Water - Blue/Cyan
        PARTICLE_COLORS.put(SpellSchools.ELEMENTAL_WATER, new ParticleColor(130, 162, 237)); // 0xFF82a2ed -> RGB
        
        // Earth - Green
        PARTICLE_COLORS.put(SpellSchools.ELEMENTAL_EARTH, new ParticleColor(98, 226, 150)); // 0xFF62e296 -> RGB
        
        // Air - Yellow
        PARTICLE_COLORS.put(SpellSchools.ELEMENTAL_AIR, new ParticleColor(212, 207, 90)); // 0xFFd4cf5a -> RGB
        
        // Manipulation - Orange
        PARTICLE_COLORS.put(SpellSchools.MANIPULATION, new ParticleColor(255, 136, 0)); // 0xFFFF8800 -> RGB
        
        // Abjuration - Magenta
        PARTICLE_COLORS.put(SpellSchools.ABJURATION, new ParticleColor(235, 124, 206)); // 0xFFeb7cce -> RGB
        
        // Necromancy - Gray
        PARTICLE_COLORS.put(SpellSchools.NECROMANCY, new ParticleColor(109, 109, 109)); // 0xFF6d6d6d -> RGB
        
        // Conjuration - Teal
        PARTICLE_COLORS.put(SpellSchools.CONJURATION, new ParticleColor(106, 227, 206)); // 0xFF6ae3ce -> RGB
        
        // Initialize hex colors (for UI and other uses)
        HEX_COLORS.put(SpellSchools.ELEMENTAL_FIRE, 0xFFf06666);
        HEX_COLORS.put(SpellSchools.ELEMENTAL_WATER, 0xFF82a2ed);
        HEX_COLORS.put(SpellSchools.ELEMENTAL_EARTH, 0xFF62e296);
        HEX_COLORS.put(SpellSchools.ELEMENTAL_AIR, 0xFFd4cf5a);
        HEX_COLORS.put(SpellSchools.MANIPULATION, 0xFFFF8800);
        HEX_COLORS.put(SpellSchools.ABJURATION, 0xFFeb7cce);
        HEX_COLORS.put(SpellSchools.NECROMANCY, 0xFF6d6d6d);
        HEX_COLORS.put(SpellSchools.CONJURATION, 0xFF6ae3ce);
    }
    
    /**
     * Get the particle color for a spell school.
     * @param school The spell school
     * @return ParticleColor for the school, or white if not found
     */
    public static ParticleColor getParticleColor(SpellSchool school) {
        return PARTICLE_COLORS.getOrDefault(school, new ParticleColor(255, 255, 255));
    }
    
    /**
     * Get the hex color for a spell school (for UI elements).
     * @param school The spell school
     * @return Hex color as integer, or white if not found
     */
    public static int getHexColor(SpellSchool school) {
        return HEX_COLORS.getOrDefault(school, 0xFFFFFFFF);
    }
    
    /**
     * Get the red component of a school's color.
     * @param school The spell school
     * @return Red component (0-255)
     */
    public static int getRed(SpellSchool school) {
        ParticleColor color = getParticleColor(school);
        return (int) color.getRed();
    }
    
    /**
     * Get the green component of a school's color.
     * @param school The spell school
     * @return Green component (0-255)
     */
    public static int getGreen(SpellSchool school) {
        ParticleColor color = getParticleColor(school);
        return (int) color.getGreen();
    }
    
    /**
     * Get the blue component of a school's color.
     * @param school The spell school
     * @return Blue component (0-255)
     */
    public static int getBlue(SpellSchool school) {
        ParticleColor color = getParticleColor(school);
        return (int) color.getBlue();
    }
}