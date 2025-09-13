package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class PerkNodeRenderer {
    private static final ResourceLocation PERK_NODE_TEXTURE = ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perk_node.png");
    private static final int NODE_SIZE = 24;
    
    private final Player player;
    private final Map<String, PerkAllocation> allocatedPerks;
    
    public PerkNodeRenderer(Player player, Map<String, PerkAllocation> allocatedPerks) {
        this.player = player;
        this.allocatedPerks = allocatedPerks;
    }
    
    public void renderNode(GuiGraphics guiGraphics, Font font, PerkNode node, int x, int y, int mouseX, int mouseY) {
        PerkAllocation allocation = allocatedPerks.get(node.getId());
        boolean isAllocated = allocation != null && allocation.isActive();
        boolean isAvailable = PerkAllocationManager.canAllocate(player, node.getId());
        
        int color = getNodeColor(node, isAllocated, isAvailable);
        
        RenderSystem.setShaderColor(
            (color >> 16 & 0xFF) / 255.0f,
            (color >> 8 & 0xFF) / 255.0f,
            (color & 0xFF) / 255.0f,
            1.0f
        );
        
        guiGraphics.blit(PERK_NODE_TEXTURE, x, y, 0, 0, NODE_SIZE, NODE_SIZE, NODE_SIZE, NODE_SIZE);
        
        guiGraphics.fill(x + 4, y + 4, x + NODE_SIZE - 4, y + NODE_SIZE - 4, 0xFFFFFFFF);
        
        if (isAllocated && node.getLevel() > 1) {
            String levelText = "Lv." + String.valueOf(node.getLevel());
            guiGraphics.drawString(font, levelText, x + NODE_SIZE - 8, y + NODE_SIZE - 8, 0xFFFFFF);
        }
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public boolean isNodeHovered(PerkNode node, int x, int y, int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + NODE_SIZE && mouseY >= y && mouseY < y + NODE_SIZE;
    }
    
    private int getNodeColor(PerkNode node, boolean isAllocated, boolean isAvailable) {
        if (isAllocated) {
            return 0xFF00FF00;
        } else if (isAvailable) {
            return 0xFF0088FF;
        } else {
            return 0xFF666666;
        }
    }
}
