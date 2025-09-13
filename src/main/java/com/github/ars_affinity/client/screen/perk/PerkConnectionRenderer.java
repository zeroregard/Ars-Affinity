package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;
import java.util.Map;

public class PerkConnectionRenderer {
    private static final int NODE_SIZE = 24;
    
    public void renderConnections(GuiGraphics guiGraphics, Map<Integer, List<PerkNode>> perksByTier, 
                                Map<String, PerkNode> schoolPerks, int startX, int startY) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : perksByTier.entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (PerkNode node : nodes) {
                for (String prerequisiteId : node.getPrerequisites()) {
                    PerkNode prerequisite = schoolPerks.get(prerequisiteId);
                    if (prerequisite != null) {
                        renderConnection(guiGraphics, prerequisite, node, startX, startY);
                    }
                }
            }
        }
    }
    
    private void renderConnection(GuiGraphics guiGraphics, PerkNode from, PerkNode to, int startX, int startY) {
        int fromX = getNodeX(from, startX);
        int fromY = getNodeY(from, startY);
        int toX = getNodeX(to, startX);
        int toY = getNodeY(to, startY);
        
        guiGraphics.fill(fromX + NODE_SIZE / 2, fromY + NODE_SIZE / 2, 
                        toX + NODE_SIZE / 2, toY + NODE_SIZE / 2, 0xFF888888);
    }
    
    private int getNodeX(PerkNode node, int startX) {
        return startX + node.getTier() * 40;
    }
    
    private int getNodeY(PerkNode node, int startY) {
        return startY + node.getTier() * 60;
    }
}
