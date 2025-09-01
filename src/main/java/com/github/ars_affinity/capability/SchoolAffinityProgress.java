package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.event.TierChangeEvent;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public class SchoolAffinityProgress implements INBTSerializable<CompoundTag> {
    
    private final Map<SpellSchool, Float> schoolAffinities = new HashMap<>();
    private final Map<SpellSchool, Integer> schoolTiers = new HashMap<>();
    private Player player; // Reference to the player for event firing
    
    public SchoolAffinityProgress() {
        initializeDefaultAffinities();
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    private void initializeDefaultAffinities() {
        float defaultAffinity = 1.0f / 8.0f;
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            schoolAffinities.put(school, defaultAffinity);
            schoolTiers.put(school, calculateTier(defaultAffinity));
        }
    }
    
    public float getAffinity(SpellSchool school) {
        return schoolAffinities.getOrDefault(school, 0.0f);
    }
    
    public int getTier(SpellSchool school) {
        return schoolTiers.getOrDefault(school, 0);
    }
    
    public void setAffinity(SpellSchool school, float value) {
        float clampedValue = Math.max(0.0f, Math.min(1.0f, value));
        int oldTier = getTier(school);
        schoolAffinities.put(school, clampedValue);
        int newTier = calculateTier(clampedValue);
        schoolTiers.put(school, newTier);
        
        // Fire event if tier changed
        if (oldTier != newTier && player != null) {
            NeoForge.EVENT_BUS.post(new TierChangeEvent(player, school, oldTier, newTier));
        }
    }
    
    private int calculateTier(float affinity) {
        return SchoolRelationshipHelper.calculateTierFromAffinity(affinity);
    }
    
    public void applyChanges(Map<SpellSchool, Float> changes) {
        for (Map.Entry<SpellSchool, Float> entry : changes.entrySet()) {
            SpellSchool school = entry.getKey();
            float change = entry.getValue();
            float currentAffinity = getAffinity(school);
            float newAffinity = Math.max(0.0f, Math.min(1.0f, currentAffinity + change));
            setAffinity(school, newAffinity);
        }
        
        normalizeAffinities();
    }
    
    private void normalizeAffinities() {
        float total = 0.0f;
        for (float affinity : schoolAffinities.values()) {
            total += affinity;
        }
        
        if (total > 0.0f) {
            for (SpellSchool school : schoolAffinities.keySet()) {
                float normalized = schoolAffinities.get(school) / total;
                int oldTier = getTier(school);
                schoolAffinities.put(school, normalized);
                int newTier = calculateTier(normalized);
                schoolTiers.put(school, newTier);
                
                // Fire event if tier changed during normalization
                if (oldTier != newTier && player != null) {
                    NeoForge.EVENT_BUS.post(new TierChangeEvent(player, school, oldTier, newTier));
                }
            }
        }
    }
    
    public float getTotalAffinity() {
        float total = 0.0f;
        for (float affinity : schoolAffinities.values()) {
            total += affinity;
        }
        return total;
    }
    
    public SpellSchool getPrimarySchool() {
        SpellSchool primary = null;
        float maxAffinity = 0.0f;
        
        for (Map.Entry<SpellSchool, Float> entry : schoolAffinities.entrySet()) {
            if (entry.getValue() > maxAffinity) {
                maxAffinity = entry.getValue();
                primary = entry.getKey();
            }
        }
        
        return primary;
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        CompoundTag affinitiesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Float> entry : schoolAffinities.entrySet()) {
            affinitiesTag.putFloat(entry.getKey().getId(), entry.getValue());
        }
        tag.put("schoolAffinities", affinitiesTag);
        
        CompoundTag tiersTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Integer> entry : schoolTiers.entrySet()) {
            tiersTag.putInt(entry.getKey().getId(), entry.getValue());
        }
        tag.put("schoolTiers", tiersTag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        schoolAffinities.clear();
        schoolTiers.clear();
        
        if (tag.contains("schoolAffinities", Tag.TAG_COMPOUND)) {
            CompoundTag affinitiesTag = tag.getCompound("schoolAffinities");
            for (String key : affinitiesTag.getAllKeys()) {
                SpellSchool school = getSpellSchoolFromString(key);
                if (school != null) {
                    float affinity = affinitiesTag.getFloat(key);
                    schoolAffinities.put(school, affinity);
                    schoolTiers.put(school, calculateTier(affinity));
                } else {
                    ArsAffinity.LOGGER.warn("Unknown spell school in NBT: {}", key);
                }
            }
        }
        
        if (tag.contains("schoolTiers", Tag.TAG_COMPOUND)) {
            CompoundTag tiersTag = tag.getCompound("schoolTiers");
            for (String key : tiersTag.getAllKeys()) {
                SpellSchool school = getSpellSchoolFromString(key);
                if (school != null) {
                    int tier = tiersTag.getInt(key);
                    schoolTiers.put(school, tier);
                }
            }
        }
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            if (!schoolAffinities.containsKey(school)) {
                float defaultAffinity = 1.0f / 8.0f;
                schoolAffinities.put(school, defaultAffinity);
                schoolTiers.put(school, calculateTier(defaultAffinity));
            }
            if (!schoolTiers.containsKey(school)) {
                schoolTiers.put(school, calculateTier(schoolAffinities.get(school)));
            }
        }
        
        normalizeAffinities();
    }
    
    private SpellSchool getSpellSchoolFromString(String name) {
        return switch (name.toLowerCase()) {
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            case "abjuration" -> SpellSchools.ABJURATION;
            case "necromancy" -> SpellSchools.NECROMANCY;
            case "conjuration" -> SpellSchools.CONJURATION;
            case "manipulation" -> SpellSchools.MANIPULATION;
            default -> {
                ArsAffinity.LOGGER.warn("Unknown spell school name: {}", name);
                yield null;
            }
        };
    }
} 