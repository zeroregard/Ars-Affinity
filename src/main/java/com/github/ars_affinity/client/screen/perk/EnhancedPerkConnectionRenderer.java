package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class EnhancedPerkConnectionRenderer {
    private static final int CONNECTION_WIDTH = 3;
    private static final int GLOW_WIDTH = 6;
    
    public void renderConnections(GuiGraphics guiGraphics, Map<Integer, List<PerkNode>> perksByTier, 
                                Map<String, PerkNode> schoolPerks, float startX, float startY, float zoom) {
        for (Map.Entry<Integer, List<PerkNode>> tierEntry : perksByTier.entrySet()) {
            List<PerkNode> nodes = tierEntry.getValue();
            
            for (PerkNode node : nodes) {
                for (String prerequisiteId : node.getPrerequisites()) {
                    PerkNode prerequisite = schoolPerks.get(prerequisiteId);
                    if (prerequisite != null) {
                        renderConnection(guiGraphics, prerequisite, node, startX, startY, zoom);
                    }
                }
            }
        }
    }
    
    private void renderConnection(GuiGraphics guiGraphics, PerkNode from, PerkNode to, 
                                float startX, float startY, float zoom) {
        float fromX = getNodeX(from, startX);
        float fromY = getNodeY(from, startY);
        float toX = getNodeX(to, startX);
        float toY = getNodeY(to, startY);
        
        // Calculate connection properties
        float distance = Mth.sqrt((toX - fromX) * (toX - fromX) + (toY - fromY) * (toY - fromY));
        float angle = (float) Mth.atan2(toY - fromY, toX - fromX);
        
        // Determine connection style based on distance and tier difference
        boolean isLongConnection = distance > 100;
        boolean isTierJump = Math.abs(to.getTier() - from.getTier()) > 1;
        
        if (isLongConnection || isTierJump) {
            renderCurvedConnection(guiGraphics, fromX, fromY, toX, toY, angle, zoom);
        } else {
            renderStraightConnection(guiGraphics, fromX, fromY, toX, toY, angle, zoom);
        }
    }
    
    private void renderStraightConnection(GuiGraphics guiGraphics, float fromX, float fromY, 
                                       float toX, float toY, float angle, float zoom) {
        // Calculate line endpoints with node radius offset
        float nodeRadius = 16 * zoom;
        float offsetX = Mth.cos(angle) * nodeRadius;
        float offsetY = Mth.sin(angle) * nodeRadius;
        
        float startX = fromX + offsetX;
        float startY = fromY + offsetY;
        float endX = toX - offsetX;
        float endY = toY - offsetY;
        
        // Render connection line
        renderLine(guiGraphics, startX, startY, endX, endY, CONNECTION_WIDTH * zoom, 0xFF888888);
        
        // Render subtle glow
        renderLine(guiGraphics, startX, startY, endX, endY, GLOW_WIDTH * zoom, 0x44888888);
    }
    
    private void renderCurvedConnection(GuiGraphics guiGraphics, float fromX, float fromY, 
                                     float toX, float toY, float angle, float zoom) {
        // Calculate control points for bezier curve
        float distance = Mth.sqrt((toX - fromX) * (toX - fromX) + (toY - fromY) * (toY - fromY));
        float controlOffset = distance * 0.3f;
        
        float controlX1 = fromX + Mth.cos(angle) * controlOffset;
        float controlY1 = fromY + Mth.sin(angle) * controlOffset;
        float controlX2 = toX - Mth.cos(angle) * controlOffset;
        float controlY2 = toY - Mth.sin(angle) * controlOffset;
        
        // Add perpendicular offset for curve
        float perpAngle = angle + Mth.PI / 2;
        float curveOffset = 20 * zoom;
        controlX1 += Mth.cos(perpAngle) * curveOffset;
        controlY1 += Mth.sin(perpAngle) * curveOffset;
        controlX2 += Mth.cos(perpAngle) * curveOffset;
        controlY2 += Mth.sin(perpAngle) * curveOffset;
        
        // Render curved line using multiple line segments
        int segments = (int) (distance / 10);
        segments = Mth.clamp(segments, 8, 32);
        
        float prevX = fromX;
        float prevY = fromY;
        
        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            float currentX = bezierPoint(fromX, controlX1, controlX2, toX, t);
            float currentY = bezierPoint(fromY, controlY1, controlY2, toY, t);
            
            // Render segment
            renderLine(guiGraphics, prevX, prevY, currentX, currentY, CONNECTION_WIDTH * zoom, 0xFF888888);
            
            prevX = currentX;
            prevY = currentY;
        }
        
        // Render subtle glow for curved connections
        prevX = fromX;
        prevY = fromY;
        
        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            float currentX = bezierPoint(fromX, controlX1, controlX2, toX, t);
            float currentY = bezierPoint(fromY, controlY1, controlY2, toY, t);
            
            renderLine(guiGraphics, prevX, prevY, currentX, currentY, GLOW_WIDTH * zoom, 0x44888888);
            
            prevX = currentX;
            prevY = currentY;
        }
    }
    
    private void renderLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float width, int color) {
        // Simple line rendering using filled rectangles
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = Mth.sqrt(dx * dx + dy * dy);
        
        if (length == 0) return;
        
        float normalizedX = dx / length;
        float normalizedY = dy / length;
        
        int segments = (int) (length / 2);
        segments = Mth.clamp(segments, 1, 100);
        
        for (int i = 0; i < segments; i++) {
            float t1 = (float) i / segments;
            float t2 = (float) (i + 1) / segments;
            
            float startX = x1 + dx * t1;
            float startY = y1 + dy * t1;
            float endX = x1 + dx * t2;
            float endY = y1 + dy * t2;
            
            // Draw line segment as rectangle
            float perpX = -normalizedY * width / 2;
            float perpY = normalizedX * width / 2;
            
            int x1i = (int) (startX + perpX);
            int y1i = (int) (startY + perpY);
            int x2i = (int) (endX + perpX);
            int y2i = (int) (endY + perpY);
            int x3i = (int) (endX - perpX);
            int y3i = (int) (endY - perpY);
            int x4i = (int) (startX - perpX);
            int y4i = (int) (startY - perpY);
            
            // Simple quad rendering
            guiGraphics.fill(x1i, y1i, x2i, y2i, color);
            guiGraphics.fill(x2i, y2i, x3i, y3i, color);
            guiGraphics.fill(x3i, y3i, x4i, y4i, color);
            guiGraphics.fill(x4i, y4i, x1i, y1i, color);
        }
    }
    
    private float bezierPoint(float p0, float p1, float p2, float p3, float t) {
        float u = 1 - t;
        float tt = t * t;
        float uu = u * u;
        float uuu = uu * u;
        float ttt = tt * t;
        
        return uuu * p0 + 3 * uu * t * p1 + 3 * u * tt * p2 + ttt * p3;
    }
    
    private float getNodeX(PerkNode node, float startX) {
        return startX + node.getTier() * 40;
    }
    
    private float getNodeY(PerkNode node, float startY) {
        return startY + node.getTier() * 60;
    }
}
