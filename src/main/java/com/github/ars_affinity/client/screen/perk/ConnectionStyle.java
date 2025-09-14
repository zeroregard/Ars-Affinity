package com.github.ars_affinity.client.screen.perk;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

public class ConnectionStyle {
    private final int color;
    private final int glowColor;
    private final float thickness;
    private final boolean isDashed;
    private final boolean hasGlow;
    private final float glowIntensity;
    
    public ConnectionStyle(int color, int glowColor, float thickness, boolean isDashed, boolean hasGlow, float glowIntensity) {
        this.color = color;
        this.glowColor = glowColor;
        this.thickness = thickness;
        this.isDashed = isDashed;
        this.hasGlow = hasGlow;
        this.glowIntensity = glowIntensity;
    }
    
    public static ConnectionStyle forType(ConnectionType type, SpellSchool school) {
        return switch (type) {
            case AVAILABLE -> new ConnectionStyle(
                0xFF888888, // Grey
                0x00000000, // No glow
                0.5f, // Very thin line
                true, // Dashed
                false,
                0.0f
            );
            case ACTIVE -> new ConnectionStyle(
                0xFFAAAAAA, // Light grey
                0x00000000, // No glow
                0.8f, // Slightly thicker
                false, // Solid
                false,
                0.0f
            );
            case LOCKED -> new ConnectionStyle(
                0xFF666666, // Dark grey
                0x00000000, // No glow
                0.4f, // Very thin
                true, // Dashed
                false,
                0.0f
            );
            case PREREQUISITE -> new ConnectionStyle(
                0xFF999999, // Medium grey
                0x00000000, // No glow
                0.6f, // Thin
                false, // Solid
                false,
                0.0f
            );
        };
    }
    
    private static int getSchoolColor(SpellSchool school, float alpha) {
        int baseColor = switch (school.getId()) {
            case "elemental_fire" -> 0xFF8B0000; // Dark red
            case "elemental_water" -> 0xFF0066CC; // Blue
            case "elemental_earth" -> 0xFF8B4513; // Brown
            case "elemental_air" -> 0xFF87CEEB; // Sky blue
            case "abjuration" -> 0xFF9370DB; // Purple
            case "conjuration" -> 0xFF32CD32; // Green
            case "manipulation" -> 0xFFFFD700; // Gold
            case "necromancy" -> 0xFF4B0082; // Indigo
            default -> 0xFF888888; // Default gray
        };
        
        int alphaValue = (int) (alpha * 255);
        return (alphaValue << 24) | (baseColor & 0x00FFFFFF);
    }
    
    public int getColor() { return color; }
    public int getGlowColor() { return glowColor; }
    public float getThickness() { return thickness; }
    public boolean isDashed() { return isDashed; }
    public boolean hasGlow() { return hasGlow; }
    public float getGlowIntensity() { return glowIntensity; }
}
