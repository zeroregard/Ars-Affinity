package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.mojang.blaze3d.systems.RenderSystem;
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
    private static final ResourceLocation PERK_NODE_TEXTURE = ArsAffinity.prefix("textures/gui/perk_node.png");
    private static final ResourceLocation PERK_LINE_TEXTURE = ArsAffinity.prefix("textures/gui/perk_line.png");
    
    private static final int NODE_SIZE = 24;
    private static final int NODE_SPACING = 40;
    private static final int TIER_SPACING = 60;
    private static final int MAX_NODES_PER_TIER = 4;
    
    private final Player player;
    private final SpellSchool school;
    private final Screen previousScreen;
    private final Map<String, PerkNode> schoolPerks;
    private final Map<String, PerkAllocation> allocatedPerks;
    private final PlayerAffinityData affinityData;
    private final Map<Integer, List<PerkNode>> perksByTier; // Cached for performance
    
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
        this.perksByTier = groupPerksByTier(); // Cache the grouping
    }
    
    @Override
    protected void init() {
        super.init();
        
        // Back button
        addRenderableWidget(Button.builder(
            Component.translatable("gui.back"),
            button -> onClose()
        ).bounds(10, 10, 60, 20).build());
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Don't call super.render() to avoid pause screen blur
        renderCustomBackground(guiGraphics);
        
        // Render the perk tree
        renderPerkTree(guiGraphics, mouseX, mouseY);
        
        // Render UI elements manually
        for (var renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        
        // Render info panel
        renderInfoPanel(guiGraphics, mouseX, mouseY);
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent blur effect
        renderCustomBackground(guiGraphics);
    }
    
    private void renderCustomBackground(GuiGraphics guiGraphics) {
        // Render background panel similar to AffinityScreen
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - 256 / 2;
        int panelY = centerY - 256 / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, 256, 256, 256, 256);
    }
    
    private void renderPerkTree(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int startX = width / 2 - (MAX_NODES_PER_TIER * NODE_SPACING) / 2 + scrollX;
        int startY = 50 + scrollY;
        
        // Render connections first (so they appear behind nodes)
        renderConnections(guiGraphics, perksByTier, startX, startY);
        
        // Render nodes
        renderNodes(guiGraphics, perksByTier, startX, startY, mouseX, mouseY);
    }
    
    private Map<Integer, List<PerkNode>> groupPerksByTier() {
        Map<Integer, List<PerkNode>> tiers = new HashMap<>();
        
        for (PerkNode node : schoolPerks.values()) {
            tiers.computeIfAbsent(node.getTier(), k -> new ArrayList<>()).add(node);
        }
        
        // Sort nodes within each tier
        for (List<PerkNode> tier : tiers.values()) {
            tier.sort(Comparator.comparing(PerkNode::getId));
        }
        
        return tiers;
    }
    
    private void renderConnections(GuiGraphics guiGraphics, Map<Integer, List<PerkNode>> perksByTier, int startX, int startY) {
        RenderSystem.setShaderTexture(0, PERK_LINE_TEXTURE);
        
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
        
        // Simple line rendering - could be enhanced with proper line textures
        guiGraphics.fill(fromX + NODE_SIZE / 2, fromY + NODE_SIZE / 2, 
                        toX + NODE_SIZE / 2, toY + NODE_SIZE / 2, 0xFF888888);
    }
    
    private void renderNodes(GuiGraphics guiGraphics, Map<Integer, List<PerkNode>> perksByTier, int startX, int startY, int mouseX, int mouseY) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : perksByTier.entrySet()) {
            int tier = tierEntry.getKey();
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                int nodeX = startX + i * NODE_SPACING;
                int nodeY = startY + tier * TIER_SPACING;
                
                renderPerkNode(guiGraphics, node, nodeX, nodeY, mouseX, mouseY);
            }
        }
    }
    
    private void renderPerkNode(GuiGraphics guiGraphics, PerkNode node, int x, int y, int mouseX, int mouseY) {
        PerkAllocation allocation = allocatedPerks.get(node.getId());
        boolean isAllocated = allocation != null && allocation.isActive();
        boolean isAvailable = PerkAllocationManager.canAllocate(player, node.getId()); // Safe check method
        boolean isHovered = mouseX >= x && mouseX < x + NODE_SIZE && mouseY >= y && mouseY < y + NODE_SIZE;
        
        // Determine node state and color
        int color = getNodeColor(node, isAllocated, isAvailable);
        
        // Render node background
        RenderSystem.setShaderColor(
            (color >> 16 & 0xFF) / 255.0f,
            (color >> 8 & 0xFF) / 255.0f,
            (color & 0xFF) / 255.0f,
            1.0f
        );
        
        guiGraphics.blit(PERK_NODE_TEXTURE, x, y, 0, 0, NODE_SIZE, NODE_SIZE, NODE_SIZE, NODE_SIZE);
        
        // Render node icon (placeholder - would need actual perk icons)
        guiGraphics.fill(x + 4, y + 4, x + NODE_SIZE - 4, y + NODE_SIZE - 4, 0xFFFFFFFF);
        
        // Render level for multi-level perks
        if (isAllocated && node.getLevel() > 1) {
            String levelText = "Lv." + String.valueOf(node.getLevel());
            guiGraphics.drawString(font, levelText, x + NODE_SIZE - 8, y + NODE_SIZE - 8, 0xFFFFFF);
        }
        
        // Render tooltip on hover
        if (isHovered) {
            renderNodeTooltip(guiGraphics, node, allocation, mouseX, mouseY);
        }
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    private int getNodeColor(PerkNode node, boolean isAllocated, boolean isAvailable) {
        if (isAllocated) {
            return 0xFF00FF00; // Green for allocated
        } else if (isAvailable) {
            return 0xFF0088FF; // Blue for available
        } else {
            return 0xFF666666; // Gray for unavailable
        }
    }
    
    private void renderNodeTooltip(GuiGraphics guiGraphics, PerkNode node, PerkAllocation allocation, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        
        // Node name (formatted from ID)
        String displayName = formatPerkName(node.getId());
        tooltip.add(Component.literal(displayName).withStyle(style -> style.withColor(0xFFFFFF)));
        
        // Node description
        tooltip.add(Component.literal("Cost: " + node.getPointCost() + " points").withStyle(style -> style.withColor(0xAAAAAA)));
        
        if (node.getLevel() > 1) {
            tooltip.add(Component.literal("Level: " + node.getLevel()).withStyle(style -> style.withColor(0xAAAAAA)));
        }
        
        if (allocation != null) {
            tooltip.add(Component.literal("✓ Allocated").withStyle(style -> style.withColor(0x00FF00)));
        } else if (PerkAllocationManager.canAllocate(player, node.getId())) {
            tooltip.add(Component.literal("Click to allocate").withStyle(style -> style.withColor(0x0088FF)));
        } else {
            tooltip.add(Component.literal("Prerequisites not met").withStyle(style -> style.withColor(0xFF6666)));
        }
        
        // Prerequisites
        if (!node.getPrerequisites().isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Prerequisites:").withStyle(style -> style.withColor(0xAAAAAA)));
            for (String prereq : node.getPrerequisites()) {
                boolean prereqMet = allocatedPerks.containsKey(prereq);
                String prereqName = formatPerkName(prereq);
                Component prereqText = Component.literal("  " + (prereqMet ? "✓ " : "✗ ") + prereqName)
                    .withStyle(style -> style.withColor(prereqMet ? 0x00FF00 : 0xFF6666));
                tooltip.add(prereqText);
            }
        }
        
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }
    
    private String formatPerkName(String nodeId) {
        // Convert "fire_thorns_1" to "Fire Thorns I"
        String[] parts = nodeId.split("_");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) result.append(" ");
            
            String part = parts[i];
            if (part.matches("\\d+")) {
                // Convert number to roman numeral
                int num = Integer.parseInt(part);
                result.append(toRomanNumeral(num));
            } else {
                // Capitalize first letter
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        
        return result.toString();
    }
    
    private String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }
    
    private void renderInfoPanel(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int panelX = 10;
        int panelY = height - 80;
        int panelWidth = 200;
        int panelHeight = 70;
        
        // Background
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);
        
        // School info
        guiGraphics.drawString(font, school.getTextComponent(), panelX + 5, panelY + 5, 0xFFFFFF);
        
        // Points info
        int availablePoints = affinityData.getAvailablePoints(school);
        int totalPoints = affinityData.getSchoolPoints(school);
        guiGraphics.drawString(font, "Available: " + availablePoints, panelX + 5, panelY + 20, 0xFFFFFF);
        guiGraphics.drawString(font, "Total: " + totalPoints, panelX + 5, panelY + 35, 0xFFFFFF);
        
        // Allocated perks count
        int allocatedCount = allocatedPerks.size();
        guiGraphics.drawString(font, "Perks: " + allocatedCount, panelX + 5, panelY + 50, 0xFFFFFF);
    }
    
    private int getNodeX(PerkNode node, int startX) {
        // Calculate position within the tier
        List<PerkNode> tierNodes = perksByTier.get(node.getTier());
        if (tierNodes == null) return startX;
        
        int index = tierNodes.indexOf(node);
        if (index == -1) return startX;
        
        return startX + index * NODE_SPACING;
    }
    
    private int getNodeY(PerkNode node, int startY) {
        return startY + node.getTier() * TIER_SPACING;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            // Check for node clicks first
            if (handleNodeClick((int) mouseX, (int) mouseY)) {
                return true;
            }
            // If not clicking on a node, start dragging
            isDragging = true;
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
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
            // Add scroll limits
            scrollX = Math.max(-200, Math.min(200, scrollX));
            scrollY = Math.max(-200, Math.min(200, scrollY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    private boolean handleNodeClick(int mouseX, int mouseY) {
        // Find clicked node and handle allocation/deallocation
        int startX = width / 2 - (MAX_NODES_PER_TIER * NODE_SPACING) / 2 + scrollX;
        int startY = 50 + scrollY;
        
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : perksByTier.entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                int nodeX = startX + i * NODE_SPACING;
                int nodeY = startY + node.getTier() * TIER_SPACING;
                
                if (mouseX >= nodeX && mouseX < nodeX + NODE_SIZE && 
                    mouseY >= nodeY && mouseY < nodeY + NODE_SIZE) {
                    
                    PerkAllocation allocation = allocatedPerks.get(node.getId());
                    if (allocation != null && allocation.isActive()) {
                        // Deallocate
                        PerkAllocationManager.deallocatePerk(player, node.getId());
                    } else {
                        // Allocate
                        PerkAllocationManager.allocatePoints(player, node.getId(), 1);
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
