# Creating Passive Perks for Ars Affinity

This guide explains how to create a new passive perk for Ars Affinity. Passive perks are abilities that activate automatically based on certain conditions or events.

## Overview

A passive perk consists of several components:
1. **Perk Definition** - The perk class and configuration
2. **Language File** - User-facing description
3. **Config Files** - Tier-based configuration
4. **Event Handler** - The actual functionality
5. **Registration** - Adding the perk to the system

## Step 1: Define the Perk Type

First, add your new perk type to the `AffinityPerkType` enum in `src/main/java/com/github/ars_affinity/perk/AffinityPerkType.java`:

```java
public enum AffinityPerkType {
    // ... existing perks ...
    PASSIVE_YOUR_NEW_PERK,
}
```

## Step 2: Create the Perk Class

Create a new perk class in `src/main/java/com/github/ars_affinity/perk/`:

### For Simple Amount-Based Perks:
```java
public class YourNewPerk extends AffinityPerk {
    public final float amount;
    
    public YourNewPerk(float amount) {
        super(AffinityPerkType.PASSIVE_YOUR_NEW_PERK);
        this.amount = amount;
    }
    
    @Override
    public String getDescription() {
        return String.format("Your perk description with %d%% effect", (int)(amount * 100));
    }
}
```

### For Complex Perks:
```java
public class ComplexPerk extends AffinityPerk {
    public final float health;
    public final float hunger;
    
    public ComplexPerk(float health, float hunger) {
        super(AffinityPerkType.PASSIVE_YOUR_NEW_PERK);
        this.health = health;
        this.hunger = hunger;
    }
    
    @Override
    public String getDescription() {
        return String.format("Restore %.1f health and %.1f hunger", health, hunger);
    }
}
```

## Step 3: Add Language Entry

Add a description to `src/main/resources/assets/ars_affinity/lang/en_us.json`:

```json
{
  "ars_affinity.perk.PASSIVE_YOUR_NEW_PERK": "Your perk description with %d%% effect"
}
```

## Step 4: Create Config Files

Create tier-based configuration files in `config/ars_affinity/perks/[school]/`:

### Tier 1 (`1.json`):
```json
[
  {
    "perk": "PASSIVE_YOUR_NEW_PERK",
    "amount": 0.25,
    "isBuff": true
  }
]
```

### Tier 2 (`2.json`):
```json
[
  {
    "perk": "PASSIVE_YOUR_NEW_PERK",
    "amount": 0.50,
    "isBuff": true
  }
]
```

### Tier 3 (`3.json`):
```json
[
  {
    "perk": "PASSIVE_YOUR_NEW_PERK",
    "amount": 0.75,
    "isBuff": true
  }
]
```

## Step 5: Implement the Event Handler

Create an event handler in `src/main/java/com/github/ars_affinity/event/`:

```java
package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class YourNewPerkEvents {

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) {
            return;
        }
        if (player.level().isClientSide()) return;

        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // Check all schools for your perk
            AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_YOUR_NEW_PERK, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    // Your perk logic here
                    float bonus = event.getAmount() * amountPerk.amount;
                    event.setAmount(event.getAmount() + bonus);
                    
                    ArsAffinity.LOGGER.info("Applied {}% bonus damage for player {}", 
                        (int)(amountPerk.amount * 100), player.getName().getString());
                }
            });
        }
    }
}
```

## Step 6: Register the Perk

Add your perk to the perk registry in `AffinityPerkHelper.java`:

```java
public static AffinityPerk createPerk(AffinityPerkType type, JsonObject json) {
    return switch (type) {
        // ... existing cases ...
        case PASSIVE_YOUR_NEW_PERK -> {
            float amount = json.get("amount").getAsFloat();
            yield new YourNewPerk(amount);
        }
    };
}
```

## Common Event Types

Here are some common events you might want to listen to:

### Spell Events:
- `SpellCastEvent` - When a spell is cast
- `SpellResolveEvent.Pre/Post` - Before/after spell resolution
- `EffectResolveEvent.Pre/Post` - Before/after individual effects

### Combat Events:
- `LivingDamageEvent` - When damage is dealt
- `LivingHurtEvent` - When an entity is hurt
- `LivingDeathEvent` - When an entity dies

### Player Events:
- `LivingHealEvent` - When healing occurs
- `PlayerTickEvent` - Every tick for players
- `LivingFallEvent` - When falling

### World Events:
- `LivingTickEvent` - Every tick for living entities
- `BlockEvent` - Block-related events

## Example: Damage Amplification Perk

Here's a complete example of a damage amplification perk:

### 1. Perk Type:
```java
PASSIVE_DAMAGE_AMPLIFICATION,
```

### 2. Perk Class:
```java
public class DamageAmplificationPerk extends AffinityPerk {
    public final float amount;
    
    public DamageAmplificationPerk(float amount) {
        super(AffinityPerkType.PASSIVE_DAMAGE_AMPLIFICATION);
        this.amount = amount;
    }
    
    @Override
    public String getDescription() {
        return String.format("Amplifies spell damage by %d%%", (int)(amount * 100));
    }
}
```

### 3. Language Entry:
```json
"ars_affinity.perk.PASSIVE_DAMAGE_AMPLIFICATION": "Amplifies spell damage by %d%%"
```

### 4. Config Files:
```json
// Tier 1
[{"perk": "PASSIVE_DAMAGE_AMPLIFICATION", "amount": 0.25, "isBuff": true}]

// Tier 2  
[{"perk": "PASSIVE_DAMAGE_AMPLIFICATION", "amount": 0.50, "isBuff": true}]

// Tier 3
[{"perk": "PASSIVE_DAMAGE_AMPLIFICATION", "amount": 0.75, "isBuff": true}]
```

### 5. Event Handler:
```java
@SubscribeEvent
public static void onSpellDamage(SpellDamageEvent.Pre event) {
    if (!(event.caster instanceof Player player)) return;
    if (player.level().isClientSide()) return;

    var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
    if (progress != null) {
        AffinityPerkHelper.applyAllHighestTierPerks(progress, AffinityPerkType.PASSIVE_DAMAGE_AMPLIFICATION, perk -> {
            if (perk instanceof DamageAmplificationPerk damagePerk) {
                float bonus = event.damage * damagePerk.amount;
                event.damage += bonus;
            }
        });
    }
}
```

## Best Practices

1. **Always check for client-side** - Use `player.level().isClientSide()` to avoid client-side execution
2. **Use proper event priorities** - Use `@SubscribeEvent(priority = EventPriority.LOW)` for modifications
3. **Log important actions** - Use `ArsAffinity.LOGGER.info()` for debugging
4. **Handle null cases** - Always check for null before accessing objects
5. **Use appropriate events** - Choose the right event for your use case
6. **Follow naming conventions** - Use `PASSIVE_` prefix for passive perks
7. **Test thoroughly** - Test your perk with different configurations

## Testing Your Perk

1. **Build the mod** - `./gradlew build`
2. **Run in development** - `./gradlew runClient`
3. **Test different tiers** - Verify the perk works at all tiers
4. **Test edge cases** - Test with null values, different scenarios
5. **Check logs** - Verify your logging messages appear correctly

## Troubleshooting

### Common Issues:
- **Perk not activating** - Check event registration and conditions
- **Wrong values** - Verify config file format and perk creation
- **Client-side errors** - Ensure client-side checks are in place
- **Missing language** - Check language file entries
- **Build errors** - Verify all imports and class references

### Debug Tips:
- Add logging to track perk activation
- Use breakpoints in your IDE
- Check the game logs for errors
- Verify config files are in the right location

This guide should help you create new passive perks for Ars Affinity. Remember to follow the existing patterns and test thoroughly! 