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
import com.github.ars_affinity.client.screen.SchoolGlyphScreen;

import java.util.*;

/**
 * Enhanced perk tree screen with dynamic rendering, smooth animations, and improved UX.
 * Features zooming, panning, smooth animations, and better visual effects.
 */
public class EnhancedPerkTreeScreen extends Screen {
    
    private static final ResourceLocation BACKGROUND_TEXTURE = ArsAffinity.prefix("textures/gui/affinity_bg.png");
    
    private final Player player;
    private final SpellSchool school;
    private final Screen previousScreen;
    private final Map<String, PerkNode> schoolPerks;
    private final Map<String, PerkAllocation> allocatedPerks;
    private final PlayerAffinityData affinityData;
    
    private final PerkTreeLayout layout;
    private final PerkTreeViewport viewport;
    private final PerkTreeAnimator animator;
    private final EnhancedPerkNodeRenderer nodeRenderer;
    private final EnhancedPerkConnectionRenderer connectionRenderer;
    private final PerkTooltipRenderer tooltipRenderer;
    private final PerkInfoPanel infoPanel;
    
    private PerkNode hoveredNode = null;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    
    public EnhancedPerkTreeScreen(Player player, SpellSchool school, Screen previousScreen) {
        super(Component.translatable("ars_affinity.screen.perk_tree.title", school.getTextComponent()));
        this.player = player;
        this.school = school;
        this.previousScreen = previousScreen;
        this.schoolPerks = PerkTreeManager.getSchoolNodes(school);
        this.affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        this.allocatedPerks = PerkAllocationManager.getAllocatedPerks(player, school);
        
        this.layout = new PerkTreeLayout(schoolPerks);
        this.viewport = new PerkTreeViewport();
        this.animator = new PerkTreeAnimator();
        this.nodeRenderer = new EnhancedPerkNodeRenderer(player, allocatedPerks, animator);
        this.connectionRenderer = new EnhancedPerkConnectionRenderer();
        this.tooltipRenderer = new PerkTooltipRenderer(player, allocatedPerks);
        this.infoPanel = new PerkInfoPanel(school, affinityData, allocatedPerks);
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Back button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> onClose()
        ).bounds(10, 10, 60, 20).build());
        
        // Zoom controls
        addRenderableWidget(Button.builder(
            Component.literal("+"),
            button -> viewport.handleMouseWheel(1, width / 2, height / 2)
        ).bounds(width - 60, 10, 25, 20).build());
        
        addRenderableWidget(Button.builder(
            Component.literal("-"),
            button -> viewport.handleMouseWheel(-1, width / 2, height / 2)
        ).bounds(width - 30, 10, 25, 20).build());
        
        // Reset view button
        addRenderableWidget(Button.builder(
            Component.literal("Reset"),
            button -> viewport.resetView(width, height)
        ).bounds(width - 100, 10, 50, 20).build());
        
        // Glyph button
        addRenderableWidget(Button.builder(
            Component.translatable("ars_affinity.screen.glyphs.button"),
            button -> minecraft.setScreen(new SchoolGlyphScreen(this, school))
        ).bounds(width - 160, 10, 50, 20).build());
        
        // Initialize viewport
        viewport.resetView(width, height);
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Update viewport and animations
        viewport.update(partialTick);
        animator.update(partialTick);
        
        // Render background
        renderCustomBackground(guiGraphics);
        
        // Render perk tree with viewport transformation
        renderPerkTree(guiGraphics, mouseX, mouseY, partialTick);
        
        // Render UI elements
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        // Render info panel
        infoPanel.render(guiGraphics, font, width, height);
        
        // Render zoom indicator
        renderZoomIndicator(guiGraphics);
        
        // Update hovered node
        updateHoveredNode(mouseX, mouseY);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderCustomBackground(guiGraphics);
    }
    
    private void renderCustomBackground(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 256 / 2;
        int panelY = centerY - 256 / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, 256, 256, 256, 256);
    }
    
    private void renderPerkTree(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Calculate world coordinates
        float startX = layout.getStartX(width, 0);
        float startY = layout.getStartY(0);
        
        // Render connections first (behind nodes)
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, 
            startX, startY, viewport.getZoom());
        
        // Render nodes
        renderNodes(guiGraphics, startX, startY, mouseX, mouseY, partialTick);
    }
    
    private void renderNodes(GuiGraphics guiGraphics, float startX, float startY, int mouseX, int mouseY, float partialTick) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : layout.getPerksByTier().entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                float nodeX = startX + i * 40;
                float nodeY = startY + node.getTier() * 60;
                
                // Convert to screen coordinates
                float screenX = viewport.worldToScreenX(nodeX);
                float screenY = viewport.worldToScreenY(nodeY);
                
                // Only render if node is visible on screen
                if (isNodeVisible(screenX, screenY)) {
                    nodeRenderer.renderNode(guiGraphics, font, node, screenX, screenY, mouseX, mouseY, viewport.getZoom());
                    
                    // Check for hover and render tooltip
                    if (nodeRenderer.isNodeHovered(node, screenX, screenY, mouseX, mouseY, viewport.getZoom())) {
                        PerkAllocation allocation = allocatedPerks.get(node.getId());
                        tooltipRenderer.renderNodeTooltip(guiGraphics, font, node, allocation, mouseX, mouseY);
                        hoveredNode = node;
                    }
                }
            }
        }
    }
    
    private boolean isNodeVisible(float screenX, float screenY) {
        float nodeSize = 32 * viewport.getZoom();
        return screenX > -nodeSize && screenX < width + nodeSize && 
               screenY > -nodeSize && screenY < height + nodeSize;
    }
    
    private void updateHoveredNode(int mouseX, int mouseY) {
        if (hoveredNode == null) {
            // Check if we're hovering over any node
            float startX = layout.getStartX(width, 0);
            float startY = layout.getStartY(0);
            
            for (Map.Entry<Integer, List<PerkNode>> tierEntry : layout.getPerksByTier().entrySet()) {
                List<PerkNode> nodes = tierEntry.getValue();
                
                for (int i = 0; i < nodes.size(); i++) {
                    PerkNode node = nodes.get(i);
                    float nodeX = startX + i * 40;
                    float nodeY = startY + node.getTier() * 60;
                    
                    float screenX = viewport.worldToScreenX(nodeX);
                    float screenY = viewport.worldToScreenY(nodeY);
                    
                    if (nodeRenderer.isNodeHovered(node, screenX, screenY, mouseX, mouseY, viewport.getZoom())) {
                        hoveredNode = node;
                        break;
                    }
                }
                if (hoveredNode != null) break;
            }
        }
    }
    
    private void renderZoomIndicator(GuiGraphics guiGraphics) {
        String zoomText = String.format("%.0f%%", viewport.getZoom() * 100);
        int textX = width - 100;
        int textY = height - 20;
        
        guiGraphics.drawString(font, zoomText, textX, textY, 0xFFFFFF);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check for node clicks first
            if (handleNodeClick((int) mouseX, (int) mouseY)) {
                return true;
            }
            // Start dragging
            viewport.startDragging((int) mouseX, (int) mouseY);
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            viewport.stopDragging();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            viewport.updateDragging((int) mouseX, (int) mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        viewport.handleMouseWheel(deltaY, (int) mouseX, (int) mouseY);
        return true;
    }
    
    private boolean handleNodeClick(int mouseX, int mouseY) {
        float startX = layout.getStartX(width, 0);
        float startY = layout.getStartY(0);
        
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : layout.getPerksByTier().entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                float nodeX = startX + i * 40;
                float nodeY = startY + node.getTier() * 60;
                
                float screenX = viewport.worldToScreenX(nodeX);
                float screenY = viewport.worldToScreenY(nodeY);
                
                if (nodeRenderer.isNodeHovered(node, screenX, screenY, mouseX, mouseY, viewport.getZoom())) {
                    PerkAllocation allocation = allocatedPerks.get(node.getId());
                    if (allocation != null && allocation.isActive()) {
                        PerkAllocationManager.deallocatePerk(player, node.getId());
                        animator.animateNodeAllocation(node.getId(), false);
                    } else {
                        PerkAllocationManager.allocatePoints(player, node.getId(), 1);
                        animator.animateNodeAllocation(node.getId(), true);
                    }
                    
                    // Refresh allocated perks
                    allocatedPerks.clear();
                    allocatedPerks.putAll(PerkAllocationManager.getAllocatedPerks(player, school));
                    
                    return true;
                }
            }
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
}
