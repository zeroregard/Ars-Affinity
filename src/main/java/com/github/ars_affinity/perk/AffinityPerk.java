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
                case PASSIVE_FREE_JUMP:
                case PASSIVE_REVERBERATION:
                    float amount = jsonObject.get("amount").getAsFloat();
                    return new AmountBasedPerk(perkType, amount, isBuff);
                case PASSIVE_BUBBLE_GUARD:
                    float bubbleAmount = jsonObject.get("amount").getAsFloat();
                    int bubbleDuration = jsonObject.get("time").getAsInt();
                    return new DurationBasedPerk(perkType, bubbleAmount, bubbleDuration, isBuff);
                case PASSIVE_SUMMON_HEALTH:
                    float healthAmount = jsonObject.get("amount").getAsFloat();
                    int duration = jsonObject.get("time").getAsInt();
                    return new DurationBasedPerk(perkType, healthAmount, duration, isBuff);
                case PASSIVE_LICH_FEAST:
                    float healthRestore = jsonObject.get("health").getAsFloat();
                    float hungerRestore = jsonObject.get("hunger").getAsFloat();
                    return new LichFeastPerk(perkType, healthRestore, hungerRestore, isBuff);
                case PASSIVE_MOB_PACIFICATION:
                    java.util.List<String> entities = context.deserialize(jsonObject.get("entities"), java.util.List.class);
                    return new EntityBasedPerk(perkType, isBuff, entities);
                case PASSIVE_IGNORE_SKULK:
                    return new SimplePerk(perkType, isBuff);
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

    public static class SimplePerk extends AffinityPerk {
        public SimplePerk(AffinityPerkType perk, boolean isBuff) {
            super(perk, isBuff);
        }
    }
} 