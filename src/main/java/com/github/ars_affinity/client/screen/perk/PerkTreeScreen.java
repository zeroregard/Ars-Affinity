package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Screen that displays the perk tree for a specific school.
 * Similar to the vanilla Progression/Achievement screen.
 */
public class PerkTreeScreen extends Screen {
    
    private static final ResourceLocation PERK_TREE_BACKGROUND = ArsAffinity.prefix("textures/gui/perk_tree_background.png");
    private static final ResourceLocation PERK_TREE_FRAME = ArsAffinity.prefix("textures/gui/perk_tree_frame.png");
    
    private final Player player;
    private final SpellSchool school;
    private final Screen previousScreen;
    private final Map<String, PerkNode> schoolPerks;
    private final Map<String, PerkAllocation> allocatedPerks;
    private final PlayerAffinityData affinityData;
    
    private final PerkTreeLayout layout;
    private final PerkNodeRenderer nodeRenderer;
    private final PerkConnectionRenderer connectionRenderer;
    private final PerkTooltipRenderer tooltipRenderer;
    
    private int scrollX = 0;
    private int scrollY = 0;
    private boolean isDragging = false;
    
    public PerkTreeScreen(Player player, SpellSchool school, Screen previousScreen) {
        super(Component.translatable("ars_affinity.screen.perk_tree.title", school.getTextComponent()));
        this.player = player;
        this.school = school;
        this.previousScreen = previousScreen;
        this.schoolPerks = PerkTreeManager.getSchoolNodes(school);
        this.affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        this.allocatedPerks = PerkAllocationManager.getAllocatedPerks(player, school);
        
        this.layout = new PerkTreeLayout(schoolPerks);
        this.nodeRenderer = new PerkNodeRenderer(player, allocatedPerks);
        this.connectionRenderer = new PerkConnectionRenderer(player, allocatedPerks, school, layout);
        this.tooltipRenderer = new PerkTooltipRenderer(player, allocatedPerks);
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Center the view on the root node (column 0)
        centerOnRootNode();
        
        // Back button is now rendered as just an icon without a button widget
    }
    
    private void centerOnRootNode() {
        // Find the root node (column 0) and center the view on it
        int rootX = 0; // Root nodes are at x=0 in our coordinate system
        int rootY = 0; // We'll center vertically on the root nodes
        
        // Calculate the center of the root nodes
        var rootNodes = schoolPerks.values().stream()
            .filter(node -> node.getPrerequisites().isEmpty())
            .toList();
        
        if (!rootNodes.isEmpty()) {
            int totalY = 0;
            for (PerkNode rootNode : rootNodes) {
                totalY += layout.getNodeY(rootNode, 0);
            }
            rootY = totalY / rootNodes.size();
        }
        
        // Center the view on the root node
        scrollX = -rootX;
        scrollY = -rootY + height / 2 - 50; // 50 is the base offset from getStartY
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200; // Reduced height
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        // Enable scissor test for background, connections, and nodes to keep them within the panel
        guiGraphics.enableScissor(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        
        // Render the parallax background within the clipped area
        renderParallaxBackground(guiGraphics, panelX, panelY, panelWidth, panelHeight);
        
        // Add a subtle border for visual definition (within clipped area)
        guiGraphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY, 0xFF404040);
        guiGraphics.fill(panelX - 1, panelY + panelHeight, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF404040);
        guiGraphics.fill(panelX - 1, panelY, panelX, panelY + panelHeight, 0xFF404040);
        guiGraphics.fill(panelX + panelWidth, panelY, panelX + panelWidth + 1, panelY + panelHeight, 0xFF404040);
        
        // Render connections with scissor test to fix clipping
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, startX, startY);
        
        // Render nodes within the clipped area (without roman numerals)
        renderNodesWithoutNumerals(guiGraphics, startX, startY, mouseX, mouseY);
        
        // Render the frame overlay within the clipped area
        renderFrameOverlay(guiGraphics);
        
        // Render roman numerals after the frame (so they appear below it) - still within scissor
        renderRomanNumerals(guiGraphics, startX, startY);
        
        guiGraphics.disableScissor();
        
        // Render tooltip outside the clipped area
        if (hoveredNode != null) {
            tooltipRenderer.renderNodeTooltip(guiGraphics, font, hoveredNode, hoveredAllocation, mouseX, mouseY);
        }
        
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        // Render custom back button icon (above frame and roman numerals)
        renderBackButtonIcon(guiGraphics, mouseX, mouseY);
        
        // Render the compact header at the very top (highest z-order, above everything)
        renderHeader(guiGraphics);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200; // Reduced height
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        // Enable scissor test for background, connections, and nodes to keep them within the panel
        guiGraphics.enableScissor(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        
        // Render the parallax background within the clipped area
        renderParallaxBackground(guiGraphics, panelX, panelY, panelWidth, panelHeight);
        
        // Add a subtle border for visual definition (within clipped area)
        guiGraphics.fill(panelX - 1, panelY - 1, panelX + panelWidth + 1, panelY, 0xFF404040);
        guiGraphics.fill(panelX - 1, panelY + panelHeight, panelX + panelWidth + 1, panelY + panelHeight + 1, 0xFF404040);
        guiGraphics.fill(panelX - 1, panelY, panelX, panelY + panelHeight, 0xFF404040);
        guiGraphics.fill(panelX + panelWidth, panelY, panelX + panelWidth + 1, panelY + panelHeight, 0xFF404040);
        
        // Render connections with scissor test to fix clipping
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, startX, startY);
        
        // Render nodes within the clipped area (without roman numerals)
        renderNodesWithoutNumerals(guiGraphics, startX, startY, mouseX, mouseY);
        
        guiGraphics.disableScissor();
        
        // Render tooltip outside the clipped area
        if (hoveredNode != null) {
            tooltipRenderer.renderNodeTooltip(guiGraphics, font, hoveredNode, hoveredAllocation, mouseX, mouseY);
        }
    }
    
    private PerkNode hoveredNode = null;
    private PerkAllocation hoveredAllocation = null;
    
    private void renderParallaxBackground(GuiGraphics guiGraphics, int panelX, int panelY, int panelWidth, int panelHeight) {
        // Calculate parallax offset based on scroll position
        // The background moves slower than the content for a parallax effect
        float parallaxFactor = 0.3f; // Adjust this value for more/less parallax effect (0.0 = no parallax, 1.0 = full parallax)
        int parallaxOffsetX = (int) (scrollX * parallaxFactor);
        int parallaxOffsetY = (int) (scrollY * parallaxFactor);
        
        // The texture is 410x410, so we tile it to create a seamless background
        int textureSize = 410;
        
        // Render the background texture with tiling and parallax offset
        // We render extra tiles around the panel to ensure coverage during parallax movement
        for (int x = panelX - textureSize + parallaxOffsetX; x < panelX + panelWidth + textureSize; x += textureSize) {
            for (int y = panelY - textureSize + parallaxOffsetY; y < panelY + panelHeight + textureSize; y += textureSize) {
                guiGraphics.blit(PERK_TREE_BACKGROUND, x, y, 0, 0, textureSize, textureSize, textureSize, textureSize);
            }
        }
    }
    
    private void renderHeader(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int headerWidth = 30;
        int headerHeight = 14;
        int headerX = panelX + (panelWidth - headerWidth) / 2; // Center within the panel
        int headerY = panelY - headerHeight - 5 + (headerHeight * 2); // Position above the panel, moved down by 200% of its height
        
        // Render black background
        guiGraphics.fill(headerX, headerY, headerX + headerWidth, headerY + headerHeight, 0xFF000000);
        
        // Get available perk points
        int availablePoints = affinityData.getAvailablePoints(school);
        
        // Render school icon on the left side (same as overview screen)
        ResourceLocation iconTexture = school.getTexturePath();
        int iconSize = 16; // Same size as overview screen
        int iconX = headerX + 2;
        int iconY = headerY - 1; // Adjust for larger icon
        guiGraphics.blit(iconTexture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
        
        // Render available points text on the right side
        String pointsText = String.valueOf(availablePoints);
        int textX = headerX + headerWidth - font.width(pointsText) - 2;
        int textY = headerY + 3;
        guiGraphics.drawString(font, pointsText, textX, textY, 0xFFFFFF);
    }
    
    private void renderBackButtonIcon(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonWidth = DocAssets.ARROW_BACK.width();
        int buttonHeight = DocAssets.ARROW_BACK.height();
        int buttonX = panelX + 10; // Position relative to panel
        int buttonY = panelY + 10; // Position relative to panel
        
        // Determine which texture to use based on hover state
        boolean isHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
                           mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        
        var backIcon = isHovered ? DocAssets.ARROW_BACK_HOVER : DocAssets.ARROW_BACK;
        
        // Render the back arrow icon
        guiGraphics.blit(
            backIcon.location(), 
            buttonX, buttonY, 
            backIcon.u(), backIcon.v(), 
            buttonWidth, buttonHeight, 
            backIcon.width(), backIcon.height()
        );
    }
    
    private void renderFrameOverlay(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        // Frame texture dimensions: 258x202
        int frameWidth = 258;
        int frameHeight = 202;
        int frameX = panelX - 1; // Align with panel edges
        int frameY = panelY - 1; // Align with panel edges
        
        // Render the frame texture on top of everything (highest Z-order)
        guiGraphics.blit(PERK_TREE_FRAME, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);
    }
    
    private void renderNodes(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        hoveredNode = null;
        hoveredAllocation = null;
        
        // Use the new dependency-based positioning for all nodes
        for (PerkNode node : schoolPerks.values()) {
            int nodeX = layout.getNodeX(node, startX);
            int nodeY = layout.getNodeY(node, startY);
            
            nodeRenderer.renderNode(guiGraphics, font, node, nodeX, nodeY, mouseX, mouseY);
            
            if (nodeRenderer.isNodeHovered(node, nodeX, nodeY, mouseX, mouseY)) {
                hoveredNode = node;
                hoveredAllocation = allocatedPerks.get(node.getId());
            }
        }
    }
    
    private void renderNodesWithoutNumerals(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        hoveredNode = null;
        hoveredAllocation = null;
        
        // Use the new dependency-based positioning for all nodes
        for (PerkNode node : schoolPerks.values()) {
            int nodeX = layout.getNodeX(node, startX);
            int nodeY = layout.getNodeY(node, startY);
            
            // Render node without roman numerals
            nodeRenderer.renderNodeWithoutNumerals(guiGraphics, font, node, nodeX, nodeY, mouseX, mouseY);
            
            if (nodeRenderer.isNodeHovered(node, nodeX, nodeY, mouseX, mouseY)) {
                hoveredNode = node;
                hoveredAllocation = allocatedPerks.get(node.getId());
            }
        }
    }
    
    private void renderRomanNumerals(GuiGraphics guiGraphics, int startX, int startY) {
        // Render only the roman numerals for each node
        for (PerkNode node : schoolPerks.values()) {
            int nodeX = layout.getNodeX(node, startX);
            int nodeY = layout.getNodeY(node, startY);
            
            boolean isActiveAbility = node.getPerkType().name().startsWith("ACTIVE_");
            int nodeSize = isActiveAbility ? 28 : 24; // ACTIVE_NODE_SIZE : NODE_SIZE
            
            String levelText = toRomanNumeral(node.getTier());
            guiGraphics.drawString(font, levelText, nodeX + nodeSize - 8, nodeY + nodeSize - 8, 0XFFFFFF);
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // First, let the parent class handle button clicks
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0) {
            // Check if back button icon was clicked
            if (handleBackButtonClick((int) mouseX, (int) mouseY)) {
                return true;
            }
            
            if (handleNodeClick((int) mouseX, (int) mouseY)) {
                return true;
            }
            isDragging = true;
            return true;
        }
        
        return false;
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            scrollX += (int) deltaX;
            scrollY += (int) deltaY;
            
            // Clamp based on actual tree bounds
            int treeMinX = layout.getTreeMinX();
            int treeMaxX = layout.getTreeMaxX();
            int treeMinY = layout.getTreeMinY();
            int treeMaxY = layout.getTreeMaxY();
            
            // Calculate bounds with some padding
            // When scrollX = 0, tree is at: getStartX(width, 0) = width/2
            // Tree bounds are: treeMinX to treeMaxX (relative to tree origin)
            // We want to allow scrolling so the tree can move within screen bounds
            int padding = 50;
            
            // For left bound: allow the rightmost part of tree to reach left edge of screen
            // Rightmost tree position at scrollX = minScrollX: width/2 + minScrollX + treeMaxX
            // We want this to be >= 0, so: width/2 + minScrollX + treeMaxX >= 0
            // Therefore: minScrollX >= -width/2 - treeMaxX
            int minScrollX = -width/2 - treeMaxX - padding;
            
            // For right bound: allow the leftmost part of tree to reach right edge of screen  
            // Leftmost tree position at scrollX = maxScrollX: width/2 + maxScrollX + treeMinX
            // We want this to be <= width, so: width/2 + maxScrollX + treeMinX <= width
            // Therefore: maxScrollX <= width/2 - treeMinX
            int maxScrollX = width/2 - treeMinX + padding;
            
            int minScrollY = -treeMaxY - padding;
            int maxScrollY = -treeMinY + height - padding;
            
            scrollX = Math.max(minScrollX, Math.min(maxScrollX, scrollX));
            scrollY = Math.max(minScrollY, Math.min(maxScrollY, scrollY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private boolean handleBackButtonClick(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = 256;
        int panelHeight = 200;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonWidth = DocAssets.ARROW_BACK.width();
        int buttonHeight = DocAssets.ARROW_BACK.height();
        int buttonX = panelX + 10;
        int buttonY = panelY + 10;
        
        if (mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
            mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
            System.out.println("Back button clicked!"); // Debug logging
            onClose();
            return true;
        }
        
        return false;
    }
    
    private boolean handleNodeClick(int mouseX, int mouseY) {
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        PerkNode clickedNode = layout.getNodeAt(mouseX, mouseY, startX, startY);
        if (clickedNode != null) {
            PerkAllocation allocation = allocatedPerks.get(clickedNode.getId());
            if (allocation != null && allocation.isActive()) {
                PerkAllocationManager.deallocatePerk(player, clickedNode.getId());
            } else {
                PerkAllocationManager.allocatePoints(player, clickedNode.getId(), 1);
            }
            
            allocatedPerks.clear();
            allocatedPerks.putAll(PerkAllocationManager.getAllocatedPerks(player, school));
            
            return true;
        }
        
        return false;
    }
    
    @Override
    public void onClose() {
        minecraft.setScreen(previousScreen);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC key
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
    
}
