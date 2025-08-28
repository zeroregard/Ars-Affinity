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

/**
 * Capability for tracking a player's progress in different spell schools.
 * Now uses memory-efficient PerkReference system instead of storing full PerkData.
 */
public class SchoolAffinityProgress implements INBTSerializable<CompoundTag> {
    
    // Player's affinity levels for each school
    private final Map<SpellSchool, Integer> affinities = new HashMap<>();
    
    // Memory-efficient perk index - stores only references, not full data
    private final Map<AffinityPerkType, PerkReference> activePerks = new HashMap<>();
    
    // Define the spell schools we want to support
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
        // Initialize all schools with 0 affinity
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            affinities.put(school, 0);
        }
    }
    
    /**
     * Get the affinity level for a specific school.
     */
    public int getAffinity(SpellSchool school) {
        return affinities.getOrDefault(school, 0);
    }
    
    /**
     * Set the affinity level for a specific school.
     * This will trigger a rebuild of the perk index.
     */
    public void setAffinity(SpellSchool school, int level) {
        int oldLevel = affinities.getOrDefault(school, 0);
        affinities.put(school, level);
        
        // Rebuild perk index when affinities change
        rebuildPerkIndex();
        
        ArsAffinity.LOGGER.debug("Affinity changed for {}: {} -> {}", 
            school.getId().toString().replace(":", "_"), oldLevel, level);
    }
    
    /**
     * Get all affinities as a map.
     */
    public Map<SpellSchool, Integer> getAllAffinities() {
        return new HashMap<>(affinities);
    }
    
    /**
     * Normalize affinities to ensure they sum to a maximum value.
     * This will trigger a rebuild of the perk index.
     */
    public void normalizeAffinities(int maxTotal) {
        int currentTotal = affinities.values().stream().mapToInt(Integer::intValue).sum();
        if (currentTotal > maxTotal) {
            double scale = (double) maxTotal / currentTotal;
            affinities.replaceAll((school, level) -> (int) (level * scale));
            
            // Rebuild perk index after normalization
            rebuildPerkIndex();
        }
    }
    
    /**
     * Check if the player has an active perk of the specified type.
     */
    public boolean hasActivePerk(AffinityPerkType perkType) {
        return activePerks.containsKey(perkType);
    }
    
    /**
     * Get the active perk reference for the specified type.
     * @return The PerkReference, or null if no active perk
     */
    public PerkReference getActivePerkReference(AffinityPerkType perkType) {
        return activePerks.get(perkType);
    }
    
    /**
     * Get the full PerkData for the specified type.
     * @return The PerkData from the registry, or null if no active perk
     */
    public PerkData getActivePerk(AffinityPerkType perkType) {
        PerkReference reference = activePerks.get(perkType);
        return reference != null ? reference.getPerkData() : null;
    }
    
    /**
     * Get all active perk references.
     */
    public Set<PerkReference> getAllActivePerkReferences() {
        return Set.copyOf(activePerks.values());
    }
    
    /**
     * Get all active perks as full PerkData objects.
     */
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
    
    /**
     * Rebuild the perk index based on current affinities.
     * This determines which perks are active for the player.
     */
    private void rebuildPerkIndex() {
        Map<AffinityPerkType, PerkReference> newActivePerks = new HashMap<>();
        
        // For each perk type, find the highest tier perk the player qualifies for
        for (AffinityPerkType perkType : AffinityPerkType.values()) {
            PerkReference bestPerk = findBestPerkForType(perkType);
            if (bestPerk != null) {
                newActivePerks.put(perkType, bestPerk);
            }
        }
        
        // Update the active perks
        activePerks.clear();
        activePerks.putAll(newActivePerks);
        
        ArsAffinity.LOGGER.debug("Perk index rebuilt. Active perks: {}", activePerks.size());
    }
    
    /**
     * Find the best (highest tier) perk for a specific type based on current affinities.
     */
    private PerkReference findBestPerkForType(AffinityPerkType perkType) {
        PerkReference bestPerk = null;
        int bestTier = 0;
        
        // Check each school for the highest tier perk the player qualifies for
        for (Map.Entry<SpellSchool, Integer> entry : affinities.entrySet()) {
            SpellSchool school = entry.getKey();
            int affinity = entry.getValue();
            
            // Check each tier from highest to lowest
            for (int tier = 5; tier >= 1; tier--) {
                if (affinity >= tier) {
                    // Check if this perk exists in the registry
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
                        break; // Found the highest tier for this school, move to next school
                    }
                }
            }
        }
        
        return bestPerk;
    }
    
    /**
     * Get the tier for a specific school based on affinity level.
     */
    public int getTier(SpellSchool school) {
        int affinity = getAffinity(school);
        // Simple tier calculation: 1-5 based on affinity level
        return Math.min(5, Math.max(0, affinity));
    }
    
    /**
     * Get the primary school (highest affinity).
     */
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
    
    /**
     * Apply changes to affinities.
     */
    public void applyChanges(Map<SpellSchool, Float> changes) {
        for (Map.Entry<SpellSchool, Float> entry : changes.entrySet()) {
            SpellSchool school = entry.getKey();
            float change = entry.getValue();
            int currentAffinity = getAffinity(school);
            int newAffinity = Math.max(0, currentAffinity + (int) change);
            setAffinity(school, newAffinity);
        }
        
        normalizeAffinities(100); // Normalize to max 100 total
    }
    
    /**
     * Get total affinity across all schools.
     */
    public int getTotalAffinity() {
        return affinities.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        // Serialize affinities
        CompoundTag affinitiesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Integer> entry : affinities.entrySet()) {
            affinitiesTag.putInt(entry.getKey().getId().toString(), entry.getValue());
        }
        tag.put("affinities", affinitiesTag);
        
        // Serialize active perk references
        ListTag perksTag = new ListTag();
        for (PerkReference reference : activePerks.values()) {
            perksTag.add(reference.serializeNBT());
        }
        tag.put("activePerks", perksTag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        // Deserialize affinities
        CompoundTag affinitiesTag = tag.getCompound("affinities");
        affinities.clear();
        for (String key : affinitiesTag.getAllKeys()) {
            SpellSchool school = getSpellSchoolFromId(key);
            int level = affinitiesTag.getInt(key);
            affinities.put(school, level);
        }
        
        // Deserialize active perk references
        ListTag perksTag = tag.getList("activePerks", Tag.TAG_COMPOUND);
        activePerks.clear();
        for (Tag perkTag : perksTag) {
            if (perkTag instanceof CompoundTag compoundTag) {
                PerkReference reference = PerkReference.deserializeNBT(compoundTag);
                activePerks.put(reference.getPerkType(), reference);
            }
        }
    }
    
    /**
     * Helper method to get SpellSchool from string ID.
     */
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
            default -> SpellSchools.ELEMENTAL_FIRE; // fallback
        };
    }
} 