package com.github.ars_affinity.client.screen.perk;

import net.minecraft.client.Minecraft;

public class PerkTreeViewport {
    private static final float MIN_ZOOM = 0.3f;
    private static final float MAX_ZOOM = 3.0f;
    private static final float ZOOM_SPEED = 0.1f;
    private static final float PAN_SPEED = 1.0f;
    
    private float zoom = 1.0f;
    private float viewportX = 0.0f;
    private float viewportY = 0.0f;
    private float targetZoom = 1.0f;
    private float targetX = 0.0f;
    private float targetY = 0.0f;
    
    private boolean isDragging = false;
    private float lastMouseX = 0.0f;
    private float lastMouseY = 0.0f;
    
    public void update(float partialTick) {
        // Smooth interpolation for zoom and pan
        float lerpSpeed = 0.1f;
        zoom = lerp(zoom, targetZoom, lerpSpeed);
        viewportX = lerp(viewportX, targetX, lerpSpeed);
        viewportY = lerp(viewportY, targetY, lerpSpeed);
    }
    
    public void handleMouseWheel(double delta, int mouseX, int mouseY) {
        float zoomFactor = delta > 0 ? 1.0f + ZOOM_SPEED : 1.0f - ZOOM_SPEED;
        float newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, targetZoom * zoomFactor));
        
        if (newZoom != targetZoom) {
            // Zoom towards mouse position
            float mouseWorldX = (mouseX - viewportX) / targetZoom;
            float mouseWorldY = (mouseY - viewportY) / targetZoom;
            
            targetZoom = newZoom;
            targetX = mouseX - mouseWorldX * targetZoom;
            targetY = mouseY - mouseWorldY * targetZoom;
        }
    }
    
    public void startDragging(int mouseX, int mouseY) {
        isDragging = true;
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }
    
    public void updateDragging(int mouseX, int mouseY) {
        if (isDragging) {
            float deltaX = mouseX - lastMouseX;
            float deltaY = mouseY - lastMouseY;
            
            targetX += deltaX;
            targetY += deltaY;
            
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }
    }
    
    public void stopDragging() {
        isDragging = false;
    }
    
    public void centerOnContent(int screenWidth, int screenHeight, int contentWidth, int contentHeight) {
        targetX = (screenWidth - contentWidth * targetZoom) / 2.0f;
        targetY = (screenHeight - contentHeight * targetZoom) / 2.0f;
    }
    
    public void resetView(int screenWidth, int screenHeight) {
        targetZoom = 1.0f;
        targetX = screenWidth / 2.0f;
        targetY = screenHeight / 2.0f;
    }
    
    public float getZoom() {
        return zoom;
    }
    
    public float getViewportX() {
        return viewportX;
    }
    
    public float getViewportY() {
        return viewportY;
    }
    
    public boolean isDragging() {
        return isDragging;
    }
    
    public float worldToScreenX(float worldX) {
        return worldX * zoom + viewportX;
    }
    
    public float worldToScreenY(float worldY) {
        return worldY * zoom + viewportY;
    }
    
    public float screenToWorldX(float screenX) {
        return (screenX - viewportX) / zoom;
    }
    
    public float screenToWorldY(float screenY) {
        return (screenY - viewportY) / zoom;
    }
    
    private float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }
}
