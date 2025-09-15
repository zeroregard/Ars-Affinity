package com.github.ars_affinity.client.screen.perk;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;

import java.util.HashMap;
import java.util.Map;

public class SchoolColorHelper {
    
    private static final Map<SpellSchool, SchoolColor> SCHOOL_COLORS = new HashMap<>();
    
    static {
        // Initialize school colors based on the docs constants
        SCHOOL_COLORS.put(SpellSchools.MANIPULATION, new SchoolColor(0xFFE99A58, 0xFFF2B366, 0xFFE99A58)); // Orange
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_FIRE, new SchoolColor(0xFFF06666, 0xFFF58A8A, 0xFFF06666)); // Red
        SCHOOL_COLORS.put(SpellSchools.NECROMANCY, new SchoolColor(0xFF8B0606, 0xFFA00A0A, 0xFF8B0606)); // Dark Red (Anima)
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_AIR, new SchoolColor(0xFFD4CF5A, 0xFFE0DC7A, 0xFFD4CF5A)); // Yellow
        SCHOOL_COLORS.put(SpellSchools.CONJURATION, new SchoolColor(0xFF6AE3CE, 0xFF8AE9D6, 0xFF6AE3CE)); // Teal
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_WATER, new SchoolColor(0xFF82A2ED, 0xFFA1BAF0, 0xFF82A2ED)); // Blue
        SCHOOL_COLORS.put(SpellSchools.ABJURATION, new SchoolColor(0xFFEB7CCE, 0xFFF099D9, 0xFFEB7CCE)); // Pink
        SCHOOL_COLORS.put(SpellSchools.ELEMENTAL_EARTH, new SchoolColor(0xFF62E296, 0xFF7EE8A8, 0xFF62E296)); // Green
    }
    
    public static int getSchoolColor(SpellSchool school) {
        SchoolColor color = SCHOOL_COLORS.get(school);
        return color != null ? color.primary : 0xFF666666; // Default gray
    }
    
    public static int getSchoolHoverColor(SpellSchool school) {
        SchoolColor color = SCHOOL_COLORS.get(school);
        return color != null ? color.hover : 0xFF888888; // Default gray
    }
    
    public static int getSchoolAllocatedColor(SpellSchool school) {
        SchoolColor color = SCHOOL_COLORS.get(school);
        return color != null ? color.allocated : 0xFF10B981; // Default green
    }
    
    private static class SchoolColor {
        final int primary;
        final int hover;
        final int allocated;
        
        SchoolColor(int primary, int hover, int allocated) {
            this.primary = primary;
            this.hover = hover;
            this.allocated = allocated;
        }
    }
}
