package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkNode;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;

public class PerkConnectionRenderer {
    private static final int NODE_SIZE = 24;
    private static final int CONNECTION_SEGMENTS = 20;
    
    private final SpellSchool school;
    private final PerkTreeLayout layout;
    
    public PerkConnectionRenderer(Player player, Map<String, PerkAllocation> allocatedPerks, SpellSchool school, PerkTreeLayout layout) {
        this.school = school;
        this.layout = layout;
    }
    
    public void renderConnections(GuiGraphics guiGraphics, Map<Integer, List<PerkNode>> perksByTier, 
                                Map<String, PerkNode> schoolPerks, int startX, int startY) {
        // Render connections between all connected perks
        for (PerkNode node : schoolPerks.values()) {
            for (String prerequisiteId : node.getPrerequisites()) {
                PerkNode prerequisite = schoolPerks.get(prerequisiteId);
                if (prerequisite != null) {
                    renderConnection(guiGraphics, prerequisite, node, startX, startY);
                }
            }
        }
    }
    
    private void renderConnection(GuiGraphics guiGraphics, PerkNode from, PerkNode to, int startX, int startY) {
        int fromX = layout.getNodeX(from, startX);
        int fromY = layout.getNodeY(from, startY);
        int toX = layout.getNodeX(to, startX);
        int toY = layout.getNodeY(to, startY);
        
        // Center the connection on the nodes
        fromX += NODE_SIZE / 2;
        fromY += NODE_SIZE / 2;
        toX += NODE_SIZE / 2;
        toY += NODE_SIZE / 2;
        
        // Create connection path
        PerkConnectionPath path = new PerkConnectionPath(from, to, school, fromX, fromY, toX, toY);
        
        // Render the connection
        renderConnectionPath(guiGraphics, path);
    }
    
    private void renderConnectionPath(GuiGraphics guiGraphics, PerkConnectionPath path) {
        List<BezierCurve.Point> points = path.getPathPoints(CONNECTION_SEGMENTS);
        ConnectionStyle style = path.getStyle();
        
        if (points.size() < 2) return;
        
        // Render glow effect if enabled
        if (style.hasGlow()) {
            renderGlowEffect(guiGraphics, points, style);
        }
        
        // Render main connection line
        renderMainLine(guiGraphics, points, style);
    }
    
    private void renderGlowEffect(GuiGraphics guiGraphics, List<BezierCurve.Point> points, ConnectionStyle style) {
        int glowColor = style.getGlowColor();
        float glowThickness = style.getThickness() + 1.0f; // Much more subtle glow
        
        for (int i = 0; i < points.size() - 1; i++) {
            BezierCurve.Point current = points.get(i);
            BezierCurve.Point next = points.get(i + 1);
            
            // Render subtle glow lines for effect
            for (int offset = -1; offset <= 1; offset++) {
                int x1 = (int) (current.x + offset);
                int y1 = (int) (current.y + offset);
                int x2 = (int) (next.x + offset);
                int y2 = (int) (next.y + offset);
                
                renderLineSegment(guiGraphics, x1, y1, x2, y2, glowColor, glowThickness, false);
            }
        }
    }
    
    private void renderMainLine(GuiGraphics guiGraphics, List<BezierCurve.Point> points, ConnectionStyle style) {
        int color = style.getColor();
        float thickness = style.getThickness();
        boolean isDashed = style.isDashed();
        
        for (int i = 0; i < points.size() - 1; i++) {
            BezierCurve.Point current = points.get(i);
            BezierCurve.Point next = points.get(i + 1);
    
            renderLineSegmentFloat(guiGraphics,
                current.x, current.y,
                next.x, next.y,
                color, thickness, isDashed);
        }
    }

    private void renderLineSegmentFloat(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2,
                                   int color, float thickness, boolean isDashed) {
                                    renderSolidLineFloat(guiGraphics, x1, y1, x2, y2, color, thickness);
    }

    private void renderSolidLineFloat(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2,
                                 int color, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float steps = Math.max(Math.abs(dx), Math.abs(dy));
        
        if (steps < 1) steps = 1;
        if (steps > 1000) steps = 1000; // Safety check

        float xInc = dx / steps;
        float yInc = dy / steps;

        float x = x1;
        float y = y1;

        for (int i = 0; i <= (int)steps; i++) {
            // Use Math.round for proper pixel positioning
            int pixelX = Math.round(x);
            int pixelY = Math.round(y);
            
            // Draw 1x1 pixel for thin lines
            guiGraphics.fill(pixelX, pixelY, pixelX + 1, pixelY + 1, color);
            
            x += xInc;
            y += yInc;
        }
    }


    
    private void renderLineSegment(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, 
                                 int color, float thickness, boolean isDashed) {
        if (isDashed) {
            renderDashedLine(guiGraphics, x1, y1, x2, y2, color, thickness);
        } else {
            renderSolidLine(guiGraphics, x1, y1, x2, y2, color, thickness);
        }
    }
    
    private void renderSolidLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, float thickness) {
        // Use Bresenham's line algorithm for proper thin line rendering
        if (thickness <= 1.0f) {
            renderBresenhamLine(guiGraphics, x1, y1, x2, y2, color);
        } else {
            // For thicker lines, render multiple parallel lines
            int halfThickness = Math.max(1, (int) (thickness / 2));
            for (int offset = -halfThickness; offset <= halfThickness; offset++) {
                renderBresenhamLine(guiGraphics, x1 + offset, y1, x2 + offset, y2, color);
            }
        }
    }
    
    private void renderBresenhamLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color) {
        // Handle edge case where start and end are the same
        if (x1 == x2 && y1 == y2) {
            guiGraphics.fill(x1, y1, x1 + 1, y1 + 1, color);
            return;
        }
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x1 < x2 ? 1 : -1;
        int sy = y1 < y2 ? 1 : -1;
        int err = dx - dy;
        
        int x = x1;
        int y = y1;
        
        // Always draw the first pixel
        guiGraphics.fill(x, y, x + 1, y + 1, color);
        
        // Continue until we reach the end point
        while (x != x2 || y != y2) {
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                y += sy;
            }
            
            // Draw the current pixel
            guiGraphics.fill(x, y, x + 1, y + 1, color);
        }
    }
    
    private void renderDashedLine(GuiGraphics guiGraphics, int x1, int y1, int x2, int y2, int color, float thickness) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        if (distance < 4) {
            renderSolidLine(guiGraphics, x1, y1, x2, y2, color, thickness);
            return;
        }
        
        float dashLength = 8.0f;
        float gapLength = 4.0f;
        float segmentLength = dashLength + gapLength;
        
        float segments = distance / segmentLength;
        float stepX = dx / segments;
        float stepY = dy / segments;
        
        for (int i = 0; i < (int) segments; i++) {
            float startX = x1 + i * stepX;
            float startY = y1 + i * stepY;
            float endX = startX + (stepX * dashLength / segmentLength);
            float endY = startY + (stepY * dashLength / segmentLength);
            
            renderSolidLine(guiGraphics, (int) startX, (int) startY, (int) endX, (int) endY, color, thickness);
        }
    }
    
}
