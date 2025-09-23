package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages manual perk allocation for players.
 * Players earn points by casting spells and can manually allocate them to specific perks.
 */
public class PerkAllocationManager {
    
    /**
     * Check if a player can allocate points to a specific perk.
     * @param player The player
     * @param perkId The perk ID to check
     * @return true if the player can allocate this perk
     */
    public static boolean canAllocate(Player player, String perkId) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return false;
        
        PerkNode node = PerkTreeManager.getNode(perkId);
        if (node == null) return false;
        
        // Check if already allocated
        if (data.isPerkAllocated(perkId)) return false;
        
        // Check if player has enough points
        int availablePoints = data.getAvailablePoints(node.getSchool());
        if (availablePoints < node.getPointCost()) return false;
        
        // Check prerequisites
        for (String prereq : node.getPrerequisites()) {
            if (!data.isPerkAllocated(prereq)) {
                return false;
            }
        }
        
        // Check glyph prerequisite
        if (node.hasPrerequisiteGlyph()) {
            if (!GlyphPrerequisiteHelper.hasUnlockedGlyph(player, node.getPrerequisiteGlyph())) {
                return false;
            }
        }
        
        // Check active ability restriction - only one active ability allowed at a time
        // But allow if the player is trying to allocate the same active ability they currently have
        if (ActiveAbilityHelper.isActiveAbility(node.getPerkType())) {
            AffinityPerkType currentActiveAbility = data.getCurrentActiveAbilityType();
            if (currentActiveAbility != null && currentActiveAbility != node.getPerkType()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if a player has a different active ability than the one they're trying to allocate.
     * @param player The player
     * @param perkId The perk ID to check
     * @return true if the player has a different active ability
     */
    public static boolean hasDifferentActiveAbility(Player player, String perkId) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return false;
        
        PerkNode node = PerkTreeManager.getNode(perkId);
        if (node == null) return false;
        
        if (!ActiveAbilityHelper.isActiveAbility(node.getPerkType())) {
            return false;
        }
        
        AffinityPerkType currentActiveAbility = data.getCurrentActiveAbilityType();
        return currentActiveAbility != null && currentActiveAbility != node.getPerkType();
    }
    
    /**
     * Allocate points to a specific perk.
     * @param player The player
     * @param perkId The perk ID to allocate
     * @param points The number of points to allocate (usually 1)
     * @return true if allocation was successful
     */
    public static boolean allocatePoints(Player player, String perkId, int points) {
        if (!canAllocate(player, perkId)) {
            return false;
        }
        
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return false;
        
        PerkNode node = PerkTreeManager.getNode(perkId);
        if (node == null) return false;
        
        // Allocate the perk
        boolean success = data.allocatePerk(node);
        if (success) {
            ArsAffinity.LOGGER.info("Player {} allocated {} points to perk {}", 
                player.getName().getString(), points, perkId);
        }
        
        return success;
    }
    
    /**
     * Deallocate a specific perk.
     * @param player The player
     * @param perkId The perk ID to deallocate
     * @return true if deallocation was successful
     */
    public static boolean deallocatePerk(Player player, String perkId) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return false;
        
        // Check if perk is allocated
        if (!data.isPerkAllocated(perkId)) return false;
        
        // Check if any other perks depend on this one
        if (hasDependentPerks(data, perkId)) {
            ArsAffinity.LOGGER.warn("Cannot deallocate perk {} - other perks depend on it", perkId);
            return false;
        }
        
        // Deallocate the perk
        boolean success = data.deallocatePerk(perkId);
        if (success) {
            ArsAffinity.LOGGER.info("Player {} deallocated perk {}", 
                player.getName().getString(), perkId);
        }
        
        return success;
    }
    
    /**
     * Get all allocated perks for a specific school.
     * @param player The player
     * @param school The spell school
     * @return Map of perk ID to allocation data
     */
    public static Map<String, PerkAllocation> getAllocatedPerks(Player player, SpellSchool school) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return new HashMap<>();
        
        Map<String, PerkAllocation> result = new HashMap<>();
        for (PerkAllocation allocation : data.getAllAllocatedPerks()) {
            PerkNode node = PerkTreeManager.getNode(allocation.getNodeId());
            if (node != null && node.getSchool() == school) {
                result.put(allocation.getNodeId(), allocation);
            }
        }
        
        return result;
    }
    
    /**
     * Get all allocated perks for all schools.
     * @param player The player
     * @return Map of perk ID to allocation data
     */
    public static Map<String, PerkAllocation> getAllAllocatedPerks(Player player) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return new HashMap<>();
        
        Map<String, PerkAllocation> result = new HashMap<>();
        for (PerkAllocation allocation : data.getAllAllocatedPerks()) {
            result.put(allocation.getNodeId(), allocation);
        }
        
        return result;
    }
    
    /**
     * Check if a perk has other perks that depend on it.
     * @param data The player's affinity data
     * @param perkId The perk ID to check
     * @return true if other perks depend on this one
     */
    private static boolean hasDependentPerks(PlayerAffinityData data, String perkId) {
        // Get all perks for the same school
        PerkNode targetNode = PerkTreeManager.getNode(perkId);
        if (targetNode == null) return false;
        
        SpellSchool school = targetNode.getSchool();
        for (PerkNode node : PerkTreeManager.getSchoolNodes(school).values()) {
            if (node.getPrerequisites().contains(perkId) && data.isPerkAllocated(node.getId())) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Reset all perk allocations for a specific school.
     * @param player The player
     * @param school The spell school to reset
     * @return true if reset was successful
     */
    public static boolean resetSchoolPerks(Player player, SpellSchool school) {
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) return false;
        
        // Get all allocated perks for this school
        Map<String, PerkAllocation> schoolPerks = getAllocatedPerks(player, school);
        
        // Deallocate all perks for this school
        for (String perkId : schoolPerks.keySet()) {
            data.deallocatePerk(perkId);
        }
        
        ArsAffinity.LOGGER.info("Player {} reset all perks for school {}", 
            player.getName().getString(), school.getId());
        
        return true;
    }
    
    /**
     * Get the total cost of all allocated perks for a school.
     * @param player The player
     * @param school The spell school
     * @return Total point cost
     */
    public static int getTotalAllocatedCost(Player player, SpellSchool school) {
        Map<String, PerkAllocation> allocatedPerks = getAllocatedPerks(player, school);
        int totalCost = 0;
        
        for (PerkAllocation allocation : allocatedPerks.values()) {
            PerkNode node = PerkTreeManager.getNode(allocation.getNodeId());
            if (node != null) {
                totalCost += node.getPointCost();
            }
        }
        
        return totalCost;
    }
}
