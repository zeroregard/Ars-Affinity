# Ars Affinity

A progression system for Ars Nouveau that tracks player affinity with different spell schools and provides passive effects based on school tiers.

## Overview

Ars Affinity adds a visual affinity screen showing your progress in each spell school. As you cast spells, you gain affinity with that school, eventually reaching tiers 1-3. Each tier provides passive perks that can be buffs or debuffs depending on the situation.

See https://ars-affinity.vercel.app/ for different perks/abilities.



## Commands (OP Only)

For testing and debugging purposes, the following commands are available for server operators:

- `/ars-affinity set <school> <percentage>` - Set affinity for a school (0-100%)
- `/ars-affinity get <school>` - Get current affinity for a school
- `/ars-affinity list` - List all current affinities
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
- `/ars-affinity set fire 100` - Set fire affinity to 100% (Tier 3)
- `/ars-affinity set water 50` - Set water affinity to 50% (Tier 2)
- `/ars-affinity get fire` - Check current fire affinity
- `/ars-affinity list` - See all affinities at once
- `/ars-affinity reset fire` - Reset fire school to 0 points
- `/ars-affinity reset all` - Reset all schools to 0 points
- `/ars-affinity blacklist` - View configured glyph blacklist