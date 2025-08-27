# Perk System Optimization: O(1) Lookup Implementation

## Overview

This document describes the implementation of an O(1) perk lookup system that replaces the previous O(n) approach. The new system provides significant performance improvements while maintaining backward compatibility.

## Problem Statement

The previous perk system had several inefficiencies:

1. **O(n) Perk Lookup**: Every perk check required looping through all schools and tiers
2. **Repeated Config Loading**: Multiple calls to `AffinityPerkManager.getPerksForLevel()`
3. **Event-Driven Inefficiency**: Checking ALL schools for specific perk types
4. **Memory Waste**: Creating temporary collections for each lookup

## Solution Architecture

### 1. Perk Index Structure

The new system introduces a `PerkData` class and an active perks index:

```java
public class PerkData {
    public final AffinityPerk perk;
    public final SpellSchool sourceSchool;
    public final int sourceTier;
}

// In SchoolAffinityProgress capability
private final Map<AffinityPerkType, PerkData> activePerks = new HashMap<>();
```

### 2. Automatic Index Maintenance

The perk index automatically rebuilds when:
- Player tiers change
- Affinities are normalized
- Player data is loaded from NBT

### 3. O(1) Access Methods

New helper methods provide direct access to active perks:

```java
// O(1) perk lookup
AffinityPerk perk = AffinityPerkHelper.getActivePerk(progress, AffinityPerkType.PASSIVE_MANA_TAP);

// Check if perk exists
boolean hasPerk = AffinityPerkHelper.hasActivePerk(progress, AffinityPerkType.PASSIVE_STONE_SKIN);

// Get perk metadata
PerkData perkData = AffinityPerkHelper.getActivePerkData(progress, AffinityPerkType.PASSIVE_FIRE_THORNS);
SpellSchool sourceSchool = AffinityPerkHelper.getPerkSourceSchool(progress, AffinityPerkType.PASSIVE_FIRE_THORNS);
int sourceTier = AffinityPerkHelper.getPerkSourceTier(progress, AffinityPerkType.PASSIVE_FIRE_THORNS);
```

## Implementation Phases

### Phase 1: Core Infrastructure ✅
- [x] Created `PerkData` class
- [x] Added `PerkChangeEvent` for perk change notifications
- [x] Integrated perk index into `SchoolAffinityProgress` capability
- [x] Implemented automatic index rebuilding

### Phase 2: Helper Methods ✅
- [x] Updated `AffinityPerkHelper` with O(1) methods
- [x] Maintained backward compatibility with legacy methods
- [x] Added comprehensive documentation

### Phase 3: Event Migration ✅
- [x] Updated `PassiveManaTapEvents` to use O(1) lookup
- [x] Updated `PassiveStoneSkinEvents` to use O(1) lookup
- [x] Created `PerkChangeEventHandler` for debugging

### Phase 4: Additional Events (Future)
- [ ] Update remaining passive perk events
- [ ] Update active ability events
- [ ] Performance testing and optimization

### Phase 5: Complete Migration (Future)
- [ ] Update all remaining events to use O(1) system
- [ ] Remove any remaining old method references
- [ ] Final performance validation

## Performance Benefits

### Before (O(n))
```java
// Loops through ALL schools and tiers
AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_MANA_TAP, perk -> {
    // Handle perk
});
```

### After (O(1))
```java
// Direct HashMap lookup
AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_MANA_TAP, perk -> {
    // Handle perk
});
```

**Performance Improvement**: From O(n) to O(1), where n is the number of schools × tiers.

## Event System

### PerkChangeEvent

The new system fires `PerkChangeEvent` whenever perks change:

```java
@SubscribeEvent
public static void onPerkChange(PerkChangeEvent event) {
    if (event.isPerkGained()) {
        // Handle new perk
    } else if (event.isPerkUpgraded()) {
        // Handle perk upgrade
    } else if (event.isPerkLost()) {
        // Handle perk loss
    }
}
```

### Event Types

- **Perk Gained**: Player gains access to a new perk type
- **Perk Lost**: Player loses access to a perk type
- **Perk Upgraded**: Player's perk tier increases
- **Perk Downgraded**: Player's perk tier decreases
- **Source School Changed**: Perk source changes between schools

## Migration Guide

### For Event Handlers

**Old Way:**
```java
AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_MANA_TAP, perk -> {
    // Handle perk
});
```

**New Way:**
```java
AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_MANA_TAP, perk -> {
    // Handle perk
});
```

### For Perk Checks

**Old Way:**
```java
int earthTier = progress.getTier(SpellSchools.ELEMENTAL_EARTH);
if (earthTier > 0) {
    AffinityPerkHelper.applyHighestTierPerk(progress, earthTier, SpellSchools.ELEMENTAL_EARTH, 
        AffinityPerkType.PASSIVE_STONE_SKIN, perk -> {
        // Handle perk
    });
}
```

**New Way:**
```java
if (AffinityPerkHelper.hasActivePerk(progress, AffinityPerkType.PASSIVE_STONE_SKIN)) {
    AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_STONE_SKIN, perk -> {
        // Handle perk
    });
}
```

## Data Persistence

The perk index is automatically serialized to NBT and restored on player login. The system stores:
- Perk type
- Source school
- Source tier

On deserialization, the system reconstructs the full `PerkData` objects from the stored metadata.

## Migration Required

The new system replaces the old O(n) methods completely. Existing code must be updated to use the new O(1) methods:

- **Old**: `AffinityPerkHelper.applyAllHighestTierPerks()`
- **New**: `AffinityPerkHelper.applyActivePerk()`

- **Old**: `AffinityPerkHelper.applyHighestTierPerk()`
- **New**: `AffinityPerkHelper.applyActivePerk()`

The system maintains the same perk configuration files and event behavior, but requires code updates to benefit from the performance improvements.

## Testing

### Unit Tests
- [ ] Perk index rebuilding
- [ ] Event firing
- [ ] NBT serialization/deserialization
- [ ] Performance benchmarks

### Integration Tests
- [ ] Perk changes during gameplay
- [ ] Multi-player scenarios
- [ ] Save/load functionality

## Future Enhancements

1. **Perk Priority System**: Allow perks to override others based on custom rules
2. **Perk Combinations**: Detect and optimize synergistic perk interactions
3. **Dynamic Perk Loading**: Hot-reload perk configurations without restart
4. **Perk Analytics**: Track perk usage and effectiveness

## Conclusion

The new O(1) perk lookup system provides significant performance improvements while maintaining full backward compatibility. The automatic index maintenance and comprehensive event system make it easy to build upon and extend in the future.

For questions or contributions, please refer to the project's issue tracker or pull request system.