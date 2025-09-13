package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;

import java.util.*;

public class PerkTreeLayout {
    private static final int NODE_SIZE = 24;
    private static final int NODE_SPACING = 40;
    private static final int TIER_SPACING = 60;
    private static final int MAX_NODES_PER_TIER = 4;
    
    private final Map<String, PerkNode> schoolPerks;
    private final Map<Integer, List<PerkNode>> perksByTier;
    
    public PerkTreeLayout(Map<String, PerkNode> schoolPerks) {
        this.schoolPerks = schoolPerks;
        this.perksByTier = groupPerksByTier();
    }
    
    public Map<Integer, List<PerkNode>> getPerksByTier() {
        return perksByTier;
    }
    
    public int getNodeX(PerkNode node, int startX) {
        List<PerkNode> tierNodes = perksByTier.get(node.getTier());
        if (tierNodes == null) return startX;
        
        int index = tierNodes.indexOf(node);
        if (index == -1) return startX;
        
        return startX + index * NODE_SPACING;
    }
    
    public int getNodeY(PerkNode node, int startY) {
        return startY + node.getTier() * TIER_SPACING;
    }
    
    public int getStartX(int width, int scrollX) {
        return width / 2 - (MAX_NODES_PER_TIER * NODE_SPACING) / 2 + scrollX;
    }
    
    public int getStartY(int scrollY) {
        return 50 + scrollY;
    }
    
    public PerkNode getNodeAt(int mouseX, int mouseY, int startX, int startY) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : perksByTier.entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                int nodeX = startX + i * NODE_SPACING;
                int nodeY = startY + node.getTier() * TIER_SPACING;
                
                if (mouseX >= nodeX && mouseX < nodeX + NODE_SIZE && 
                    mouseY >= nodeY && mouseY < nodeY + NODE_SIZE) {
                    return node;
                }
            }
        }
        return null;
    }
    
    private Map<Integer, List<PerkNode>> groupPerksByTier() {
        Map<Integer, List<PerkNode>> tiers = new HashMap<>();
        
        for (PerkNode node : schoolPerks.values()) {
            tiers.computeIfAbsent(node.getTier(), k -> new ArrayList<>()).add(node);
        }
        
        for (List<PerkNode> tier : tiers.values()) {
            tier.sort(Comparator.comparing(PerkNode::getId));
        }
        
        return tiers;
    }
}
