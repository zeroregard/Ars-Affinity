package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkData;
import com.github.ars_affinity.perk.PerkReference;
import com.github.ars_affinity.perk.PerkRegistry;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SchoolAffinityProgress implements INBTSerializable<CompoundTag> {
    
    private final Map<SpellSchool, Float> affinities = new HashMap<>();
    private final Map<AffinityPerkType, PerkReference> activePerks = new HashMap<>();
    
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
    
    public float getAffinity(SpellSchool school) {
        return affinities.getOrDefault(school, 0.0f);
    }
    
    public void setAffinity(SpellSchool school, float level) {
        float oldLevel = affinities.getOrDefault(school, 0.0f);
        affinities.put(school, level);
        
        rebuildPerkIndex();
        
        ArsAffinity.LOGGER.debug("Affinity changed for {}: {} -> {}", 
            school.getId().toString().replace(":", "_"), oldLevel, level);
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
    
    public PerkData getActivePerk(AffinityPerkType perkType) {
        PerkReference reference = activePerks.get(perkType);
        return reference != null ? reference.getPerkData() : null;
    }
    
    public Set<PerkReference> getAllActivePerkReferences() {
        return Set.copyOf(activePerks.values());
    }
    
    public Map<AffinityPerkType, PerkData> getAllActivePerks() {
        Map<AffinityPerkType, PerkData> result = new HashMap<>();
        for (Map.Entry<AffinityPerkType, PerkReference> entry : activePerks.entrySet()) {
            PerkData data = entry.getValue().getPerkData();
            if (data != null) {
                result.put(entry.getKey(), data);
            }
        }
        return result;
    }
    
    private void rebuildPerkIndex() {
        Map<AffinityPerkType, PerkReference> newActivePerks = new HashMap<>();
        
        for (AffinityPerkType perkType : AffinityPerkType.values()) {
            PerkReference bestPerk = findBestPerkForType(perkType);
            if (bestPerk != null) {
                newActivePerks.put(perkType, bestPerk);
            }
        }
        
        activePerks.clear();
        activePerks.putAll(newActivePerks);
        
        ArsAffinity.LOGGER.debug("Perk index rebuilt. Active perks: {}", activePerks.size());
    }
    
    private PerkReference findBestPerkForType(AffinityPerkType perkType) {
        PerkReference bestPerk = null;
        int bestTier = 0;
        
        for (Map.Entry<SpellSchool, Float> entry : affinities.entrySet()) {
            SpellSchool school = entry.getKey();
            float affinity = entry.getValue();
            int tier = getTier(school);
            
            if (tier > 0) {
                String key = String.format("%s_%s_%d", 
                    school.getId().toString().toUpperCase().replace(":", "_"), 
                    perkType.name(), 
                    tier);
                
                if (PerkRegistry.hasPerk(key)) {
                    PerkReference candidate = new PerkReference(perkType, school, tier);
                    if (tier > bestTier) {
                        bestPerk = candidate;
                        bestTier = tier;
                    }
                    break;
                }
            }
        }
        
        return bestPerk;
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
        CompoundTag tag = new CompoundTag();
        
        CompoundTag affinitiesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Float> entry : affinities.entrySet()) {
            affinitiesTag.putFloat(entry.getKey().getId().toString(), entry.getValue());
        }
        tag.put("affinities", affinitiesTag);
        
        ListTag perksTag = new ListTag();
        for (PerkReference reference : activePerks.values()) {
            perksTag.add(reference.serializeNBT());
        }
        tag.put("activePerks", perksTag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        CompoundTag affinitiesTag = tag.getCompound("affinities");
        affinities.clear();
        for (String key : affinitiesTag.getAllKeys()) {
            SpellSchool school = getSpellSchoolFromId(key);
            float level = affinitiesTag.getFloat(key);
            affinities.put(school, level);
        }
        
        ListTag perksTag = tag.getList("activePerks", Tag.TAG_COMPOUND);
        activePerks.clear();
        for (Tag perkTag : perksTag) {
            if (perkTag instanceof CompoundTag compoundTag) {
                PerkReference reference = PerkReference.deserializeNBT(compoundTag);
                activePerks.put(reference.getPerkType(), reference);
            }
        }
    }
    
    private static SpellSchool getSpellSchoolFromId(String id) {
        return switch (id) {
            case "ars_nouveau:elemental_fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "ars_nouveau:elemental_water" -> SpellSchools.ELEMENTAL_WATER;
            case "ars_nouveau:elemental_earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "ars_nouveau:elemental_air" -> SpellSchools.ELEMENTAL_AIR;
            case "ars_nouveau:abjuration" -> SpellSchools.ABJURATION;
            case "ars_nouveau:necromancy" -> SpellSchools.NECROMANCY;
            case "ars_nouveau:conjuration" -> SpellSchools.CONJURATION;
            case "ars_nouveau:manipulation" -> SpellSchools.MANIPULATION;
            default -> SpellSchools.ELEMENTAL_FIRE;
        };
    }
} 