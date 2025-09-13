package com.github.ars_affinity.perk;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;


public abstract class AffinityPerk {
    public AffinityPerkType perk;
    public boolean isBuff;

    protected AffinityPerk(AffinityPerkType perk, boolean isBuff) {
        this.perk = perk;
        this.isBuff = isBuff;
    }

    // Static factory method for GSON deserialization
    public static AffinityPerk fromJson(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String perkTypeStr = jsonObject.get("perk").getAsString();
        
        try {
            AffinityPerkType perkType = AffinityPerkType.valueOf(perkTypeStr);
            boolean isBuff = jsonObject.get("isBuff").getAsBoolean();

            switch (perkType) {
                case PASSIVE_DOUSED:
                case PASSIVE_DEHYDRATED:
                case PASSIVE_BURIED:
                case PASSIVE_GROUNDED:
                case PASSIVE_FIRE_THORNS:
                case PASSIVE_MANA_TAP:
                case PASSIVE_HEALING_AMPLIFICATION:
                case PASSIVE_BLIGHTED:
                case PASSIVE_FREE_JUMP:
                case PASSIVE_PACIFIST:
                case PASSIVE_SOULSPIKE:
                    float amount = jsonObject.get("amount").getAsFloat();
                    return new AmountBasedPerk(perkType, amount, isBuff);
                case PASSIVE_COLD_WALKER:
                    float perkAmount = jsonObject.get("amount").getAsFloat();
                    return new AmountBasedPerk(perkType, perkAmount, isBuff);
                case PASSIVE_DEFLECTION:
                case PASSIVE_SUMMON_HEALTH:
                case PASSIVE_SUMMONING_POWER:
                case PASSIVE_SUMMON_DEFENSE:
                case PASSIVE_STONE_SKIN:
                    float durationAmount = jsonObject.get("amount").getAsFloat();
                    int durationTime = jsonObject.get("time").getAsInt();
                    return new DurationBasedPerk(perkType, durationAmount, durationTime, isBuff);
                case PASSIVE_LICH_FEAST:
                    float healthRestore = jsonObject.get("health").getAsFloat();
                    float hungerRestore = jsonObject.get("hunger").getAsFloat();
                    return new LichFeastPerk(perkType, healthRestore, hungerRestore, isBuff);
                case PASSIVE_MOB_PACIFICATION:
                    java.util.List<String> entities = context.deserialize(jsonObject.get("entities"), java.util.List.class);
                    return new EntityBasedPerk(perkType, isBuff, entities);
                case ACTIVE_ICE_BLAST:
                    float manaCost = jsonObject.get("manaCost").getAsFloat();
                    int cooldown = jsonObject.get("cooldown").getAsInt();
                    float damage = jsonObject.get("damage").getAsFloat();
                    int freezeTime = jsonObject.get("freezeTime").getAsInt();
                    float radius = jsonObject.get("radius").getAsFloat();
                    return new ActiveAbilityPerk(perkType, manaCost, cooldown, damage, freezeTime, radius, isBuff);
                case ACTIVE_SWAP_ABILITY:
                    float swapManaCost = jsonObject.get("manaCost").getAsFloat();
                    int swapCooldown = jsonObject.get("cooldown").getAsInt();
                    return new ActiveAbilityPerk(perkType, swapManaCost, swapCooldown, 0.0f, 0, 0.0f, isBuff);
                case ACTIVE_GROUND_SLAM:
                    float activeManaCost = jsonObject.get("manaCost").getAsFloat();
                    int activeCooldown = jsonObject.get("cooldown").getAsInt();
                    return new ActiveAbilityPerk(perkType, activeManaCost, activeCooldown, 0.0f, 0, 0.0f, isBuff);
                case ACTIVE_AIR_DASH:
                case ACTIVE_FIRE_DASH:
                case ACTIVE_GHOST_STEP:
                    float dashManaCost = jsonObject.get("manaCost").getAsFloat();
                    int dashCooldown = jsonObject.get("cooldown").getAsInt();
                    float dashLength = jsonObject.get("dashLength").getAsFloat();
                    float dashDuration = jsonObject.get("dashDuration").getAsFloat();
                    return new ActiveAbilityPerk(perkType, dashManaCost, dashCooldown, 0.0f, 0, 0.0f, dashLength, dashDuration, isBuff);
                case ACTIVE_SANCTUARY:
                    float sanctMana = jsonObject.get("manaCost").getAsFloat();
                    int sanctCd = jsonObject.get("cooldown").getAsInt();
                    return new ActiveAbilityPerk(perkType, sanctMana, sanctCd, 0.0f, 0, 0.0f, isBuff);
                case ACTIVE_CURSE_FIELD:
                    float curseMana = jsonObject.get("manaCost").getAsFloat();
                    int curseCd = jsonObject.get("cooldown").getAsInt();
                    return new ActiveAbilityPerk(perkType, curseMana, curseCd, 0.0f, 0, 0.0f, isBuff);
                case ACTIVE_SWARM:
                    float swarmMana = jsonObject.get("manaCost").getAsFloat();
                    int swarmCd = jsonObject.get("cooldown").getAsInt();
                    return new ActiveAbilityPerk(perkType, swarmMana, swarmCd, 0.0f, 0, 0.0f, isBuff);
                case PASSIVE_UNSTABLE_SUMMONING:
                    float chance = jsonObject.get("chance").getAsFloat();
                    java.util.List<String> possibleEntities = context.deserialize(jsonObject.get("entities"), java.util.List.class);
                    return new UnstableSummoningPerk(perkType, chance, possibleEntities, isBuff);
                case PASSIVE_GHOST_STEP:
                    float healAmount = jsonObject.get("amount").getAsFloat();
                    int invisibilityTime = jsonObject.get("time").getAsInt();
                    int cooldownTime = jsonObject.get("cooldown").getAsInt();
                    return new GhostStepPerk(perkType, healAmount, invisibilityTime, cooldownTime, isBuff);
                case PASSIVE_MANIPULATION_SICKNESS:
                    int sicknessDuration = jsonObject.get("duration").getAsInt();
                    int hungerAmount = jsonObject.get("hunger").getAsInt();
                    return new ManipulationSicknessPerk(perkType, sicknessDuration, hungerAmount, isBuff);
                case PASSIVE_HYDRATION:
                    float maxAmplification = jsonObject.get("amount").getAsFloat();
                    int countdownTicks = jsonObject.get("time").getAsInt();
                    return new DurationBasedPerk(perkType, maxAmplification, countdownTicks, isBuff);
                case PASSIVE_MID_AIR_PHASING:
                    int phasingCooldown = jsonObject.get("cooldown").getAsInt();
                    return new DurationBasedPerk(perkType, 0.0f, phasingCooldown, isBuff);
                case PASSIVE_BREEZE_RETALIATION:
                    float retaliationChance = jsonObject.get("chance").getAsFloat();
                    return new AmountBasedPerk(perkType, retaliationChance, isBuff);
                case PASSIVE_STATIC_CHARGE:
                    int buildUpTime = jsonObject.get("buildUpTime").getAsInt();
                    float damage = jsonObject.get("damage").getAsFloat();
                    float aoeRadius = jsonObject.get("aoeRadius").getAsFloat();
                    return new StaticChargePerk(perkType, buildUpTime, damage, aoeRadius, isBuff);
                default:
                    throw new JsonParseException("Unknown perk type: " + perkTypeStr);
            }
        } catch (IllegalArgumentException e) {
            throw new JsonParseException("Invalid perk type: '" + perkTypeStr + "'. Valid types are: " + 
                java.util.Arrays.toString(AffinityPerkType.values()), e);
        }
    }

    public static class AmountBasedPerk extends AffinityPerk {
        public float amount;

        public AmountBasedPerk(AffinityPerkType perk, float amount, boolean isBuff) {
            super(perk, isBuff);
            this.amount = amount;
        }
    }

    public static class DurationBasedPerk extends AffinityPerk {
        public float amount;
        public int time;

        public DurationBasedPerk(AffinityPerkType perk, float amount, int time, boolean isBuff) {
            super(perk, isBuff);
            this.amount = amount;
            this.time = time;
        }
    }

    public static class TupleAmountPerk extends AffinityPerk {
        public float amount1;
        public float amount2;

        public TupleAmountPerk(AffinityPerkType perk, float amount1, float amount2, boolean isBuff) {
            super(perk, isBuff);
            this.amount1 = amount1;
            this.amount2 = amount2;
        }
    }

    public static class EntityBasedPerk extends AffinityPerk {
        public java.util.List<String> entities;

        public EntityBasedPerk(AffinityPerkType perk, boolean isBuff, java.util.List<String> entities) {
            super(perk, isBuff);
            this.entities = entities;
        }
    }

    public static class LichFeastPerk extends AffinityPerk {
        public float health;
        public float hunger;

        public LichFeastPerk(AffinityPerkType perk, float health, float hunger, boolean isBuff) {
            super(perk, isBuff);
            this.health = health;
            this.hunger = hunger;
        }
    }

    public static class ActiveAbilityPerk extends AffinityPerk {
        public float manaCost;
        public int cooldown;
        public float damage;
        public int freezeTime;
        public float radius;
        public float dashLength;
        public float dashDuration;

        public ActiveAbilityPerk(AffinityPerkType perk, float manaCost, int cooldown, float damage, int freezeTime, float radius, boolean isBuff) {
            super(perk, isBuff);
            this.manaCost = manaCost;
            this.cooldown = cooldown;
            this.damage = damage;
            this.freezeTime = freezeTime;
            this.radius = radius;
            this.dashLength = 0.0f;
            this.dashDuration = 0.0f;
        }

        public ActiveAbilityPerk(AffinityPerkType perk, float manaCost, int cooldown, float damage, int freezeTime, float radius, float dashLength, float dashDuration, boolean isBuff) {
            super(perk, isBuff);
            this.manaCost = manaCost;
            this.cooldown = cooldown;
            this.damage = damage;
            this.freezeTime = freezeTime;
            this.radius = radius;
            this.dashLength = dashLength;
            this.dashDuration = dashDuration;
        }
    }

    public static class UnstableSummoningPerk extends AffinityPerk {
        public float chance;
        public java.util.List<String> entities;

        public UnstableSummoningPerk(AffinityPerkType perk, float chance, java.util.List<String> entities, boolean isBuff) {
            super(perk, isBuff);
            this.chance = chance;
            this.entities = entities;
        }
    }

    public static class GhostStepPerk extends AffinityPerk {
        public float amount;
        public int time;
        public int cooldown;

        public GhostStepPerk(AffinityPerkType perk, float amount, int time, int cooldown, boolean isBuff) {
            super(perk, isBuff);
            this.amount = amount;
            this.time = time;
            this.cooldown = cooldown;
        }
    }

    public static class ManipulationSicknessPerk extends AffinityPerk {
        public int duration;
        public int hunger;

        public ManipulationSicknessPerk(AffinityPerkType perk, int duration, int hunger, boolean isBuff) {
            super(perk, isBuff);
            this.duration = duration;
            this.hunger = hunger;
        }
    }

    public static class StaticChargePerk extends AffinityPerk {
        public int buildUpTime;
        public float damage;
        public float aoeRadius;

        public StaticChargePerk(AffinityPerkType perk, int buildUpTime, float damage, float aoeRadius, boolean isBuff) {
            super(perk, isBuff);
            this.buildUpTime = buildUpTime;
            this.damage = damage;
            this.aoeRadius = aoeRadius;
        }
    }
    

}
