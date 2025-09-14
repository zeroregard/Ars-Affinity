# Ars Affinity

A sophisticated progression system for Ars Nouveau that tracks player affinity with different spell schools and provides a point-based perk allocation system.

## Overview

Ars Affinity adds a comprehensive perk system where players gain affinity points by casting spells and can manually allocate these points to specific perks in each school. The system features:

- **Point-Based Progression**: Gain affinity points by casting spells instead of automatic tier progression
- **Manual Perk Allocation**: Choose which perks to invest in with strategic point spending
- **Active Ability System**: One active ability per player with 8 different school-specific abilities
- **Scaling Difficulty**: Points become harder to gain as you progress, encouraging specialization
- **Respec System**: Reset and reallocate points with consumable items or commands
- **Interactive UI**: Web-based perk tree viewer and in-game interface

See https://ars-affinity.vercel.app/ for the interactive perk tree viewer.

## Perk System

### How It Works
1. **Gain Points**: Cast spells to earn affinity points in the corresponding school
2. **Allocate Points**: Spend points on specific perks in each school's perk tree
3. **Choose Active Ability**: Select one active ability from any school (only one at a time)
4. **Respec**: Reset points and reallocate them as your strategy evolves

### Perk Categories
- **Passive Perks**: Always-active effects (e.g., Fire Thorns, Mana Tap, Stone Skin)
- **Active Abilities**: Triggered abilities with cooldowns (e.g., Ice Blast, Ground Slam, Fire Dash)
- **Utility Perks**: Helper effects (e.g., Cold Walker, Hydration, Ghost Step)
- **School Power**: Increases spell power for specific schools
- **School Resistance**: Reduces damage from specific school spells

### Active Abilities
Each school has one unique active ability:
- **Fire**: Fire Dash - Dash forward with fire damage
- **Water**: Ice Blast - Freeze and damage enemies in an area
- **Earth**: Ground Slam - Slam down with area damage
- **Air**: Air Dash - Dash forward with air effects
- **Abjuration**: Sanctuary - Create a protective area
- **Necromancy**: Curse Field - Create a damaging curse area
- **Conjuration**: Swap Ability - Swap positions with a target
- **Manipulation**: Ghost Step - Become invisible and heal

### Point Scaling
- **School Scaling**: Points become harder to gain in schools where you already have many points
- **Global Scaling**: Points become harder to gain as your total points across all schools increase
- **Encourages Specialization**: Focus on one or two schools for maximum efficiency

## Commands (OP Only)

For testing and debugging purposes, the following commands are available for server operators:

- `/ars-affinity set <school> <percentage>` - Set affinity points for a school (0-100%)
- `/ars-affinity get <school>` - Get current affinity points for a school
- `/ars-affinity list` - List all current affinities and allocated perks
- `/ars-affinity reset <school|all>` - Reset specific school or all schools to 0 points
- `/ars-affinity blacklist` - Show current glyph blacklist

## Items

### Tablet of Amnesia
A consumable tablet that allows you to reset your affinity progress in a specific school, making it easier to gain points in other schools.

**Crafting:**
1. **Base Tablet**: Book + 4 Paper (100 source cost) → 4 Tablets of Amnesia
2. **School-Specific**: Base Tablet + 1 Essence of desired school (200 source cost) → 1 School-Specific Tablet

**Usage:**
- Right-click to consume the tablet
- Resets the specified school's affinity to 0 points
- Uses the same chat messaging system as the reset command 

**Examples:**
- `/ars-affinity set fire 100` - Set fire affinity to 100% (maximum points)
- `/ars-affinity set water 50` - Set water affinity to 50% (half points)
- `/ars-affinity get fire` - Check current fire affinity points
- `/ars-affinity list` - See all affinities and allocated perks
- `/ars-affinity reset fire` - Reset fire school to 0 points
- `/ars-affinity reset all` - Reset all schools to 0 points
- `/ars-affinity blacklist` - View configured glyph blacklist