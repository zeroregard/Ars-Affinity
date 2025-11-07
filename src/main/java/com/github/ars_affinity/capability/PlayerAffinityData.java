package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.ActiveAbilityHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkNode;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class PlayerAffinityData implements INBTSerializable<CompoundTag> {
    
    // Points per school (awarded at percentage thresholds)
    private final Map<SpellSchool, Integer> schoolPoints = new HashMap<>();
    
    // Percentage progress per school (0.0 to 100.0)
    private final Map<SpellSchool, Float> schoolPercentages = new HashMap<>();
    
    // Available points to spend (schoolPoints - allocatedPoints)
    private final Map<SpellSchool, Integer> availablePoints = new HashMap<>();
    
    // Allocated perks (what player has chosen to spend points on)
    private final Map<String, PerkAllocation> allocatedPerks = new HashMap<>();
    
    // Unlocked perk nodes (for prerequisite checking)
    private final Set<String> unlockedNodes = new HashSet<>();
    
    private boolean isDirty = false;
    private Player player;
    
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
    
    public PlayerAffinityData() {
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            schoolPoints.put(school, 0);
            schoolPercentages.put(school, 0.0f);
            availablePoints.put(school, 0);
        }
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public void clearPlayer() {
        this.player = null;
    }
    
    private void markDirty() {
        this.isDirty = true;
    }
    
    public int getSchoolPoints(SpellSchool school) {
        return schoolPoints.getOrDefault(school, 0);
    }
    
    public int getAvailablePoints(SpellSchool school) {
        return availablePoints.getOrDefault(school, 0);
    }
    
    public boolean hasAnyAvailablePoints() {
        return availablePoints.values().stream().anyMatch(points -> points > 0);
    }
    
    // Percentage Management
    public float getSchoolPercentage(SpellSchool school) {
        return schoolPercentages.getOrDefault(school, 0.0f);
    }
    
    public Map<SpellSchool, Float> getAllSchoolPercentages() {
        return new HashMap<>(schoolPercentages);
    }
    
    public Map<SpellSchool, Integer> getAllSchoolPoints() {
        return new HashMap<>(schoolPoints);
    }
    
    /**
     * Add percentage progress to a school and award points at thresholds.
     * This is the core method for percentage-based progression.
     * 
     * @param school The school to add progress to
     * @param percentageIncrease The percentage increase (0.0 to 100.0)
     * @return The number of points awarded (if any)
     */
    public int addSchoolProgress(SpellSchool school, float percentageIncrease) {
        float currentPercentage = getSchoolPercentage(school);
        float newPercentage = Math.min(100.0f, currentPercentage + percentageIncrease);
        
        // Update the percentage
        schoolPercentages.put(school, newPercentage);
        
        // Calculate how many points should be awarded based on percentage thresholds
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
        
        // Prevent division by zero - if no max points configured, no points can be awarded
        if (maxPoints <= 0) {
            ArsAffinity.LOGGER.warn("No max points configured for school {}, cannot award points", school.getId());
            return 0;
        }
        
        int thresholdInterval = 100 / maxPoints; // e.g., 10% per point for 10 points
        
        int oldPoints = getSchoolPoints(school);
        int newPoints = (int) (newPercentage / thresholdInterval);
        int pointsAwarded = Math.max(0, newPoints - oldPoints);
        
        if (pointsAwarded > 0) {
            // Award the points
            schoolPoints.put(school, newPoints);
            updateAvailablePoints(school);
            markDirty();
            
            ArsAffinity.LOGGER.info("Awarded {} points to {} school ({}% -> {} points)", 
                pointsAwarded, school.getId(), newPercentage, newPoints);
        }
        
        return pointsAwarded;
    }
    
    /**
     * Get the total points across all schools for global scaling calculations.
     * This is used to determine how much global scaling should be applied.
     * 
     * @return Total points across all schools
     */
    public int getTotalPointsAcrossAllSchools() {
        return schoolPoints.values().stream().mapToInt(Integer::intValue).sum();
    }
    
    // Percentage tracking methods for display purposes
    public float getSchoolAffinityPercentage(SpellSchool school) {
        int currentPoints = getSchoolPoints(school);
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
        return PerkTreeManager.convertPointsToPercentage(currentPoints, maxPoints);
    }
    
    public Map<SpellSchool, Float> getAllSchoolAffinities() {
        Map<SpellSchool, Float> affinities = new HashMap<>();
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            affinities.put(school, getSchoolAffinityPercentage(school));
        }
        return affinities;
    }
    
    public void addSchoolPoints(SpellSchool school, int points) {
        int currentPoints = getSchoolPoints(school);
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
        
        // Cap points at maximum to prevent exceeding 100%
        int newTotalPoints = currentPoints + points;
        if (newTotalPoints > maxPoints) {
            points = maxPoints - currentPoints;
            if (points <= 0) {
                ArsAffinity.LOGGER.debug("Cannot add points to {} school - already at maximum ({}/{})", 
                    school.getId(), currentPoints, maxPoints);
                return;
            }
        }
        
        schoolPoints.put(school, currentPoints + points);
        updateAvailablePoints(school);
        markDirty();
        
        ArsAffinity.LOGGER.debug("Added {} points to {} school. Total: {}/{}", 
            points, school.getId(), getSchoolPoints(school), maxPoints);
    }
    
    public void setSchoolPoints(SpellSchool school, int points) {
        if (points >= 0) {
            int maxPoints = PerkTreeManager.getMaxPointsForSchool(school);
            int cappedPoints = Math.min(points, maxPoints);
            schoolPoints.put(school, cappedPoints);
            updateAvailablePoints(school);
            markDirty();
            
            if (points > maxPoints) {
                ArsAffinity.LOGGER.debug("Capped {} school points from {} to maximum {}", 
                    school.getId(), points, maxPoints);
            }
        }
    }
    
    public void setSchoolPercentage(SpellSchool school, float percentage) {
        schoolPercentages.put(school, Math.max(0.0f, Math.min(100.0f, percentage)));
        markDirty();
    }
    
    public void resetSchoolPercentage(SpellSchool school) {
        schoolPercentages.put(school, 0.0f);
        markDirty();
    }
    
    public void resetAllSchoolPercentages() {
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            schoolPercentages.put(school, 0.0f);
        }
        markDirty();
    }
    
    public void addAvailablePoints(SpellSchool school, int points) {
        int currentAvailable = getAvailablePoints(school);
        availablePoints.put(school, currentAvailable + points);
        markDirty();
    }
    
    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }
    
    public Map<String, PerkAllocation> getPerksForSchool(SpellSchool school) {
        Map<String, PerkAllocation> schoolPerks = new HashMap<>();
        for (Map.Entry<String, PerkAllocation> entry : allocatedPerks.entrySet()) {
            if (entry.getValue().getSchool().equals(school)) {
                schoolPerks.put(entry.getKey(), entry.getValue());
            }
        }
        return schoolPerks;
    }
    
    private void updateAvailablePoints(SpellSchool school) {
        int totalPoints = getSchoolPoints(school);
        int allocatedPoints = getAllocatedPointsForSchool(school);
        availablePoints.put(school, Math.max(0, totalPoints - allocatedPoints));
    }
    
    private int getAllocatedPointsForSchool(SpellSchool school) {
        return allocatedPerks.values().stream()
            .filter(allocation -> allocation.getSchool().equals(school))
            .mapToInt(PerkAllocation::getPointsInvested)
            .sum();
    }
    
    // Perk Allocation
    public boolean canAllocatePerk(PerkNode node) {
        // Check if player has enough points
        if (getAvailablePoints(node.getSchool()) < node.getPointCost()) {
            return false;
        }
        
        // Check prerequisites
        for (String prerequisiteId : node.getPrerequisites()) {
            if (!unlockedNodes.contains(prerequisiteId)) {
                return false;
            }
        }
        
        // Check if this is an active ability and if player already has one
        if (ActiveAbilityHelper.isActiveAbility(node.getPerkType())) {
            if (hasAnyActiveAbility()) {
                ArsAffinity.LOGGER.debug("Cannot allocate active ability {} - player already has an active ability", node.getPerkType());
                return false;
            }
        }
        
        return true;
    }
    
    public boolean allocatePerk(PerkNode node) {
        if (!canAllocatePerk(node)) {
            return false;
        }
        
        String nodeId = node.getId();
        
        // Check if already allocated
        if (allocatedPerks.containsKey(nodeId)) {
            return false;
        }
        
        // Allocate the perk
        PerkAllocation allocation = new PerkAllocation(node);
        allocatedPerks.put(nodeId, allocation);
        unlockedNodes.add(nodeId);
        
        // Update available points
        updateAvailablePoints(node.getSchool());
        markDirty();
        
        // If this is an active ability, update the active ability data
        if (ActiveAbilityHelper.isActiveAbility(node.getPerkType())) {
            updateActiveAbilityData();
        }
        
        ArsAffinity.LOGGER.info("Allocated perk: {} for {} points", nodeId, node.getPointCost());
        return true;
    }
    
    public boolean deallocatePerk(String nodeId) {
        PerkAllocation allocation = allocatedPerks.get(nodeId);
        if (allocation == null) {
            return false;
        }
        
        // Check if any other perks depend on this one
        for (PerkAllocation otherAllocation : allocatedPerks.values()) {
            if (otherAllocation.getNode().getPrerequisites().contains(nodeId)) {
                ArsAffinity.LOGGER.warn("Cannot deallocate {} - other perks depend on it", nodeId);
                return false;
            }
        }
        
        // Deallocate the perk
        allocatedPerks.remove(nodeId);
        unlockedNodes.remove(nodeId);
        
        // Update available points
        updateAvailablePoints(allocation.getSchool());
        markDirty();
        
        // If this was an active ability, update the active ability data
        if (ActiveAbilityHelper.isActiveAbility(allocation.getPerkType())) {
            updateActiveAbilityData();
        }
        
        ArsAffinity.LOGGER.info("Deallocated perk: {}", nodeId);
        return true;
    }
    
    public boolean isPerkAllocated(String nodeId) {
        return allocatedPerks.containsKey(nodeId);
    }
    
    public PerkAllocation getAllocatedPerk(String nodeId) {
        return allocatedPerks.get(nodeId);
    }
    
    public Set<PerkAllocation> getAllAllocatedPerks() {
        return Set.copyOf(allocatedPerks.values());
    }
    
    public Set<PerkAllocation> getAllocatedPerksForSchool(SpellSchool school) {
        return allocatedPerks.values().stream()
            .filter(allocation -> allocation.getSchool().equals(school))
            .collect(java.util.stream.Collectors.toSet());
    }
    
    // Active Ability Management
    public boolean hasAnyActiveAbility() {
        return allocatedPerks.values().stream()
            .anyMatch(allocation -> ActiveAbilityHelper.isActiveAbility(allocation.getPerkType()));
    }
    
    public AffinityPerkType getCurrentActiveAbilityType() {
        return allocatedPerks.values().stream()
            .filter(allocation -> ActiveAbilityHelper.isActiveAbility(allocation.getPerkType()))
            .findFirst()
            .map(PerkAllocation::getPerkType)
            .orElse(null);
    }
    
    public PerkAllocation getCurrentActiveAbilityAllocation() {
        return allocatedPerks.values().stream()
            .filter(allocation -> ActiveAbilityHelper.isActiveAbility(allocation.getPerkType()))
            .findFirst()
            .orElse(null);
    }
    
    private void updateActiveAbilityData() {
        if (player != null) {
            var activeAbilityData = ActiveAbilityProvider.getActiveAbilityData(player);
            if (activeAbilityData != null) {
                AffinityPerkType currentType = getCurrentActiveAbilityType();
                activeAbilityData.setActiveAbilityType(currentType);
                ActiveAbilityProvider.savePlayerData(player);
            }
        }
    }
    
    // Respec System
    public boolean canRespec() {
        // TODO: Add cooldown and cost checking
        return true;
    }
    
    public void respecSchool(SpellSchool school) {
        if (!canRespec()) {
            return;
        }
        
        // Check if we're removing an active ability
        boolean hadActiveAbility = hasAnyActiveAbility();
        
        // Remove all allocated perks for this school
        allocatedPerks.entrySet().removeIf(entry -> 
            entry.getValue().getSchool().equals(school));
        
        // Remove unlocked nodes for this school
        unlockedNodes.removeIf(nodeId -> 
            nodeId.startsWith(school.getId().toString()));
        
        // Reset available points
        updateAvailablePoints(school);
        markDirty();
        
        // Update active ability data if we removed an active ability
        if (hadActiveAbility) {
            updateActiveAbilityData();
        }
        
        ArsAffinity.LOGGER.info("Respeced {} school", school.getId());
    }
    
    public void respecAll() {
        if (!canRespec()) {
            return;
        }
        
        allocatedPerks.clear();
        unlockedNodes.clear();
        
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            updateAvailablePoints(school);
        }
        
        markDirty();
        
        // Clear active ability data since all perks are removed
        updateActiveAbilityData();
        
        ArsAffinity.LOGGER.info("Respeced all schools");
    }
    
    // Migration from old system
    public void migrateFromOldSystem(Map<SpellSchool, Float> oldAffinities) {
        ArsAffinity.LOGGER.info("Migrating from old affinity system...");
        
        for (Map.Entry<SpellSchool, Float> entry : oldAffinities.entrySet()) {
            SpellSchool school = entry.getKey();
            float oldAffinity = entry.getValue();
            
            // Convert percentage to points using the same scaling as the old system
            int points = convertAffinityToPoints(oldAffinity);
            schoolPoints.put(school, points);
            availablePoints.put(school, points);
            
            ArsAffinity.LOGGER.info("Migrated {}: {}% -> {} points", 
                school.getId(), oldAffinity * 100, points);
        }
        
        markDirty();
    }
    
    private int convertAffinityToPoints(float affinity) {
        // Use the same scaling formula as the old system
        // This will be refined based on the actual scaling implementation
        double scalingFactor = 1.0 - Math.pow(affinity, 3.0); // AFFINITY_SCALING_DECAY_STRENGTH = 3.0
        double minimumFactor = 0.1; // AFFINITY_SCALING_MINIMUM_FACTOR = 0.1
        
        // Calculate points based on the inverse of the scaling
        // This is a rough approximation - will need refinement
        double basePoints = 100.0; // Base points for 100% affinity
        double scaledPoints = basePoints * Math.max(minimumFactor, scalingFactor);
        
        return (int) Math.round(scaledPoints);
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        // Serialize school points
        CompoundTag schoolPointsTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Integer> entry : schoolPoints.entrySet()) {
            String schoolId = getShortSchoolId(entry.getKey());
            schoolPointsTag.putInt(schoolId, entry.getValue());
        }
        tag.put("schoolPoints", schoolPointsTag);
        
        // Serialize school percentages
        CompoundTag schoolPercentagesTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Float> entry : schoolPercentages.entrySet()) {
            String schoolId = getShortSchoolId(entry.getKey());
            schoolPercentagesTag.putFloat(schoolId, entry.getValue());
        }
        tag.put("schoolPercentages", schoolPercentagesTag);
        
        // Serialize available points
        CompoundTag availablePointsTag = new CompoundTag();
        for (Map.Entry<SpellSchool, Integer> entry : availablePoints.entrySet()) {
            String schoolId = getShortSchoolId(entry.getKey());
            availablePointsTag.putInt(schoolId, entry.getValue());
        }
        tag.put("availablePoints", availablePointsTag);
        
        // Serialize allocated perks
        ListTag allocatedPerksTag = new ListTag();
        for (PerkAllocation allocation : allocatedPerks.values()) {
            allocatedPerksTag.add(allocation.serializeNBT());
        }
        tag.put("allocatedPerks", allocatedPerksTag);
        
        // Serialize unlocked nodes
        ListTag unlockedNodesTag = new ListTag();
        for (String nodeId : unlockedNodes) {
            unlockedNodesTag.add(net.minecraft.nbt.StringTag.valueOf(nodeId));
        }
        tag.put("unlockedNodes", unlockedNodesTag);
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        // Deserialize school points
        CompoundTag schoolPointsTag = tag.getCompound("schoolPoints");
        schoolPoints.clear();
        for (String key : schoolPointsTag.getAllKeys()) {
            SpellSchool school = getSpellSchoolFromId(key);
            if (school != null) {
                int points = schoolPointsTag.getInt(key);
                schoolPoints.put(school, points);
            }
        }
        
        // Deserialize school percentages
        CompoundTag schoolPercentagesTag = tag.getCompound("schoolPercentages");
        schoolPercentages.clear();
        for (String key : schoolPercentagesTag.getAllKeys()) {
            SpellSchool school = getSpellSchoolFromId(key);
            if (school != null) {
                float percentage = schoolPercentagesTag.getFloat(key);
                schoolPercentages.put(school, percentage);
            }
        }
        
        // If no percentage data exists (backward compatibility), initialize with 0%
        if (schoolPercentages.isEmpty()) {
            for (SpellSchool school : SUPPORTED_SCHOOLS) {
                schoolPercentages.put(school, 0.0f);
            }
        }
        
        // Deserialize available points
        CompoundTag availablePointsTag = tag.getCompound("availablePoints");
        availablePoints.clear();
        for (String key : availablePointsTag.getAllKeys()) {
            SpellSchool school = getSpellSchoolFromId(key);
            if (school != null) {
                int points = availablePointsTag.getInt(key);
                availablePoints.put(school, points);
            }
        }
        
        // Deserialize allocated perks
        ListTag allocatedPerksTag = tag.getList("allocatedPerks", Tag.TAG_COMPOUND);
        allocatedPerks.clear();
        for (Tag perkTag : allocatedPerksTag) {
            if (perkTag instanceof CompoundTag compoundTag) {
                PerkAllocation allocation = PerkAllocation.deserializeNBT(compoundTag);
                if (allocation != null) {
                    allocatedPerks.put(allocation.getNodeId(), allocation);
                } else {
                    ArsAffinity.LOGGER.warn("Failed to deserialize perk allocation, skipping");
                }
            }
        }
        
        // Deserialize unlocked nodes
        ListTag unlockedNodesTag = tag.getList("unlockedNodes", Tag.TAG_STRING);
        unlockedNodes.clear();
        for (Tag nodeTag : unlockedNodesTag) {
            if (nodeTag instanceof net.minecraft.nbt.StringTag stringTag) {
                unlockedNodes.add(stringTag.getAsString());
            }
        }
    }
    
    private static SpellSchool getSpellSchoolFromId(String id) {
        return switch (id) {
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            case "abjuration" -> SpellSchools.ABJURATION;
            case "necromancy" -> SpellSchools.NECROMANCY;
            case "conjuration" -> SpellSchools.CONJURATION;
            case "manipulation" -> SpellSchools.MANIPULATION;
            default -> null;
        };
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
