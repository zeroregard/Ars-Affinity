package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.event.PerkChangeEvent;
import com.github.ars_affinity.event.TierChangeEvent;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkData;
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
import java.util.List;
import java.util.Map;

public class SchoolAffinityProgress implements INBTSerializable<CompoundTag> {
    
    private final Map<SpellSchool, Float> schoolAffinities = new HashMap<>();
    private final Map<SpellSchool, Integer> schoolTiers = new HashMap<>();
    private final Map<AffinityPerkType, PerkData> activePerks = new HashMap<>();
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
            // Rebuild perk index after tier change
            rebuildPerkIndex();
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
            boolean tiersChanged = false;
            for (SpellSchool school : schoolAffinities.keySet()) {
                float normalized = schoolAffinities.get(school) / total;
                int oldTier = getTier(school);
                schoolAffinities.put(school, normalized);
                int newTier = calculateTier(normalized);
                schoolTiers.put(school, newTier);
                
                // Fire event if tier changed during normalization
                if (oldTier != newTier && player != null) {
                    NeoForge.EVENT_BUS.post(new TierChangeEvent(player, school, oldTier, newTier));
                    tiersChanged = true;
                }
            }
            
            // Rebuild perk index if any tiers changed during normalization
            if (tiersChanged) {
                rebuildPerkIndex();
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
    
    /**
     * Get the active perk data for a specific perk type.
     * This provides O(1) access to the highest tier perk of the given type.
     * 
     * @param perkType The type of perk to look up
     * @return The perk data, or null if no perk of this type is active
     */
    public PerkData getActivePerk(AffinityPerkType perkType) {
        return activePerks.get(perkType);
    }
    
    /**
     * Check if a specific perk type is currently active.
     * 
     * @param perkType The type of perk to check
     * @return True if the perk is active, false otherwise
     */
    public boolean hasActivePerk(AffinityPerkType perkType) {
        return activePerks.containsKey(perkType);
    }
    
    /**
     * Get all currently active perks.
     * 
     * @return A map of perk types to their active perk data
     */
    public Map<AffinityPerkType, PerkData> getAllActivePerks() {
        return new HashMap<>(activePerks);
    }
    
    /**
     * Rebuild the perk index after tier changes.
     * This method automatically determines which perks should be active
     * based on the current tier levels and perk configurations.
     */
    private void rebuildPerkIndex() {
        Map<AffinityPerkType, PerkData> oldPerks = new HashMap<>(activePerks);
        activePerks.clear();
        
        // For each perk type, find the best (highest tier) perk available
        for (AffinityPerkType perkType : AffinityPerkType.values()) {
            PerkData bestPerk = findBestPerkForType(perkType);
            if (bestPerk != null) {
                activePerks.put(perkType, bestPerk);
            }
        }
        
        // Fire events for any perks that changed
        firePerkChangeEvents(oldPerks);
    }
    
    /**
     * Find the best (highest tier) perk for a specific perk type.
     * 
     * @param perkType The type of perk to find
     * @return The best perk data, or null if no perk of this type is available
     */
    private PerkData findBestPerkForType(AffinityPerkType perkType) {
        PerkData best = null;
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            int tier = getTier(school);
            if (tier > 0) {
                List<AffinityPerk> tierPerks = AffinityPerkManager.getPerksForCurrentLevel(school, tier);
                for (AffinityPerk perk : tierPerks) {
                    if (perk.perk == perkType) {
                        // Found a perk of this type - check if it's the highest tier
                        if (best == null || tier > best.sourceTier) {
                            best = new PerkData(perk, school, tier);
                        }
                        break; // Found the highest tier perk for this school
                    }
                }
            }
        }
        
        return best;
    }
    
    /**
     * Fire perk change events for any perks that changed during the rebuild.
     * 
     * @param oldPerks The previous perk state
     */
    private void firePerkChangeEvents(Map<AffinityPerkType, PerkData> oldPerks) {
        if (player == null) return;
        
        // Check for perks that were added, removed, or changed
        for (AffinityPerkType perkType : AffinityPerkType.values()) {
            PerkData oldPerk = oldPerks.get(perkType);
            PerkData newPerk = activePerks.get(perkType);
            
            // Only fire event if something actually changed
            if (oldPerk != newPerk) {
                NeoForge.EVENT_BUS.post(new PerkChangeEvent(player, perkType, oldPerk, newPerk));
            }
        }
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
        
        // Serialize active perks index
        CompoundTag perksTag = new CompoundTag();
        for (Map.Entry<AffinityPerkType, PerkData> entry : activePerks.entrySet()) {
            CompoundTag perkTag = new CompoundTag();
            perkTag.putString("perkType", entry.getKey().name());
            perkTag.putString("sourceSchool", entry.getValue().sourceSchool.getId());
            perkTag.putInt("sourceTier", entry.getValue().sourceTier);
            perksTag.put(entry.getKey().name(), perkTag);
        }
        tag.put("activePerks", perksTag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        schoolAffinities.clear();
        schoolTiers.clear();
        activePerks.clear();
        
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
        
        // Deserialize active perks index if available
        if (tag.contains("activePerks", Tag.TAG_COMPOUND)) {
            CompoundTag perksTag = tag.getCompound("activePerks");
            for (String perkTypeName : perksTag.getAllKeys()) {
                try {
                    AffinityPerkType perkType = AffinityPerkType.valueOf(perkTypeName);
                    CompoundTag perkTag = perksTag.getCompound(perkTypeName);
                    String schoolName = perkTag.getString("sourceSchool");
                    int tier = perkTag.getInt("sourceTier");
                    
                    SpellSchool school = getSpellSchoolFromString(schoolName);
                    if (school != null && tier > 0) {
                        // Reconstruct the perk data from the stored school and tier
                        List<AffinityPerk> tierPerks = AffinityPerkManager.getPerksForCurrentLevel(school, tier);
                        for (AffinityPerk perk : tierPerks) {
                            if (perk.perk == perkType) {
                                activePerks.put(perkType, new PerkData(perk, school, tier));
                                break;
                            }
                        }
                    }
                } catch (IllegalArgumentException e) {
                    ArsAffinity.LOGGER.warn("Unknown perk type in NBT: {}", perkTypeName);
                }
            }
        }
        
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            if (!schoolAffinities.containsKey(school)) {
                schoolAffinities.put(school, 0.0f);
                schoolTiers.put(school, 0);
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