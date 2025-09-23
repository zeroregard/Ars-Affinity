package com.github.ars_affinity.perk;

import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class PerkPrerequisiteChecker {
    
    public static class PrerequisiteResult {
        private final boolean canAllocate;
        private final List<String> reasons;
        
        public PrerequisiteResult(boolean canAllocate, List<String> reasons) {
            this.canAllocate = canAllocate;
            this.reasons = new ArrayList<>(reasons);
        }
        
        public boolean canAllocate() {
            return canAllocate;
        }
        
        public List<String> getReasons() {
            return new ArrayList<>(reasons);
        }
        
        public boolean hasReasons() {
            return !reasons.isEmpty();
        }
    }
    
    /**
     * Check all prerequisites for a perk and return detailed reasons if it cannot be allocated.
     * @param player The player
     * @param node The perk node to check
     * @return PrerequisiteResult with detailed information
     */
    public static PrerequisiteResult checkPrerequisites(Player player, PerkNode node) {
        List<String> reasons = new ArrayList<>();
        
        PlayerAffinityData data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            reasons.add("Player data not available");
            return new PrerequisiteResult(false, reasons);
        }
        
        // Check if already allocated
        if (data.isPerkAllocated(node.getId())) {
            reasons.add("Perk already allocated");
            return new PrerequisiteResult(false, reasons);
        }
        
        // Check if player has enough points
        int availablePoints = data.getAvailablePoints(node.getSchool());
        if (availablePoints < node.getPointCost()) {
            reasons.add("No points available");
        }
        
        // Check prerequisite perks
        for (String prereq : node.getPrerequisites()) {
            if (!data.isPerkAllocated(prereq)) {
                PerkNode prereqNode = PerkTreeManager.getNode(prereq);
                if (prereqNode != null) {
                    reasons.add("Previous perks not unlocked");
                } else {
                    reasons.add("Previous perks not unlocked");
                }
                break; // Only add this reason once
            }
        }
        
        // Check glyph prerequisite
        if (node.hasPrerequisiteGlyph()) {
            if (!GlyphPrerequisiteHelper.hasUnlockedGlyph(player, node.getPrerequisiteGlyph())) {
                String glyphName = GlyphPrerequisiteHelper.getGlyphDisplayName(node.getPrerequisiteGlyph());
                reasons.add("Glyph '" + glyphName + "' not unlocked");
            }
        }
        
        // Check active ability restriction
        if (ActiveAbilityHelper.isActiveAbility(node.getPerkType())) {
            AffinityPerkType currentActiveAbility = data.getCurrentActiveAbilityType();
            if (currentActiveAbility != null && currentActiveAbility != node.getPerkType()) {
                reasons.add("Existing active ability already allocated");
            }
        }
        
        boolean canAllocate = reasons.isEmpty();
        return new PrerequisiteResult(canAllocate, reasons);
    }
    
    /**
     * Get a formatted tooltip component showing prerequisite reasons.
     * @param player The player
     * @param node The perk node
     * @return Component with prerequisite information
     */
    public static MutableComponent getPrerequisiteTooltip(Player player, PerkNode node) {
        PrerequisiteResult result = checkPrerequisites(player, node);
        
        if (result.canAllocate()) {
            return Component.empty();
        }
        
        MutableComponent tooltip = Component.translatable("ars_affinity.tooltip.prerequisites_not_met");
        
        if (result.hasReasons()) {
            tooltip.append("\n");
            for (String reason : result.getReasons()) {
                tooltip.append("\n").append(Component.literal("• " + reason));
            }
        }
        
        return tooltip;
    }
}
