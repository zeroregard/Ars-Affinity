package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

public class PerkTreeAnimator {
    private final Map<String, NodeAnimation> nodeAnimations = new HashMap<>();
    private final Map<String, Float> hoverAnimations = new HashMap<>();
    
    public void update(float partialTick) {
        // Update node animations
        nodeAnimations.values().forEach(anim -> anim.update(partialTick));
        
        // Update hover animations
        hoverAnimations.replaceAll((key, value) -> {
            float target = value > 0.5f ? 1.0f : 0.0f;
            return Mth.lerp(0.1f, value, target);
        });
    }
    
    public void setNodeHovered(String nodeId, boolean hovered) {
        hoverAnimations.put(nodeId, hovered ? 1.0f : 0.0f);
    }
    
    public float getHoverAnimation(String nodeId) {
        return hoverAnimations.getOrDefault(nodeId, 0.0f);
    }
    
    public void animateNodeAppearance(String nodeId, PerkNode node) {
        NodeAnimation anim = nodeAnimations.computeIfAbsent(nodeId, k -> new NodeAnimation());
        anim.startAppearanceAnimation();
    }
    
    public void animateNodeAllocation(String nodeId, boolean allocated) {
        NodeAnimation anim = nodeAnimations.computeIfAbsent(nodeId, k -> new NodeAnimation());
        anim.startAllocationAnimation(allocated);
    }
    
    public float getNodeScale(String nodeId) {
        NodeAnimation anim = nodeAnimations.get(nodeId);
        return anim != null ? anim.getScale() : 1.0f;
    }
    
    public float getNodeAlpha(String nodeId) {
        NodeAnimation anim = nodeAnimations.get(nodeId);
        return anim != null ? anim.getAlpha() : 1.0f;
    }
    
    public float getNodeGlow(String nodeId) {
        NodeAnimation anim = nodeAnimations.get(nodeId);
        return anim != null ? anim.getGlow() : 0.0f;
    }
    
    private static class NodeAnimation {
        private float scale = 1.0f;
        private float alpha = 1.0f;
        private float glow = 0.0f;
        private float targetScale = 1.0f;
        private float targetAlpha = 1.0f;
        private float targetGlow = 0.0f;
        private int animationTime = 0;
        private int maxAnimationTime = 0;
        
        public void update(float partialTick) {
            if (animationTime < maxAnimationTime) {
                animationTime++;
                float progress = (float) animationTime / maxAnimationTime;
                
                // Smooth easing function
                float easedProgress = 1.0f - (1.0f - progress) * (1.0f - progress);
                
                scale = Mth.lerp(easedProgress, scale, targetScale);
                alpha = Mth.lerp(easedProgress, alpha, targetAlpha);
                glow = Mth.lerp(easedProgress, glow, targetGlow);
            }
        }
        
        public void startAppearanceAnimation() {
            scale = 0.0f;
            alpha = 0.0f;
            targetScale = 1.0f;
            targetAlpha = 1.0f;
            animationTime = 0;
            maxAnimationTime = 20; // 1 second at 20 TPS
        }
        
        public void startAllocationAnimation(boolean allocated) {
            targetScale = allocated ? 1.2f : 1.0f;
            targetGlow = allocated ? 1.0f : 0.0f;
            animationTime = 0;
            maxAnimationTime = 10; // 0.5 seconds
        }
        
        public float getScale() {
            return scale;
        }
        
        public float getAlpha() {
            return alpha;
        }
        
        public float getGlow() {
            return glow;
        }
    }
}
