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
    
    private final Map<SpellSchool, Integer> affinities = new HashMap<>();
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
            affinities.put(school, 0);
        }
    }
    
    public int getAffinity(SpellSchool school) {
        return affinities.getOrDefault(school, 0);
    }
    
    public void setAffinity(SpellSchool school, int level) {
        int oldLevel = affinities.getOrDefault(school, 0);
        affinities.put(school, level);
        
        rebuildPerkIndex();
        
        ArsAffinity.LOGGER.debug("Affinity changed for {}: {} -> {}", 
            school.getId().toString().replace(":", "_"), oldLevel, level);
    }
    
    public Map<SpellSchool, Integer> getAllAffinities() {
        return new HashMap<>(affinities);
    }
    
    public void normalizeAffinities(int maxTotal) {
        int currentTotal = affinities.values().stream().mapToInt(Integer::intValue).sum();
        if (currentTotal > maxTotal) {
            double scale = (double) maxTotal / currentTotal;
            affinities.replaceAll((school, level) -> (int) (level * scale));
            
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
        
        for (Map.Entry<SpellSchool, Integer> entry : affinities.entrySet()) {
            SpellSchool school = entry.getKey();
            int affinity = entry.getValue();
            
            for (int tier = 5; tier >= 1; tier--) {
                if (affinity >= tier) {
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
        }
        
        return bestPerk;
    }
    
    public int getTier(SpellSchool school) {
        int affinity = getAffinity(school);
        return Math.min(5, Math.max(0, affinity));
    }
    
    public SpellSchool getPrimarySchool() {
        SpellSchool primary = null;
        int maxAffinity = 0;
        
        for (Map.Entry<SpellSchool, Integer> entry : affinities.entrySet()) {
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
            int currentAffinity = getAffinity(school);
            int newAffinity = Math.max(0, currentAffinity + (int) change);
            setAffinity(school, newAffinity);
        }
        
        normalizeAffinities(100);
    }
    
    public int getTotalAffinity() {
        return affinities.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        CompoundTag affinitiesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Integer> entry : affinities.entrySet()) {
            affinitiesTag.putInt(entry.getKey().getId().toString(), entry.getValue());
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
            int level = affinitiesTag.getInt(key);
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