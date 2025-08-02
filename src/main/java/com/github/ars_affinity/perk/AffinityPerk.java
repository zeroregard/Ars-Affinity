package com.github.ars_affinity.perk;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

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
                case PASSIVE_FIRE_THORNS:
                    float amount = jsonObject.get("amount").getAsFloat();
                    return new AmountBasedPerk(perkType, amount, isBuff);
                case PASSIVE_MOB_PACIFICATION:
                    java.util.List<String> entities = context.deserialize(jsonObject.get("entities"), java.util.List.class);
                    return new EntityBasedPerk(perkType, isBuff, entities);
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

    public static class EntityBasedPerk extends AffinityPerk {
        public java.util.List<String> entities;

        public EntityBasedPerk(AffinityPerkType perk, boolean isBuff, java.util.List<String> entities) {
            super(perk, isBuff);
            this.entities = entities;
        }
    }
} 