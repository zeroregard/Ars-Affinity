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
    private static final ResourceLocation PERK_BACKGROUND_TEXTURE = ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perk_background.png");
    private static final ResourceLocation PERK_BORDER_TEXTURE = ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perk_border.png");
    private static final ResourceLocation PERK_BORDER_ACTIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perk_border_active.png");
    private static final int NODE_SIZE = 24;
    private static final int ACTIVE_NODE_SIZE = 28;
    
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
        boolean isActiveAbility = node.getPerkType().name().startsWith("ACTIVE_");
        
        int nodeSize = isActiveAbility ? ACTIVE_NODE_SIZE : NODE_SIZE;
        int color = getNodeColor(node, isAllocated, isAvailable);
        
        // Render the base background (no coloring)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(PERK_BACKGROUND_TEXTURE, x, y, 0, 0, nodeSize, nodeSize, nodeSize, nodeSize);
        
        // Render the colored border
        RenderSystem.setShaderColor(
            (color >> 16 & 0xFF) / 255.0f,
            (color >> 8 & 0xFF) / 255.0f,
            (color & 0xFF) / 255.0f,
            1.0f
        );
        
        ResourceLocation borderTexture = isActiveAbility ? PERK_BORDER_ACTIVE_TEXTURE : PERK_BORDER_TEXTURE;
        guiGraphics.blit(borderTexture, x, y, 0, 0, nodeSize, nodeSize, nodeSize, nodeSize);
        
        // Reset color for icon rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Render the perk icon on top of the node background
        renderPerkIcon(guiGraphics, node, x, y, isAllocated, isAvailable, nodeSize);
        
        // Render tier level if allocated and tier > 1
        if (isAllocated && node.getTier() > 1) {
            String levelText = toRomanNumeral(node.getTier());
            guiGraphics.drawString(font, levelText, x + nodeSize - 8, y + nodeSize - 8, 0xFFFFFF);
        }
    }
    
    private void renderPerkIcon(GuiGraphics guiGraphics, PerkNode node, int nodeX, int nodeY, boolean isAllocated, boolean isAvailable, int nodeSize) {
        ResourceLocation perkIcon = getPerkIcon(node);
        int iconSize = 18; // Standard icon size
        int iconX = nodeX + (nodeSize - iconSize) / 2; // Center horizontally
        int iconY = nodeY + (nodeSize - iconSize) / 2; // Center vertically on the node
        
        if (isAvailable && !isAllocated) {
            // Render grayscale for available but not allocated
            renderGrayscaleIcon(guiGraphics, perkIcon, iconX, iconY, iconSize);
        } else {
            // Render normal icon for allocated or unavailable
            guiGraphics.blit(perkIcon, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        }
    }
    
    private void renderGrayscaleIcon(GuiGraphics guiGraphics, ResourceLocation icon, int x, int y, int size) {
        // Apply grayscale shader effect
        RenderSystem.setShaderColor(0.5f, 0.5f, 0.5f, 0.7f); // Darker and more transparent
        guiGraphics.blit(icon, x, y, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
    }
    
    private ResourceLocation getPerkIcon(PerkNode node) {
        String perkType = node.getPerkType().name().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perks/" + perkType + ".png");
    }
    
    public boolean isNodeHovered(PerkNode node, int x, int y, int mouseX, int mouseY) {
        // Check if mouse is over the node box (which now includes the icon)
        boolean isActiveAbility = node.getPerkType().name().startsWith("ACTIVE_");
        int nodeSize = isActiveAbility ? ACTIVE_NODE_SIZE : NODE_SIZE;
        return mouseX >= x && mouseX < x + nodeSize && mouseY >= y && mouseY < y + nodeSize;
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
    
    private String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            case 6 -> "VI";
            case 7 -> "VII";
            case 8 -> "VIII";
            case 9 -> "IX";
            case 10 -> "X";
            default -> String.valueOf(number);
        };
    }
}
