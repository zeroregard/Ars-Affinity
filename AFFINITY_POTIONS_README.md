# Affinity Potions

This mod adds 8 new consumable potions that increase your affinity towards specific schools of magic by 10% each.

## Available Potions

1. **Potion of Fire Affinity** - Increases Fire school affinity by 10%
2. **Potion of Water Affinity** - Increases Water school affinity by 10%
3. **Potion of Earth Affinity** - Increases Earth school affinity by 10%
4. **Potion of Air Affinity** - Increases Air school affinity by 10%
5. **Potion of Abjuration Affinity** - Increases Abjuration school affinity by 10%
6. **Potion of Necromancy Affinity** - Increases Necromancy school affinity by 10%
7. **Potion of Conjuration Affinity** - Increases Conjuration school affinity by 10%
8. **Potion of Manipulation Affinity** - Increases Manipulation school affinity by 10%

## Crafting

All affinity potions are crafted using Ars Nouveau's **Enchanting Apparatus**:

- **Reagent**: Any mundane potion (minecraft:potion)
- **Pedestal Item**: The corresponding essence from Ars Nouveau
  - Fire Essence → Fire Affinity Potion
  - Water Essence → Water Affinity Potion
  - Earth Essence → Earth Affinity Potion
  - Air Essence → Air Affinity Potion
  - Abjuration Essence → Abjuration Affinity Potion
  - Necromancy Essence → Necromancy Affinity Potion
  - Conjuration Essence → Conjuration Affinity Potion
  - Manipulation Essence → Manipulation Affinity Potion

## Usage

Simply drink the potion to instantly increase your affinity towards that school of magic by 10%. The effect is permanent and stacks with other affinity changes from spell casting.

## Technical Details

- Each potion applies a 1-tick duration effect that triggers once when consumed
- Affinity increases are capped at 100% (1.0) per school
- The potions use the same color scheme as their respective magic schools
- All potions are beneficial effects and can be used in splash and lingering variants

## Recipe Examples

### Fire Affinity Potion
```json
{
  "type": "ars_nouveau:enchanting_apparatus",
  "keepNbtOfReagent": false,
  "pedestalItems": [
    {
      "item": "ars_nouveau:fire_essence"
    }
  ],
  "reagent": {
    "item": "minecraft:potion"
  },
  "result": {
    "count": 1,
    "id": "ars_affinity:fire_affinity"
  },
  "sourceCost": 0
}
```

## Notes

- These potions provide a way to quickly boost specific school affinities without extensive spell casting
- Useful for players who want to specialize in particular schools early on
- The 10% increase is significant enough to be useful but not overpowered
- All recipes use 0 source cost, making them accessible to new players