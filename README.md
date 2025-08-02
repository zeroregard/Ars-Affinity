# Ars Affinity

A progression system for Ars Nouveau that tracks player affinity with different spell schools and provides passive effects based on school tiers.

## Overview

Ars Affinity adds a visual affinity screen showing your progress in each spell school. As you cast spells, you gain affinity with that school, eventually reaching tiers 1-3. Each tier provides passive perks that can be buffs or debuffs depending on the situation.

## Buffs and Debuffs

### Fire School
- **PASSIVE_DOUSED**: Reduces mana regeneration by % when in water or rain
- **PASSIVE_MOB_PACIFICATION**: Mobs Blaze and Magma Cube ignore you (Tier 3)

### Water School
- **PASSIVE_DEHYDRATED**: Reduces mana regeneration by % when in the Nether or on fire

### Earth School
- TBD

### Air School
- TBD

### Abjuration School
- TBD

### Conjuration School
- TBD

### Necromancy School
- TBD

### Manipulation School
- TBD

## Commands (OP Only)

For testing and debugging purposes, the following commands are available for server operators:

- `/ars-affinity set <school> <percentage>` - Set affinity for a school (0-100%)
- `/ars-affinity get <school>` - Get current affinity for a school
- `/ars-affinity list` - List all current affinities
- `/ars-affinity reset` - Reset all affinities to 0%

**Examples:**
- `/ars-affinity set fire 100` - Set fire affinity to 100% (Tier 3)
- `/ars-affinity set water 50` - Set water affinity to 50% (Tier 2)
- `/ars-affinity get fire` - Check current fire affinity
- `/ars-affinity list` - See all affinities at once
