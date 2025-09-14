package com.github.ars_affinity.client.screen.perk;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Renders a dynamic, animated galaxy background for the perk tree UI.
 * Uses procedural generation for stars, nebula effects, and color gradients.
 */
public class GalaxyBackgroundRenderer {
    
    private static final int STAR_COUNT = 150; // Reduced for better performance
    private static final int NEBULA_LAYERS = 2; // Reduced for better performance
    private static final float ANIMATION_SPEED = 0.3f; // Slower for smoother performance
    private static final int CACHE_SIZE = 64; // Size of cached nebula data
    private static final float CACHE_UPDATE_INTERVAL = 0.1f; // Update cache every 100ms
    
    private final List<Star> stars;
    private final List<NebulaLayer> nebulaLayers;
    private final Random random;
    private long lastUpdateTime;
    private float animationTime;
    private float lastCacheUpdate;
    
    // Performance optimization: cache nebula data
    private final int[][] nebulaCache;
    
    public GalaxyBackgroundRenderer() {
        this.stars = new ArrayList<>();
        this.nebulaLayers = new ArrayList<>();
        this.random = new Random(42L); // Fixed seed for consistent generation
        this.lastUpdateTime = System.currentTimeMillis();
        this.lastCacheUpdate = 0.0f;
        
        // Initialize cache
        this.nebulaCache = new int[NEBULA_LAYERS][CACHE_SIZE * CACHE_SIZE];
        
        generateStars();
        generateNebulaLayers();
    }
    
    private void generateStars() {
        for (int i = 0; i < STAR_COUNT; i++) {
            float x = random.nextFloat() * 2.0f - 1.0f; // -1 to 1
            float y = random.nextFloat() * 2.0f - 1.0f; // -1 to 1
            float brightness = 0.3f + random.nextFloat() * 0.7f;
            float twinkleSpeed = 0.5f + random.nextFloat() * 1.5f;
            int color = generateStarColor();
            
            stars.add(new Star(x, y, brightness, twinkleSpeed, color));
        }
    }
    
    private void generateNebulaLayers() {
        for (int i = 0; i < NEBULA_LAYERS; i++) {
            float intensity = 0.2f + random.nextFloat() * 0.4f;
            float speed = 0.1f + random.nextFloat() * 0.3f;
            int color1 = generateNebulaColor();
            int color2 = generateNebulaColor();
            
            nebulaLayers.add(new NebulaLayer(intensity, speed, color1, color2));
        }
    }
    
    private int generateStarColor() {
        // Generate colors ranging from blue-white to yellow-white
        float hue = random.nextFloat() * 0.2f + 0.5f; // Blue to yellow range
        float saturation = 0.3f + random.nextFloat() * 0.4f;
        float brightness = 0.8f + random.nextFloat() * 0.2f;
        
        return hsbToRgb(hue, saturation, brightness);
    }
    
    private int generateNebulaColor() {
        // Generate deep space colors: purples, blues, magentas
        float hue = random.nextFloat() * 0.3f + 0.7f; // Purple to magenta range
        float saturation = 0.4f + random.nextFloat() * 0.4f;
        float brightness = 0.2f + random.nextFloat() * 0.3f;
        
        return hsbToRgb(hue, saturation, brightness);
    }
    
    private int hsbToRgb(float h, float s, float b) {
        int r = (int) (Mth.lerp(s, 1.0f, Mth.clamp(Math.abs(6.0f * h - 3.0f) - 1.0f, 0.0f, 1.0f)) * 255);
        int g = (int) (Mth.lerp(s, 1.0f, Mth.clamp(2.0f - Math.abs(6.0f * h - 2.0f), 0.0f, 1.0f)) * 255);
        int blue = (int) (Mth.lerp(s, 1.0f, Mth.clamp(2.0f - Math.abs(6.0f * h - 4.0f), 0.0f, 1.0f)) * 255);
        
        r = (int) (r * b);
        g = (int) (g * b);
        blue = (int) (blue * b);
        
        return (255 << 24) | (r << 16) | (g << 8) | blue;
    }
    
    public void render(GuiGraphics guiGraphics, int x, int y, int width, int height, float partialTick) {
        updateAnimation(partialTick);
        
        // Render nebula background layers
        renderNebulaLayers(guiGraphics, x, y, width, height);
        
        // Render stars
        renderStars(guiGraphics, x, y, width, height);
        
        // Render subtle gradient overlay for depth
        renderGradientOverlay(guiGraphics, x, y, width, height);
    }
    
    private void updateAnimation(float partialTick) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000.0f;
        lastUpdateTime = currentTime;
        
        animationTime += deltaTime * ANIMATION_SPEED;
    }
    
    private void renderNebulaLayers(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Update cache if needed
        if (animationTime - lastCacheUpdate > CACHE_UPDATE_INTERVAL) {
            updateNebulaCache();
            lastCacheUpdate = animationTime;
        }
        
        for (int i = 0; i < nebulaLayers.size(); i++) {
            // Use cached data for better performance
            int cacheIndex = 0;
            for (int px = 0; px < width; px += 4) {
                for (int py = 0; py < height; py += 4) {
                    if (cacheIndex < nebulaCache[i].length) {
                        int color = nebulaCache[i][cacheIndex];
                        if (color != 0) {
                            guiGraphics.fill(px, py, px + 4, py + 4, color);
                        }
                        cacheIndex++;
                    }
                }
            }
        }
    }
    
    private void updateNebulaCache() {
        for (int i = 0; i < nebulaLayers.size(); i++) {
            NebulaLayer layer = nebulaLayers.get(i);
            
            float offsetX = (float) Math.sin(animationTime * layer.speed + i) * 0.1f;
            float offsetY = (float) Math.cos(animationTime * layer.speed * 0.7f + i) * 0.1f;
            
            int cacheIndex = 0;
            for (int px = 0; px < CACHE_SIZE; px++) {
                for (int py = 0; py < CACHE_SIZE; py++) {
                    float normalizedX = (float) px / CACHE_SIZE;
                    float normalizedY = (float) py / CACHE_SIZE;
                    
                    float distance = (float) Math.sqrt(
                        Math.pow(normalizedX - 0.5f, 2) + 
                        Math.pow(normalizedY - 0.5f, 2)
                    ) * 2.0f;
                    
                    if (distance > 1.0f) {
                        nebulaCache[i][cacheIndex] = 0;
                    } else {
                        // Noise-based intensity
                        float noiseX = (px + offsetX * CACHE_SIZE) / 64.0f;
                        float noiseY = (py + offsetY * CACHE_SIZE) / 64.0f;
                        float noise = (float) (Math.sin(noiseX) * Math.cos(noiseY) + 
                                             Math.sin(noiseX * 2.1f) * Math.cos(noiseY * 2.1f) * 0.5f);
                        
                        float intensity = layer.intensity * (1.0f - distance) * (0.5f + noise * 0.5f);
                        
                        if (intensity > 0.1f) {
                            int color = blendColors(layer.color1, layer.color2, noise * 0.5f + 0.5f);
                            int alpha = (int) (intensity * 100);
                            nebulaCache[i][cacheIndex] = (alpha << 24) | (color & 0x00FFFFFF);
                        } else {
                            nebulaCache[i][cacheIndex] = 0;
                        }
                    }
                    cacheIndex++;
                }
            }
        }
    }
    
    private void renderStars(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        for (Star star : stars) {
            float twinkle = (float) Math.sin(animationTime * star.twinkleSpeed) * 0.3f + 0.7f;
            float brightness = star.brightness * twinkle;
            
            int screenX = (int) (x + (star.x + 1.0f) * width / 2.0f);
            int screenY = (int) (y + (star.y + 1.0f) * height / 2.0f);
            
            if (screenX >= x && screenX < x + width && screenY >= y && screenY < y + height) {
                int color = (int) (brightness * 255) << 24 | (star.color & 0x00FFFFFF);
                int size = brightness > 0.8f ? 2 : 1;
                
                guiGraphics.fill(screenX - size/2, screenY - size/2, 
                               screenX + size/2 + 1, screenY + size/2 + 1, color);
            }
        }
    }
    
    private void renderGradientOverlay(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        // Create a subtle radial gradient for depth
        int centerX = x + width / 2;
        int centerY = y + height / 2;
        int maxRadius = Math.max(width, height) / 2;
        
        for (int r = 0; r < maxRadius; r += 2) {
            float alpha = (1.0f - (float) r / maxRadius) * 0.1f;
            int color = (int) (alpha * 255) << 24 | 0x000000;
            
            // Draw circle outline
            for (int angle = 0; angle < 360; angle += 2) {
                double rad = Math.toRadians(angle);
                int px = centerX + (int) (Math.cos(rad) * r);
                int py = centerY + (int) (Math.sin(rad) * r);
                
                if (px >= x && px < x + width && py >= y && py < y + height) {
                    guiGraphics.fill(px, py, px + 1, py + 1, color);
                }
            }
        }
    }
    
    private int blendColors(int color1, int color2, float ratio) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;
        
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;
        
        int r = (int) (r1 + (r2 - r1) * ratio);
        int g = (int) (g1 + (g2 - g1) * ratio);
        int b = (int) (b1 + (b2 - b1) * ratio);
        
        return (r << 16) | (g << 8) | b;
    }
    
    private static class Star {
        final float x, y, brightness, twinkleSpeed;
        final int color;
        
        Star(float x, float y, float brightness, float twinkleSpeed, int color) {
            this.x = x;
            this.y = y;
            this.brightness = brightness;
            this.twinkleSpeed = twinkleSpeed;
            this.color = color;
        }
    }
    
    private static class NebulaLayer {
        final float intensity, speed;
        final int color1, color2;
        
        NebulaLayer(float intensity, float speed, int color1, int color2) {
            this.intensity = intensity;
            this.speed = speed;
            this.color1 = color1;
            this.color2 = color2;
        }
    }
}
