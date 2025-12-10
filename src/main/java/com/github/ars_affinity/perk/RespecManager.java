package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

/**
 * Manages respec functionality for the perk system.
 * Allows players to reallocate their points for a cost.
 */
public class RespecManager {
    
    private RespecManager() {}
    
    /**
     * The cost in experience levels to respec a single school.
     */
    public static final int RESPEC_COST_PER_SCHOOL = 5;
    
    /**
     * The cost in experience levels to respec all schools at once.
     */
    public static final int RESPEC_COST_ALL_SCHOOLS = 20;
    
    /**
     * Check if a player can afford to respec a specific school.
     * 
     * @param player The player
     * @param school The school to respec
     * @return true if the player can afford the respec
     */
    public static boolean canRespecSchool(Player player, SpellSchool school) {
        return player.experienceLevel >= RESPEC_COST_PER_SCHOOL;
    }
    
    /**
     * Check if a player can afford to respec all schools.
     * 
     * @param player The player
     * @return true if the player can afford the respec
     */
    public static boolean canRespecAllSchools(Player player) {
        return player.experienceLevel >= RESPEC_COST_ALL_SCHOOLS;
    }
    
    /**
     * Respec a specific school, returning all allocated points to available points.
     * 
     * @param player The player
     * @param school The school to respec
     * @return true if respec was successful, false otherwise
     */
    public static boolean respecSchool(Player player, SpellSchool school) {
        if (!canRespecSchool(player, school)) {
        ArsAffinity.LOGGER.debug("Player {} cannot afford to respec school {}", 
            player.getName().getString(), school.getId());
            return false;
        }
        
        PlayerAffinityData affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData == null) {
            ArsAffinity.LOGGER.warn("PlayerAffinityData not found for player {}", player.getName().getString());
            return false;
        }
        
        // Get all allocated perks for this school
        Map<String, PerkAllocation> allocatedPerks = affinityData.getPerksForSchool(school);
        if (allocatedPerks.isEmpty()) {
            ArsAffinity.LOGGER.debug("No perks allocated for school {} - nothing to respec", school.getId());
            return false;
        }
        
        // Calculate total points to return
        int totalPointsToReturn = 0;
        for (PerkAllocation allocation : allocatedPerks.values()) {
            totalPointsToReturn += allocation.getPointsInvested();
        }
        
        // Remove all allocated perks for this school
        for (String nodeId : allocatedPerks.keySet()) {
            affinityData.deallocatePerk(nodeId);
        }
        
        // Return points to available points
        affinityData.addAvailablePoints(school, totalPointsToReturn);
        
        // Deduct experience cost
        player.giveExperienceLevels(-RESPEC_COST_PER_SCHOOL);
        
        // Mark data as dirty
        affinityData.setDirty(true);
        
        ArsAffinity.LOGGER.debug("Player {} respecced school {} - returned {} points, cost {} levels", 
            player.getName().getString(), school.getId(), totalPointsToReturn, RESPEC_COST_PER_SCHOOL);
        
        return true;
    }
    
    /**
     * Respec all schools, returning all allocated points to available points.
     * 
     * @param player The player
     * @return true if respec was successful, false otherwise
     */
    public static boolean respecAllSchools(Player player) {
        if (!canRespecAllSchools(player)) {
            ArsAffinity.LOGGER.debug("Player {} cannot afford to respec all schools", player.getName().getString());
            return false;
        }
        
        PlayerAffinityData affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData == null) {
            ArsAffinity.LOGGER.warn("PlayerAffinityData not found for player {}", player.getName().getString());
            return false;
        }
        
        // Get all allocated perks
        java.util.Set<PerkAllocation> allAllocatedPerks = affinityData.getAllAllocatedPerks();
        if (allAllocatedPerks.isEmpty()) {
            ArsAffinity.LOGGER.debug("No perks allocated - nothing to respec");
            return false;
        }
        
        // Calculate total points to return per school
        Map<SpellSchool, Integer> pointsToReturn = new java.util.HashMap<>();
        for (PerkAllocation allocation : allAllocatedPerks) {
            SpellSchool school = allocation.getNode().getSchool();
            pointsToReturn.merge(school, allocation.getPointsInvested(), Integer::sum);
        }
        
        // Remove all allocated perks
        for (PerkAllocation allocation : allAllocatedPerks) {
            affinityData.deallocatePerk(allocation.getNodeId());
        }
        
        // Return points to available points for each school
        for (Map.Entry<SpellSchool, Integer> entry : pointsToReturn.entrySet()) {
            affinityData.addAvailablePoints(entry.getKey(), entry.getValue());
        }
        
        // Deduct experience cost
        player.giveExperienceLevels(-RESPEC_COST_ALL_SCHOOLS);
        
        // Mark data as dirty
        affinityData.setDirty(true);
        
        ArsAffinity.LOGGER.debug("Player {} respecced all schools - returned {} total points, cost {} levels", 
            player.getName().getString(), pointsToReturn.values().stream().mapToInt(Integer::intValue).sum(), RESPEC_COST_ALL_SCHOOLS);
        
        return true;
    }
    
    /**
     * Get the cost to respec a specific school.
     * 
     * @param school The school
     * @return The cost in experience levels
     */
    public static int getRespecCost(SpellSchool school) {
        return RESPEC_COST_PER_SCHOOL;
    }
    
    /**
     * Get the cost to respec all schools.
     * 
     * @return The cost in experience levels
     */
    public static int getRespecAllCost() {
        return RESPEC_COST_ALL_SCHOOLS;
    }
}
