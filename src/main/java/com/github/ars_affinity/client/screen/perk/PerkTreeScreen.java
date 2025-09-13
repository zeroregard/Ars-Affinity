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
        this.connectionRenderer = new PerkConnectionRenderer();
        this.tooltipRenderer = new PerkTooltipRenderer(player, allocatedPerks);
        this.infoPanel = new PerkInfoPanel(school, affinityData, allocatedPerks);
    }
    
    @Override
    protected void init() {
        super.init();
        
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> onClose()
        ).bounds(10, 10, 60, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderCustomBackground(guiGraphics);
        
        int startX = layout.getStartX(width, scrollX);
        int startY = layout.getStartY(scrollY);
        
        connectionRenderer.renderConnections(guiGraphics, layout.getPerksByTier(), schoolPerks, startX, startY);
        renderNodes(guiGraphics, startX, startY, mouseX, mouseY);
        
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        infoPanel.render(guiGraphics, font, width, height);
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
    
    private void renderNodes(GuiGraphics guiGraphics, int startX, int startY, int mouseX, int mouseY) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : layout.getPerksByTier().entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                int nodeX = startX + i * 40;
                int nodeY = startY + node.getTier() * 60;
                
                nodeRenderer.renderNode(guiGraphics, font, node, nodeX, nodeY, mouseX, mouseY);
                
                if (nodeRenderer.isNodeHovered(node, nodeX, nodeY, mouseX, mouseY)) {
                    PerkAllocation allocation = allocatedPerks.get(node.getId());
                    tooltipRenderer.renderNodeTooltip(guiGraphics, font, node, allocation, mouseX, mouseY);
                }
            }
        }
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (handleNodeClick((int) mouseX, (int) mouseY)) {
                return true;
            }
            isDragging = true;
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
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
            scrollX = Math.max(-200, Math.min(200, scrollX));
            scrollY = Math.max(-200, Math.min(200, scrollY));
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
}
