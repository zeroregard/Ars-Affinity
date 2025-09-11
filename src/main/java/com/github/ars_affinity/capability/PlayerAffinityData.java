package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
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
    
    // Points per school (earned by casting spells)
    private final Map<SpellSchool, Integer> schoolPoints = new HashMap<>();
    
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
        // Initialize all schools with 0 points
        for (SpellSchool school : SUPPORTED_SCHOOLS) {
            schoolPoints.put(school, 0);
            availablePoints.put(school, 0);
        }
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    private void markDirty() {
        this.isDirty = true;
    }
    
    // Point Management
    public int getSchoolPoints(SpellSchool school) {
        return schoolPoints.getOrDefault(school, 0);
    }
    
    public int getAvailablePoints(SpellSchool school) {
        return availablePoints.getOrDefault(school, 0);
    }
    
    public Map<SpellSchool, Integer> getAllSchoolPoints() {
        return new HashMap<>(schoolPoints);
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
    
    // Respec System
    public boolean canRespec() {
        // TODO: Add cooldown and cost checking
        return true;
    }
    
    public void respecSchool(SpellSchool school) {
        if (!canRespec()) {
            return;
        }
        
        // Remove all allocated perks for this school
        allocatedPerks.entrySet().removeIf(entry -> 
            entry.getValue().getSchool().equals(school));
        
        // Remove unlocked nodes for this school
        unlockedNodes.removeIf(nodeId -> 
            nodeId.startsWith(school.getId().toString()));
        
        // Reset available points
        updateAvailablePoints(school);
        markDirty();
        
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
                allocatedPerks.put(allocation.getNodeId(), allocation);
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
