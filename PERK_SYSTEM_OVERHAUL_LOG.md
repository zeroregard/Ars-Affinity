# Ars Affinity Perk System Overhaul - Work Log

## Overview
Complete rebuild of the Ars Affinity perk system based on Discord discussion. Moving from automatic tier-based perks to a manual point-buy system where players gain affinity points by using spells and can manually assign them to specific perks.

## Current System Analysis

### What We Have Now:
1. **Automatic Tier System**: Players gain affinity percentages (0-100%) by casting spells
2. **Tier Thresholds**: Configurable thresholds (e.g., 25%, 50%, 75%) determine tier levels
3. **Automatic Perk Application**: Perks are automatically applied based on tier level
4. **JSON Configuration**: Perks defined in `config/ars_affinity/perks/school/tier/` structure
5. **Data Storage**: `SchoolAffinityProgress` capability stores affinity percentages and active perks
6. **UI**: Visual affinity screen showing progress circles for each school

### Problems with Current System:
- **No Player Choice**: Perks are automatically applied, no manual control
- **Soft-locking**: Using utility spells "wrong" element penalizes players
- **No Respec**: Can't change perk choices once made
- **Negative Perks**: Unwanted debuffs that players don't want
- **Rigid Structure**: Hard to add new schools or modify existing ones

### What We Removed:
- **All Negative Perks**: Removed `PASSIVE_DOUSED`, `PASSIVE_DEHYDRATED`, `PASSIVE_BURIED`, `PASSIVE_GROUNDED`, `PASSIVE_BLIGHTED`, `PASSIVE_PACIFIST`, `PASSIVE_MANIPULATION_SICKNESS`
- **Mob Pacification Perk**: Removed `PASSIVE_MOB_PACIFICATION` as it was deemed unnecessary
- **Affinity Reduction Logic**: Removed all logic that reduced affinity in one school when increasing another
- **Stackable Perk System**: Replaced with level-based system where each perk node represents a specific level
- **Old Event Handlers**: Deleted event classes for removed negative perks (`MobPacificationEvents.java`, `ManaRegenCalcEvents.java`, `PassiveBuriedEvents.java`, `PassiveDehydratedEvents.java`, `PassiveGroundedEvents.java`, `PassiveManipulationSicknessEvents.java`, `PassivePacifistEvents.java`, `SpellBlightEvents.java`)
- **Old Configuration System**: Replaced tier-based JSON structure with perk tree structure
- **Old Data Storage**: Replaced `SchoolAffinityProgress` with `PlayerAffinityData`

## New System Design

### Core Concepts:
1. **Affinity Points**: Players gain points in each school by using spells (not percentages)
2. **Point-Buy System**: Players manually spend points on specific perks
3. **Scaling Costs**: More total points = more expensive to gain new points
4. **Respec System**: Players can reallocate points (with cost/cooldown)
5. **No Negative Perks**: All perks are beneficial, debuffs become temporary effects on enemies
6. **Perk Trees**: Visual tree structure for each school

### Data Structure Changes:

#### New Player Data:
```java
public class PlayerAffinityData {
    // Points per school (not percentages)
    private Map<SpellSchool, Integer> schoolPoints = new HashMap<>();
    
    // Allocated perks (what player has chosen)
    private Map<AffinityPerkType, PerkAllocation> allocatedPerks = new HashMap<>();
    
    // Available points to spend
    private Map<SpellSchool, Integer> availablePoints = new HashMap<>();
    
    // Perk tree unlocks
    private Set<PerkNode> unlockedNodes = new HashSet<>();
}
```

#### New Perk System:
```java
public class PerkNode {
    private AffinityPerkType perkType;
    private SpellSchool school;
    private int tier;
    private int pointCost;
    private List<PerkNode> prerequisites;
    private PerkCategory category; // PASSIVE, ACTIVE, UTILITY
}

public class PerkAllocation {
    private AffinityPerkType perkType;
    private SpellSchool school;
    private int tier;
    private int pointsInvested;
    private boolean isActive;
}
```

### UI Changes:
1. **Perk Tree Screen**: Replace affinity circles with perk trees
2. **Point Display**: Show available points per school
3. **Perk Details**: Hover/click for perk descriptions and costs
4. **Respec Button**: Allow point reallocation
5. **School Tabs**: Separate tabs for each school

### Configuration Changes:
1. **Perk Definitions**: Define perks with point costs and prerequisites
2. **Scaling Formulas**: Configure how point costs increase
3. **Respec Costs**: Configure respec mechanics
4. **Point Gain Rates**: Configure how fast points are gained

## Implementation Plan

### Phase 1: Data Structure Overhaul
- [ ] Create new `PlayerAffinityData` class
- [ ] Create `PerkNode` and `PerkAllocation` classes
- [ ] Update capability system for new data structure
- [ ] Create migration system from old to new data

### Phase 2: Point System Implementation
- [ ] Modify spell tracking to award points instead of percentages
- [ ] Implement point scaling formulas
- [ ] Create point management system
- [ ] Update commands for new system

### Phase 3: Perk Tree System
- [ ] Create perk tree data structure
- [ ] Implement perk allocation logic
- [ ] Create perk validation system
- [ ] Implement prerequisite checking

### Phase 4: UI Overhaul
- [ ] Create new perk tree screen
- [ ] Implement school tabs
- [ ] Add point display and management
- [ ] Create perk detail panels
- [ ] Add respec functionality

### Phase 5: Configuration System
- [ ] Create new perk definition format
- [ ] Implement perk tree configuration
- [ ] Add scaling formula configuration
- [ ] Create respec configuration

### Phase 6: Migration and Testing
- [ ] Create data migration from old system
- [ ] Test all functionality
- [ ] Update documentation
- [ ] Create example configurations

## Technical Details

### Point Gain Formula:
```
baseGain = manaCost * schoolMultiplier
schoolScaling = 1 / (1 + currentSchoolPoints^schoolDecayStrength)
globalScaling = 1 / (1 + totalPointsAcrossAllSchools^globalDecayStrength)
finalGain = baseGain * schoolScaling * globalScaling
```

### Scaling System:
- **School-specific scaling**: Points become harder to gain as you have more points in that specific school
- **Global scaling**: Points become harder to gain as you have more total points across all schools
- **Combined effect**: Both scaling factors multiply together, creating exponential difficulty
- **Configurable**: Both decay strength and minimum factors are configurable per server

### Perk Cost Formula:
```
baseCost = perkTier * 10
scalingCost = baseCost * (1 + (totalPoints / 100))
finalCost = baseCost + scalingCost
```

### Respec System:
- Cost: 10% of total points or minimum cost
- Cooldown: 24 hours (configurable)
- Partial respec: Only specific schools

## File Structure Changes

### New Files:
- `PlayerAffinityData.java` - New data storage
- `PerkNode.java` - Perk tree node definition
- `PerkAllocation.java` - Player's perk choices
- `PerkTreeManager.java` - Tree management logic
- `PointSystem.java` - Point calculation and management
- `PerkTreeScreen.java` - New UI screen
- `PerkTreeRenderer.java` - Tree rendering logic

### Modified Files:
- `SchoolAffinityProgress.java` - Complete rewrite
- `AffinityScreen.java` - Replace with perk tree
- `SpellTrackingMixin.java` - Award points instead of percentages
- `ArsAffinityConfig.java` - Add new configuration options

### Configuration Structure:
```
config/ars_affinity/
├── perk_trees/
│   ├── fire.json
│   ├── water.json
│   ├── earth.json
│   ├── air.json
│   ├── abjuration.json
│   ├── necromancy.json
│   ├── conjuration.json
│   └── manipulation.json
├── scaling.json
└── respec.json
```

## Progress Tracking

### Completed:
- [x] Analysis of current system
- [x] Design of new system
- [x] Implementation plan
- [x] **Phase 1: Data Structure Overhaul**
  - [x] Created `PlayerAffinityData` class to replace `SchoolAffinityProgress`
  - [x] Created `PerkNode` and `PerkAllocation` classes for tree system
  - [x] Created `PerkCategory` enum for perk classification
  - [x] Updated `AffinityPerkType` enum - **REMOVED ALL NEGATIVE PERKS**
  - [x] Added new stackable perk types
  - [x] Created `PerkTreeManager` for tree management and point scaling
  - [x] Created new capability system (`PlayerAffinityDataProvider`, `PlayerAffinityDataHelper`)
  - [x] Implemented point scaling using existing config formulas
  - [x] Created nested JSON configuration structure for perk trees
- [x] **Phase 2: Core Functionality Implementation**
  - [x] Updated spell tracking to award points instead of percentages
  - [x] Implemented point allocation and deallocation system
  - [x] Created respec functionality for point reallocation
  - [x] Removed all affinity reduction logic
  - [x] Updated commands to work with new system
- [x] **Phase 3: UI Overhaul**
  - [x] Created `PerkTreeScreen` for individual school perk trees
  - [x] Updated `AffinityScreen` to navigate to perk trees
  - [x] Implemented Minecraft-style perk tree UI with dragging and tooltips
  - [x] Added confirmation dialogs for respec functionality
  - [x] Fixed critical rendering bugs and performance issues
- [x] **Perk Tree Configurations**
  - [x] Created `fire.json` - 4 perks (3 levels Fire Thorns + 1 Fire Dash)
  - [x] Created `water.json` - 7 perks (3 levels Cold Walker + 3 levels Hydration + 1 Ice Blast)
  - [x] Created `earth.json` - 4 perks (3 levels Stone Skin + 1 Ground Slam)
  - [x] Created `air.json` - 4 perks (3 levels Free Jump + 1 Air Dash)
  - [x] Created `abjuration.json` - 4 perks (3 levels Deflection + 1 Sanctuary)
  - [x] Created `necromancy.json` - 7 perks (Summon Health/Power/Defense + Lich Feast + Curse Field)
  - [x] Created `conjuration.json` - 4 perks (3 levels Unstable Summoning + 1 Swap Ability)
  - [x] Created `manipulation.json` - 7 perks (Mana Tap + Ghost Step + Soulspike + Active Ghost Step)

### In Progress:
- [ ] Data migration system from old to new format
- [ ] Comprehensive testing of the new system

### Next Steps:
1. Create data migration system from old to new format
2. Comprehensive testing of all functionality
3. Performance optimization if needed
4. Documentation updates

## Recent Changes

### Removed Potions and Affinity Anchor Charm
- [x] **Removed all affinity potions**: Deleted all potion effects and potions that increased affinity percentages
- [x] **Removed Affinity Anchor Charm**: Deleted the charm item that prevented affinity changes
- [x] **Cleaned up related files**: Removed all associated models, recipes, language entries, and configuration options
- [x] **Updated spell tracking**: Removed anchor charm checks from spell tracking mixin
- [x] **Simplified data generation**: Removed potion and charm recipe/model generation

This removal aligns with the migration away from pure affinity-based progression to the new point-buy perk system.

### Adjusted Point Gain Rate
- [x] **Reduced affinity gain multiplier**: Changed from 0.0025 to 0.0001 (25x reduction)
- [x] **Balanced progression**: Players now need ~10,000 mana worth of spells per point gained
- [x] **Meaningful progression**: Maxing a school (4-8 perks) requires significant spell usage

This ensures the perk system provides meaningful progression without being too easy to max out.

### Implemented Global Scaling System
- [x] **Added global scaling configuration**: New config options for global scaling decay strength and minimum factor
- [x] **Implemented cross-school scaling**: The more total points you have across all schools, the harder it becomes to gain points in any school
- [x] **Encourages specialization**: Players can still spread points across schools, but specializing in one school is more efficient
- [x] **Configurable scaling**: Server admins can adjust how aggressive the global scaling is
- [x] **Combined scaling**: Both school-specific and global scaling work together multiplicatively

This creates meaningful choices between specializing in one school vs. spreading points across multiple schools, encouraging but not mandating specialization.

### Enhanced Reset Command
- [x] **Updated reset command syntax**: Now supports `/ars-affinity reset <school|all>` instead of just resetting all
- [x] **Created reusable chat message system**: ChatMessageHelper class for consistent messaging
- [x] **Added specific school reset**: Players can reset individual schools with `/ars-affinity reset fire`
- [x] **Added all schools reset**: Players can reset all schools with `/ars-affinity reset all`
- [x] **Enhanced feedback**: Chat messages show exactly how many points were reset
- [x] **Prepared for consumables**: Chat message system is designed to be reusable for future consumable items

This makes the reset functionality more flexible and prepares the foundation for consumable reset items.

### Tablet of Amnesia
- [x] **Created Tablet of Amnesia item**: A consumable tablet that resets affinity progress in a specific school
- [x] **Essence-based school detection**: Tablet must be augmented with an essence to determine which school to reset
- [x] **School-specific recipes**: Separate recipes for each school using their respective essences
- [x] **Reusable chat messaging**: Uses the same ChatMessageHelper system as the reset command
- [x] **Proper item properties**: Consumable food item with appropriate tooltips and usage instructions
- [x] **Complete asset pipeline**: Textures, models, recipes, and language entries all created

**Usage:**
1. Craft base tablet using book + 4 paper (100 source cost)
2. Augment with 4 essences of desired school (200 source cost)
3. Right-click to consume and reset that school's affinity to 0 points

This provides players with a consumable way to reset specific schools, encouraging strategic specialization choices.

## Notes
- Keep existing perk effects, just change how they're allocated
- Maintain backward compatibility during transition
- Consider adding perk categories (offensive, defensive, utility)
- Plan for future school additions
- Ensure performance with large perk trees
