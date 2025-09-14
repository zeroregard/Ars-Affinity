package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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
    
    private static final ResourceLocation BACKGROUND_TEXTURE = ArsAffinity.prefix("textures/gui/affinity_bg.png");
    
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
    private final PerkInfoPanel infoPanel;
    
    private int scrollX = 0;
    private int scrollY = 0;
    private boolean isDragging = false;
    
    // Smooth camera system
    private float targetScrollX = 0;
    private float targetScrollY = 0;
    private boolean isLerping = false;
    private static final float LERP_SPEED = 0.15f; // Adjust for faster/slower lerping
    private long lastDragTime = 0;
    private static final long LERP_DELAY = 500; // Start lerping 500ms after drag ends
    
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
        this.infoPanel = new PerkInfoPanel(school, affinityData, allocatedPerks);
        this.galaxyBackground = new GalaxyBackgroundRenderer();
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Center the view on the root node (column 0)
        centerOnRootNode();
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> {
                System.out.println("Back button clicked!"); // Debug logging
                onClose();
            }
        ).bounds(10, 10, 60, 20).build());
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
        renderCustomBackground(guiGraphics);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 256 / 2;
        int panelY = centerY - 256 / 2;
        
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        // Render connections without scissor test to prevent clipping
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, startX, startY);
        
        // Enable scissor test only for nodes to keep them within the background panel
        guiGraphics.enableScissor(panelX, panelY, panelX + 256, panelY + 256);
        renderNodes(guiGraphics, startX, startY, mouseX, mouseY);
        guiGraphics.disableScissor();
        
        // Render tooltip outside the clipped area
        if (hoveredNode != null) {
            tooltipRenderer.renderNodeTooltip(guiGraphics, font, hoveredNode, hoveredAllocation, mouseX, mouseY);
        }
        
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        infoPanel.render(guiGraphics, font, width, height);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderCustomBackground(guiGraphics);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 256 / 2;
        int panelY = centerY - 256 / 2;
        
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        // Render connections without scissor test to prevent clipping
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, startX, startY);
        
        // Enable scissor test only for nodes to keep them within the background panel
        guiGraphics.enableScissor(panelX, panelY, panelX + 256, panelY + 256);
        renderNodes(guiGraphics, startX, startY, mouseX, mouseY);
        guiGraphics.disableScissor();
        
        // Render tooltip outside the clipped area
        if (hoveredNode != null) {
            tooltipRenderer.renderNodeTooltip(guiGraphics, font, hoveredNode, hoveredAllocation, mouseX, mouseY);
        }
    }
    
    private void renderCustomBackground(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 256 / 2;
        int panelY = centerY - 256 / 2;
        
        // Render the dynamic galaxy background
        galaxyBackground.render(guiGraphics, panelX, panelY, 256, 256, 0.016f); // ~60 FPS delta time
        
        // Add a subtle overlay to maintain the original panel feel
        guiGraphics.fill(panelX, panelY, panelX + 256, panelY + 256, 0x20000000);
    }
    
    private PerkNode hoveredNode = null;
    private PerkAllocation hoveredAllocation = null;
    
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
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // First, let the parent class handle button clicks
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        
        if (button == 0) {
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
            // Start the lerping process after a delay
            lastDragTime = System.currentTimeMillis();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            // Stop any ongoing lerping when user starts dragging
            isLerping = false;
            
            scrollX += (int) deltaX;
            scrollY += (int) deltaY;
            
            // Clamp based on actual tree bounds
            int treeMinX = layout.getTreeMinX();
            int treeMaxX = layout.getTreeMaxX();
            int treeMinY = layout.getTreeMinY();
            int treeMaxY = layout.getTreeMaxY();
            
            // Calculate bounds with some padding
            int padding = 50;
            int minScrollX = -treeMaxX - padding;
            int maxScrollX = -treeMinX + width - padding;
            int minScrollY = -treeMaxY - padding;
            int maxScrollY = -treeMinY + height - padding;
            
            scrollX = Math.max(minScrollX, Math.min(maxScrollX, scrollX));
            scrollY = Math.max(minScrollY, Math.min(maxScrollY, scrollY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
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
    
    private void updateSmoothCamera() {
        if (isDragging) {
            // Don't lerp while dragging
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDragTime < LERP_DELAY) {
            // Wait for the delay before starting lerping
            return;
        }
        
        // Find the closest perk node to the current view center
        PerkNode closestNode = findClosestNodeToViewCenter();
        if (closestNode != null) {
            // Calculate target position to center the closest node
            int nodeX = layout.getNodeX(closestNode, 0);
            int nodeY = layout.getNodeY(closestNode, 0);
            
            // Target scroll position to center the node on screen
            targetScrollX = -nodeX;
            targetScrollY = -nodeY + height / 2 - 50; // 50 is the base offset from getStartY
            
            // Clamp target position to valid bounds
            int treeMinX = layout.getTreeMinX();
            int treeMaxX = layout.getTreeMaxX();
            int treeMinY = layout.getTreeMinY();
            int treeMaxY = layout.getTreeMaxY();
            
            int padding = 50;
            int minScrollX = -treeMaxX - padding;
            int maxScrollX = -treeMinX + width - padding;
            int minScrollY = -treeMaxY - padding;
            int maxScrollY = -treeMinY + height - padding;
            
            targetScrollX = Math.max(minScrollX, Math.min(maxScrollX, targetScrollX));
            targetScrollY = Math.max(minScrollY, Math.min(maxScrollY, targetScrollY));
            
            isLerping = true;
        }
        
        if (isLerping) {
            // Lerp towards the target position
            float deltaX = targetScrollX - scrollX;
            float deltaY = targetScrollY - scrollY;
            
            // Check if we're close enough to stop lerping
            if (Math.abs(deltaX) < 1.0f && Math.abs(deltaY) < 1.0f) {
                scrollX = (int) targetScrollX;
                scrollY = (int) targetScrollY;
                isLerping = false;
            } else {
                scrollX += (int) (deltaX * LERP_SPEED);
                scrollY += (int) (deltaY * LERP_SPEED);
            }
        }
    }
    
    private PerkNode findClosestNodeToViewCenter() {
        // Get the current view center in world coordinates
        int viewCenterX = width / 2 - scrollX; // Convert screen center to world coordinates
        int viewCenterY = height / 2 - scrollY + 50; // Account for getStartY offset
        
        PerkNode closestNode = null;
        double closestDistance = Double.MAX_VALUE;
        
        for (PerkNode node : schoolPerks.values()) {
            int nodeX = layout.getNodeX(node, 0);
            int nodeY = layout.getNodeY(node, 0);
            
            // Calculate distance from view center to node center
            double distance = Math.sqrt(
                Math.pow(nodeX - viewCenterX, 2) + 
                Math.pow(nodeY - viewCenterY, 2)
            );
            
            if (distance < closestDistance) {
                closestDistance = distance;
                closestNode = node;
            }
        }
        
        return closestNode;
    }
}
