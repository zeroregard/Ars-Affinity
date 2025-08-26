# Ars Affinity

A progression system for Ars Nouveau that tracks player affinity with different spell schools and provides passive effects based on school tiers.

## Overview

Ars Affinity adds a visual affinity screen showing your progress in each spell school. As you cast spells, you gain affinity with that school, eventually reaching tiers 1-3. Each tier provides passive perks that can be buffs or debuffs depending on the situation.

See https://ars-affinity.vercel.app/ for different perks/abilities.

## Features

- **Affinity Progression**: Track your progress in each spell school through spell casting
- **Passive Perks**: Unlock passive abilities and effects as you reach higher tiers
- **Glyph Blacklist**: Configure specific glyphs to be ignored for affinity tracking
- **Visual Interface**: Beautiful GUI showing your current progress and tiers

## Configuration

### Glyph Blacklist

You can configure specific glyphs to be ignored for affinity progress tracking. This is useful for utility spells, transportation spells, or any glyphs that shouldn't contribute to magical training progression.

**Configuration File**: `config/ars_affinity-server.toml`

**Example**:
```toml
[glyph_blacklist]
blacklistedGlyphs = [
    "ars_nouveau:effect_heal",
    "ars_nouveau:method_touch",
    "ars_elemental:effect_charm"
]
```

**Glyph ID Format**: `modid:glyph_name`

For detailed configuration instructions, see [GLYPH_BLACKLIST.md](docs/GLYPH_BLACKLIST.md).

## Commands (OP Only)

For testing and debugging purposes, the following commands are available for server operators:

- `/ars-affinity set <school> <percentage>` - Set affinity for a school (0-100%)
- `/ars-affinity get <school>` - Get current affinity for a school
- `/ars-affinity list` - List all current affinities
- `/ars-affinity reset` - Reset all affinities to 0%
- `/ars-affinity blacklist` - Show current glyph blacklist configuration

**Examples:**
- `/ars-affinity set fire 100` - Set fire affinity to 100% (Tier 3)
- `/ars-affinity set water 50` - Set water affinity to 50% (Tier 2)
- `/ars-affinity get fire` - Check current fire affinity
- `/ars-affinity list` - See all affinities at once
- `/ars-affinity blacklist` - View configured glyph blacklist
