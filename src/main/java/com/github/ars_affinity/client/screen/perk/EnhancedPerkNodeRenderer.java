package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class EnhancedPerkNodeRenderer {
    private static final ResourceLocation PERK_NODE_TEXTURE = ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perk_node.png");
    private static final int NODE_SIZE = 32;
    private static final int ICON_SIZE = 20;
    
    private final Player player;
    private final Map<String, PerkAllocation> allocatedPerks;
    private final PerkTreeAnimator animator;
    
    public EnhancedPerkNodeRenderer(Player player, Map<String, PerkAllocation> allocatedPerks, PerkTreeAnimator animator) {
        this.player = player;
        this.allocatedPerks = allocatedPerks;
        this.animator = animator;
    }
    
    public void renderNode(GuiGraphics guiGraphics, Font font, PerkNode node, float x, float y, int mouseX, int mouseY, float zoom) {
        PerkAllocation allocation = allocatedPerks.get(node.getId());
        boolean isAllocated = allocation != null && allocation.isActive();
        boolean isAvailable = PerkAllocationManager.canAllocate(player, node.getId());
        boolean isHovered = isNodeHovered(node, x, y, mouseX, mouseY, zoom);
        
        // Update hover animation
        animator.setNodeHovered(node.getId(), isHovered);
        
        // Get animation values
        float scale = animator.getNodeScale(node.getId());
        float alpha = animator.getNodeAlpha(node.getId());
        float glow = animator.getNodeGlow(node.getId());
        float hoverAnimation = animator.getHoverAnimation(node.getId());
        
        // Calculate final size with zoom and animation
        float finalSize = NODE_SIZE * scale * zoom;
        float finalIconSize = ICON_SIZE * scale * zoom;
        
        // Calculate colors with animations
        int baseColor = getNodeColor(node, isAllocated, isAvailable);
        int glowColor = getGlowColor(baseColor, glow);
        int hoverColor = getHoverColor(baseColor, hoverAnimation);
        
        // Apply alpha
        int finalColor = applyAlpha(hoverColor, alpha);
        
        // Render glow effect
        if (glow > 0.0f) {
            renderGlowEffect(guiGraphics, x, y, finalSize, glow);
        }
        
        // Render node background with animation
        RenderSystem.setShaderColor(
            (finalColor >> 16 & 0xFF) / 255.0f,
            (finalColor >> 8 & 0xFF) / 255.0f,
            (finalColor & 0xFF) / 255.0f,
            alpha
        );
        
        // Render node as circle instead of square for better visual appeal
        renderCircle(guiGraphics, x, y, finalSize / 2);
        
        // Render node icon
        if (finalIconSize > 4) { // Only render icon if large enough
            float iconX = x - finalIconSize / 2;
            float iconY = y - finalIconSize / 2;
            guiGraphics.blit(PERK_NODE_TEXTURE, (int) iconX, (int) iconY, 0, 0, 
                (int) finalIconSize, (int) finalIconSize, (int) finalIconSize, (int) finalIconSize);
        }
        
        // Render level indicator for multi-level perks
        if (isAllocated && node.getLevel() > 1 && finalSize > 16) {
            String levelText = "Lv." + String.valueOf(node.getLevel());
            float textSize = finalSize * 0.3f;
            int textX = (int) (x + finalSize * 0.3f);
            int textY = (int) (y + finalSize * 0.3f);
            
            // Render text with shadow
            guiGraphics.drawString(font, levelText, textX + 1, textY + 1, 0x000000, false);
            guiGraphics.drawString(font, levelText, textX, textY, 0xFFFFFF, false);
        }
        
        // Render hover effect
        if (isHovered) {
            renderHoverEffect(guiGraphics, x, y, finalSize, hoverAnimation);
        }
        
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    public boolean isNodeHovered(PerkNode node, float x, float y, int mouseX, int mouseY, float zoom) {
        float finalSize = NODE_SIZE * zoom;
        float distance = Mth.sqrt((mouseX - x) * (mouseX - x) + (mouseY - y) * (mouseY - y));
        return distance <= finalSize / 2;
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
    
    private int getGlowColor(int baseColor, float glow) {
        if (glow <= 0.0f) return baseColor;
        
        int glowColor = 0xFFFFFFFF;
        int r = (int) Mth.lerp(glow, baseColor >> 16 & 0xFF, glowColor >> 16 & 0xFF);
        int g = (int) Mth.lerp(glow, baseColor >> 8 & 0xFF, glowColor >> 8 & 0xFF);
        int b = (int) Mth.lerp(glow, baseColor & 0xFF, glowColor & 0xFF);
        
        return (r << 16) | (g << 8) | b;
    }
    
    private int getHoverColor(int baseColor, float hoverAnimation) {
        if (hoverAnimation <= 0.0f) return baseColor;
        
        int hoverColor = 0xFFFFFFFF;
        int r = (int) Mth.lerp(hoverAnimation, baseColor >> 16 & 0xFF, hoverColor >> 16 & 0xFF);
        int g = (int) Mth.lerp(hoverAnimation, baseColor >> 8 & 0xFF, hoverColor >> 8 & 0xFF);
        int b = (int) Mth.lerp(hoverAnimation, baseColor & 0xFF, hoverColor & 0xFF);
        
        return (r << 16) | (g << 8) | b;
    }
    
    private int applyAlpha(int color, float alpha) {
        int a = (int) (alpha * 255);
        return (a << 24) | (color & 0x00FFFFFF);
    }
    
    private void renderCircle(GuiGraphics guiGraphics, float x, float y, float radius) {
        // Simple circle rendering using filled rectangles
        int intRadius = (int) radius;
        int intX = (int) (x - radius);
        int intY = (int) (y - radius);
        
        for (int dy = -intRadius; dy <= intRadius; dy++) {
            for (int dx = -intRadius; dx <= intRadius; dx++) {
                if (dx * dx + dy * dy <= radius * radius) {
                    guiGraphics.fill(intX + dx, intY + dy, intX + dx + 1, intY + dy + 1, 0xFFFFFFFF);
                }
            }
        }
    }
    
    private void renderGlowEffect(GuiGraphics guiGraphics, float x, float y, float size, float intensity) {
        // Render multiple circles with decreasing opacity for glow effect
        for (int i = 0; i < 3; i++) {
            float glowSize = size + (i + 1) * 4;
            int glowAlpha = (int) (intensity * 50 / (i + 1));
            int glowColor = (glowAlpha << 24) | 0x00FFFFFF;
            
            renderCircle(guiGraphics, x, y, glowSize / 2);
        }
    }
    
    private void renderHoverEffect(GuiGraphics guiGraphics, float x, float y, float size, float intensity) {
        // Render pulsing ring effect
        float ringSize = size + 4 * intensity;
        int ringAlpha = (int) (intensity * 100);
        int ringColor = (ringAlpha << 24) | 0x00FFFFFF;
        
        // Draw ring outline
        int intRadius = (int) (ringSize / 2);
        int intX = (int) (x - ringSize / 2);
        int intY = (int) (y - ringSize / 2);
        
        for (int dy = -intRadius; dy <= intRadius; dy++) {
            for (int dx = -intRadius; dx <= intRadius; dx++) {
                float distance = Mth.sqrt(dx * dx + dy * dy);
                if (distance >= ringSize / 2 - 2 && distance <= ringSize / 2) {
                    guiGraphics.fill(intX + dx, intY + dy, intX + dx + 1, intY + dy + 1, ringColor);
                }
            }
        }
    }
}
