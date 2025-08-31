package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.PerkData;
import com.github.ars_affinity.perk.PerkReference;
import com.github.ars_affinity.perk.PerkRegistry;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchoolAffinityProgress implements INBTSerializable<CompoundTag> {
    
    private final Map<SpellSchool, Float> affinities = new HashMap<>();
    private final Map<AffinityPerkType, PerkReference> activePerks = new HashMap<>();
    private boolean isDirty = false;
    private Player player; // Reference to the player for saving
    
    private static final SpellSchool[] SUPPORTED_SCHOOLS = {
        SpellSchools.ELEMENTAL_FIRE,
        SpellSchools.ELEMENTAL_WATER,
        SpellSchools.ELEMENTAL_EARTH,
        SpellSchools.ELEMENTAL_AIR,
        SpellSchools.ABJURATION,
        SpellSchools.NECROMANCY,
        SpellSchools.CONJURATION,
        SpellSchools.MANIPULATION
    };
    
    public SchoolAffinityProgress() {
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            affinities.put(school, 0.0f);
        }
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    private void markDirty() {
        this.isDirty = true;
        // Data will be saved when player logs out or server stops
    }
    
    public float getAffinity(SpellSchool school) {
        return affinities.getOrDefault(school, 0.0f);
    }
    
    public void setAffinity(SpellSchool school, float affinity) {
        float oldLevel = affinities.getOrDefault(school, 0.0f);
        affinities.put(school, affinity);
        rebuildPerkIndex();
        
        ArsAffinity.LOGGER.debug("Affinity changed for {}: {} -> {}", 
            school.getId().toString().replace(":", "_"), oldLevel, affinity);
        
        // Mark that this progress needs to be saved
        markDirty();
    }
    
    public Map<SpellSchool, Float> getAllAffinities() {
        return new HashMap<>(affinities);
    }
    
    public void normalizeAffinities(float maxTotal) {
        float currentTotal = (float) affinities.values().stream().mapToDouble(Float::doubleValue).sum();
        if (currentTotal > maxTotal) {
            double scale = maxTotal / currentTotal;
            affinities.replaceAll((school, level) -> (float) (level * scale));
            
            rebuildPerkIndex();
        }
    }
    
    public boolean hasActivePerk(AffinityPerkType perkType) {
        return activePerks.containsKey(perkType);
    }
    
    public PerkReference getActivePerkReference(AffinityPerkType perkType) {
        return activePerks.get(perkType);
    }
    

    
    public Set<PerkReference> getAllActivePerkReferences() {
        return Set.copyOf(activePerks.values());
    }
    

    
    private void rebuildPerkIndex() {
        activePerks.clear();
        ArsAffinity.LOGGER.info("Rebuilding perk index for player...");
        
        for (Map.Entry<SpellSchool, Float> entry : affinities.entrySet()) {
            SpellSchool school = entry.getKey();
            float affinity = entry.getValue();
            int tier = getTier(school);
            
            ArsAffinity.LOGGER.info("School: {}, Affinity: {}, Tier: {}", 
                school.getId(), affinity, tier);
            
            if (tier > 0) {
                // Use AffinityPerkManager to get perks from JSON config files
                List<AffinityPerk> perksForTier = AffinityPerkManager.getPerksForLevel(school, tier);
                ArsAffinity.LOGGER.info("Found {} perks for school {} at tier {}", perksForTier.size(), school.getId(), tier);
                
                for (AffinityPerk perk : perksForTier) {
                    ArsAffinity.LOGGER.info("Processing perk: {} for school {} at tier {}", 
                        perk.perk, school.getId(), tier);
                    
                    // Check if we already have a perk of this type with a higher tier
                    PerkReference existing = activePerks.get(perk.perk);
                    if (existing == null || existing.getSourceTier() < tier) {
                        activePerks.put(perk.perk, new PerkReference(perk.perk, school, tier));
                        ArsAffinity.LOGGER.info("Added perk: {} for school {} at tier {}", 
                            perk.perk, school.getId(), tier);
                    }
                }
            }
        }
        
        ArsAffinity.LOGGER.info("Perk index rebuilt. Active perks: {}", activePerks.size());
        for (Map.Entry<AffinityPerkType, PerkReference> entry : activePerks.entrySet()) {
            ArsAffinity.LOGGER.info("Active perk: {} -> {}", entry.getKey(), entry.getValue());
        }
    }
    
    public int getTier(SpellSchool school) {
        float affinity = getAffinity(school);
        float percentage = affinity * 100.0f;
        
        if (percentage >= 75.0f) {
            return 3;
        } else if (percentage >= 40.0f) {
            return 2;
        } else if (percentage >= 20.0f) {
            return 1;
        } else {
            return 0;
        }
    }
    
    public SpellSchool getPrimarySchool() {
        SpellSchool primary = null;
        float maxAffinity = 0.0f;
        
        for (Map.Entry<SpellSchool, Float> entry : affinities.entrySet()) {
            if (entry.getValue() > maxAffinity) {
                maxAffinity = entry.getValue();
                primary = entry.getKey();
            }
        }
        
        return primary;
    }
    
    public void applyChanges(Map<SpellSchool, Float> changes) {
        for (Map.Entry<SpellSchool, Float> entry : changes.entrySet()) {
            SpellSchool school = entry.getKey();
            float change = entry.getValue();
            float currentAffinity = getAffinity(school);
            float newAffinity = Math.max(0.0f, currentAffinity + change);
            setAffinity(school, newAffinity);
        }
        
        normalizeAffinities(100.0f);
    }
    
    public float getTotalAffinity() {
        return (float) affinities.values().stream().mapToDouble(Float::doubleValue).sum();
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        ArsAffinity.LOGGER.info("Starting serialization. Affinities map size: {}, contents: {}", affinities.size(), affinities);
        
        CompoundTag tag = new CompoundTag();
        
        CompoundTag affinitiesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Float> entry : affinities.entrySet()) {
            String schoolId = getShortSchoolId(entry.getKey());
            float affinity = entry.getValue();
            ArsAffinity.LOGGER.info("Serializing affinity: school={}, shortId={}, affinity={}", 
                entry.getKey(), schoolId, affinity);
            affinitiesTag.putFloat(schoolId, affinity);
        }
        tag.put("affinities", affinitiesTag);
        
        ListTag perksTag = new ListTag();
        for (PerkReference reference : activePerks.values()) {
            CompoundTag perkTag = reference.serializeNBT();
            perksTag.add(perkTag);
            ArsAffinity.LOGGER.debug("Serializing perk: {} -> {}", reference.getPerkType(), perkTag);
        }
        tag.put("activePerks", perksTag);
        
        ArsAffinity.LOGGER.info("Serializing SchoolAffinityProgress: {} affinities, {} perks", 
            affinities.size(), activePerks.size());
        ArsAffinity.LOGGER.debug("Serialized NBT: {}", tag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        ArsAffinity.LOGGER.info("Deserializing NBT: {}", tag);
        
        CompoundTag affinitiesTag = tag.getCompound("affinities");
        ArsAffinity.LOGGER.info("Affinities tag: {} (keys: {})", affinitiesTag, affinitiesTag.getAllKeys());
        affinities.clear();
        for (String key : affinitiesTag.getAllKeys()) {
            ArsAffinity.LOGGER.info("Processing affinity key: '{}'", key);
            SpellSchool school = getSpellSchoolFromId(key);
            if (school != null) {
                float level = affinitiesTag.getFloat(key);
                affinities.put(school, level);
                ArsAffinity.LOGGER.info("Deserialized affinity: {} = {} -> school: {}", key, level, school);
            } else {
                ArsAffinity.LOGGER.error("Skipping affinity for unknown school ID: '{}'", key);
            }
        }
        
        ListTag perksTag = tag.getList("activePerks", Tag.TAG_COMPOUND);
        ArsAffinity.LOGGER.debug("Perks tag: {} (size: {})", perksTag, perksTag.size());
        activePerks.clear();
        for (Tag perkTag : perksTag) {
            if (perkTag instanceof CompoundTag compoundTag) {
                ArsAffinity.LOGGER.debug("Deserializing perk tag: {}", compoundTag);
                PerkReference reference = PerkReference.deserializeNBT(compoundTag);
                activePerks.put(reference.getPerkType(), reference);
                ArsAffinity.LOGGER.debug("Deserialized perk: {} -> {}", reference.getPerkType(), reference);
            }
        }
        
        ArsAffinity.LOGGER.info("Deserialized SchoolAffinityProgress: {} affinities, {} perks", 
            affinities.size(), activePerks.size());
        
        // Don't rebuild perk index when loading from NBT - preserve the loaded data
    }
    
    private static SpellSchool getSpellSchoolFromId(String id) {
        SpellSchool school = switch (id) {
            // Full format (current)
            case "ars_nouveau:elemental_fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "ars_nouveau:elemental_water" -> SpellSchools.ELEMENTAL_WATER;
            case "ars_nouveau:elemental_earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "ars_nouveau:elemental_air" -> SpellSchools.ELEMENTAL_AIR;
            case "ars_nouveau:abjuration" -> SpellSchools.ABJURATION;
            case "ars_nouveau:necromancy" -> SpellSchools.NECROMANCY;
            case "ars_nouveau:conjuration" -> SpellSchools.CONJURATION;
            case "ars_nouveau:manipulation" -> SpellSchools.MANIPULATION;
            // Short format (legacy - for backward compatibility)
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            default -> null;
        };
        
        if (school == null) {
            ArsAffinity.LOGGER.error("Unknown spell school ID: '{}'. This will cause data loss!", id);
            // Don't return a default school - let the caller handle this
            return null;
        }
        
        return school;
    }
    
    private static String getShortSchoolId(SpellSchool school) {
        String schoolId = school.getId().toString();
        return switch (schoolId) {
            case "ars_nouveau:elemental_fire" -> "fire";
            case "ars_nouveau:elemental_water" -> "water";
            case "ars_nouveau:elemental_earth" -> "earth";
            case "ars_nouveau:elemental_air" -> "air";
            case "ars_nouveau:abjuration" -> "abjuration";
            case "ars_nouveau:necromancy" -> "necromancy";
            case "ars_nouveau:conjuration" -> "conjuration";
            case "ars_nouveau:manipulation" -> "manipulation";
            default -> schoolId;
        };
    }
} 