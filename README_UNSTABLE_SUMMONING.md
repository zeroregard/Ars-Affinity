# UNSTABLE_SUMMONING Perk Implementation

## Overview
I have successfully implemented the `UNSTABLE_SUMMONING` passive perk for the Ars Affinity mod. This perk adds an element of unpredictability to conjuration spells by transforming summoned entities into different creatures based on a configurable chance and entity list.

## What Was Created

### 1. New Perk Type
- Added `PASSIVE_UNSTABLE_SUMMONING` to `AffinityPerkType.java`

### 2. New Perk Class
- Created `UnstableSummoningPerk` class in `AffinityPerk.java`
- Supports configurable chance and entity list
- Extends the base `AffinityPerk` class

### 3. Event Handler
- Created `PassiveUnstableSummoningEvents.java`
- Listens to `SummonEvent` from Ars Nouveau
- Applies the perk when conjuration spells are cast
- Transforms entities based on configuration

### 4. Configuration Support
- Updated `AffinityPerkDescriptionHelper.java` to handle the new perk type
- Added proper description formatting for the perk

### 5. Configuration Examples
- Created example configurations for different tiers
- Added documentation and usage examples

## How It Works

### Core Functionality
1. **Trigger**: Every time a conjuration spell summons a creature
2. **Chance Check**: Configurable percentage chance (0.0 to 1.0) for transformation
3. **Entity Replacement**: Original summoned entity is replaced with a random entity from the configured list
4. **Lifetime Preservation**: The transformed entity maintains the same behavior as summoned entities
5. **No Drops**: Transformed entities drop no XP or items on death
6. **Persistence**: Entities are marked as persistent to prevent despawning

### Technical Implementation
- Uses the existing `SummonEvent` system from Ars Nouveau
- Integrates with the existing perk management system
- Follows the same pattern as other passive perks
- Maintains compatibility with existing conjuration mechanics

## Configuration

### Basic Structure
```json
{
  "perk": "PASSIVE_UNSTABLE_SUMMONING",
  "chance": 0.25,
  "entities": [
    "minecraft:zombie",
    "minecraft:skeleton",
    "minecraft:spider"
  ],
  "isBuff": true
}
```

### Parameters
- **perk**: Must be `"PASSIVE_UNSTABLE_SUMMONING"`
- **chance**: Float value between 0.0 and 1.0 (e.g., 0.25 = 25% chance)
- **entities**: Array of entity registry names
- **isBuff**: Boolean indicating if this is beneficial (usually true)

### Example Configurations

#### Tier 1 (Basic)
```json
[
  {
    "perk": "PASSIVE_UNSTABLE_SUMMONING",
    "chance": 0.15,
    "entities": [
      "minecraft:zombie",
      "minecraft:skeleton",
      "minecraft:spider"
    ],
    "isBuff": true
  }
]
```

#### Tier 2 (Advanced)
```json
[
  {
    "perk": "PASSIVE_UNSTABLE_SUMMONING",
    "chance": 0.30,
    "entities": [
      "minecraft:blaze",
      "minecraft:ghast",
      "minecraft:magma_cube",
      "minecraft:guardian"
    ],
    "isBuff": true
  }
]
```

#### Tier 3 (Expert)
```json
[
  {
    "perk": "PASSIVE_UNSTABLE_SUMMONING",
    "chance": 0.45,
    "entities": [
      "minecraft:ender_dragon",
      "minecraft:wither",
      "minecraft:warden",
      "minecraft:elder_guardian"
    ],
    "isBuff": true
  }
]
```

## Entity Registry Names

### Common Hostile Mobs
- `minecraft:zombie` - Zombie
- `minecraft:skeleton` - Skeleton
- `minecraft:creeper` - Creeper
- `minecraft:spider` - Spider
- `minecraft:enderman` - Enderman

### Nether Mobs
- `minecraft:blaze` - Blaze
- `minecraft:ghast` - Ghast
- `minecraft:magma_cube` - Magma Cube
- `minecraft:wither_skeleton` - Wither Skeleton

### Water Mobs
- `minecraft:guardian` - Guardian
- `minecraft:elder_guardian` - Elder Guardian
- `minecraft:drowned` - Drowned

### Boss Mobs
- `minecraft:ender_dragon` - Ender Dragon
- `minecraft:wither` - Wither
- `minecraft:warden` - Warden

## File Placement

### Configuration Files
Place your perk configuration in:
```
config/ars_affinity/perks/conjuration/[tier].json
```

Where `[tier]` is the level (1, 2, 3, etc.)

### Code Files
The following files were created/modified:
- `src/main/java/com/github/ars_affinity/perk/AffinityPerkType.java`
- `src/main/java/com/github/ars_affinity/perk/AffinityPerk.java`
- `src/main/java/com/github/ars_affinity/event/PassiveUnstableSummoningEvents.java`
- `src/main/java/com/github/ars_affinity/perk/AffinityPerkDescriptionHelper.java`

## Requirements

### Player Requirements
- Must have the Conjuration school unlocked
- Must have reached the required tier level
- The perk must be configured in the appropriate tier file

### Mod Requirements
- Ars Nouveau (for summon events)
- Ars Affinity (for perk system)
- Minecraft Forge/NeoForge

## Features

### What It Does
✅ Transforms summoned entities based on chance
✅ Maintains entity lifetime behavior
✅ Prevents XP and item drops
✅ Configurable entity list
✅ Configurable chance percentage
✅ Integrates with existing perk system
✅ Logs transformations for debugging

### What It Doesn't Do
❌ Doesn't affect non-conjuration spells
❌ Doesn't modify entity AI or behavior
❌ Doesn't change entity stats or abilities
❌ Doesn't affect player-owned entities

## Usage Tips

### Balancing
- Start with low chances (10-15%) for tier 1
- Increase chances progressively with tiers
- Mix powerful and weak entities for balance
- Consider the player's level when choosing entities

### Configuration
- Use entity registry names (e.g., `minecraft:zombie`)
- Test with small entity lists first
- Monitor server logs for transformation events
- Adjust chances based on player feedback

### Integration
- Works with all existing conjuration spells
- Compatible with other passive perks
- No conflicts with summon health or other effects
- Follows the same configuration pattern

## Troubleshooting

### Common Issues
1. **Perk not working**: Check if conjuration school is unlocked
2. **Invalid entities**: Verify entity registry names
3. **No transformations**: Check chance values and entity lists
4. **Performance issues**: Limit entity list size

### Debug Information
The perk logs all transformations to the server log:
```
Player [PlayerName] triggered UNSTABLE_SUMMONING perk (25%) - transformed summon into minecraft:zombie
```

## Future Enhancements

### Potential Improvements
- Entity-specific transformation chances
- Tier-based entity filtering
- Custom entity properties
- Transformation effects/particles
- Player preference settings

### Compatibility
- Designed to work with future Ars Nouveau updates
- Extensible for additional entity types
- Maintains backward compatibility

## Conclusion

The `UNSTABLE_SUMMONING` perk has been successfully implemented and provides a fun, configurable way to add unpredictability to conjuration spells. It follows the existing code patterns and integrates seamlessly with the current perk system.

The perk is ready for use and can be configured through JSON files in the config directory. Players will experience random entity transformations when summoning creatures, adding an element of surprise and strategy to their conjuration magic.