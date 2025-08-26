# Glyph Blacklist Configuration

The Glyph Blacklist feature allows you to configure specific glyphs to be ignored for Affinity progress tracking. This is useful when you want to prevent certain spells or effects from contributing to school affinity progression.

## Overview

When a glyph is blacklisted:
- It will not contribute to affinity progress when cast
- Mana cost is still consumed normally
- The spell effect still works as intended
- Only the affinity tracking is disabled

## Default Blacklist

The mod comes with a default blacklist that includes commonly problematic glyphs:

- `ars_nouveau:effect_break` - The break effect glyph (blacklisted by default)

This default ensures that basic utility spells don't contribute to affinity progression without requiring manual configuration.

## Configuration

### In-Game Configuration

1. Open your mod configuration (usually accessible through Mod Menu or in-game settings)
2. Navigate to the `ars_affinity` section
3. Find the `glyph_blacklist` subsection
4. Add glyph IDs to the `blacklistedGlyphs` list

### Configuration File

The configuration is stored in your config directory at:
```
config/ars_affinity-server.toml
```

### Glyph ID Format

Glyph IDs use the format: `modid:glyph_name`

Examples:
- `ars_nouveau:effect_heal` - Ars Nouveau's heal effect
- `ars_nouveau:method_touch` - Ars Nouveau's touch cast method
- `ars_elemental:effect_charm` - Ars Elemental's charm effect

## Finding Glyph IDs

### Method 1: In-Game Debug
1. Enable debug logging for Ars Affinity
2. Cast a spell with the glyph you want to blacklist
3. Check the logs for the glyph ID

### Method 2: Mod Documentation
Check the mod's documentation or source code for registry names.

### Method 3: Registry Inspection
Use commands or tools that can inspect the game's registry.

## Example Configuration

```toml
[glyph_blacklist]
blacklistedGlyphs = [
    # The break effect is blacklisted by default
    # "ars_nouveau:effect_break",
    
    # Additional custom blacklisted glyphs
    "ars_nouveau:effect_heal",
    "ars_nouveau:method_touch",
    "ars_elemental:effect_charm"
]
```

## Use Cases

### Common Scenarios for Blacklisting:

1. **Utility Spells**: Spells that don't represent combat or magical training
2. **Transportation**: Teleportation or movement spells that shouldn't affect affinity
3. **Crafting**: Spells used for item creation or modification
4. **Balancing**: Overpowered or problematic glyphs that give too much affinity
5. **Roleplay**: Spells that don't fit your server's lore or progression system

### Example: Blacklisting Heal Spells

If you want to prevent healing spells from contributing to affinity:

```toml
[glyph_blacklist]
blacklistedGlyphs = [
    # Default blacklisted glyphs are automatically included
    # "ars_nouveau:effect_break",
    
    # Additional custom blacklisted glyphs
    "ars_nouveau:effect_heal",
    "ars_nouveau:effect_heal_undead"
]
```

## Technical Details

- The blacklist is checked before any affinity calculations
- Blacklisted glyphs are completely ignored in the tracking system
- The feature is server-side only (affects all players)
- Changes require a server restart to take effect
- Empty or invalid glyph IDs are safely ignored
- Default blacklisted glyphs are automatically included

## Troubleshooting

### Glyph Still Contributing to Affinity

1. Check that the glyph ID is correct (case-sensitive)
2. Ensure the server has been restarted
3. Verify the configuration file syntax
4. Check server logs for any errors
5. Remember that default blacklisted glyphs are automatically included

### Configuration Not Loading

1. Verify the configuration file is in the correct location
2. Check for syntax errors in the TOML file
3. Ensure the mod is properly installed
4. Check server logs for configuration errors

## Support

If you encounter issues with the glyph blacklist feature:

1. Check the server logs for error messages
2. Verify your configuration syntax
3. Ensure you're using the correct glyph IDs
4. Remember that some glyphs are blacklisted by default
5. Report bugs with detailed information about your setup