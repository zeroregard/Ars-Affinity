package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.client.screen.perk.PerkTreeScreen;
import com.github.ars_affinity.client.screen.perk.SchoolColorHelper;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import com.mojang.blaze3d.systems.RenderSystem;

import java.util.List;

public class AffinityScreen extends Screen {

    private static final int BACKGROUND_HEIGHT = 194;
    private static final int ICON_SIZE = 16;
    private static final int PERK_NODE_SIZE = 24;

    private static final ResourceLocation PERK_TREE_BACKGROUND = ArsAffinity.prefix("textures/gui/perk_tree_background.png");
    private static final ResourceLocation PERK_BACKGROUND_TEXTURE = ArsAffinity.prefix("textures/gui/perk_background.png");
    private static final ResourceLocation PERK_BORDER_TEXTURE = ArsAffinity.prefix("textures/gui/perk_border.png");
    private static final ResourceLocation AFFINITY_FRAME = ArsAffinity.prefix("textures/gui/affinity_frame.png");

    private final Player player;
    private final boolean showBackButton;
    private final Screen parentScreen;
    private final List<SpellSchool> schools = List.of(
            SpellSchools.ELEMENTAL_EARTH,      // Top left
            SpellSchools.MANIPULATION,         // Top right
            SpellSchools.ELEMENTAL_FIRE,       // Right top
            SpellSchools.NECROMANCY,           // Right bottom
            SpellSchools.ELEMENTAL_AIR,        // Bottom right
            SpellSchools.CONJURATION,          // Bottom left
            SpellSchools.ELEMENTAL_WATER,      // Left bottom
            SpellSchools.ABJURATION            // Left top
    );
    
    private SpellSchool hoveredSchool = null;
    private SpellSchool previousHoveredSchool = null;
    private int lastMouseX = 0;
    private int lastMouseY = 0;
    private long hoverStartTime = 0;
    private long fadeStartTime = 0;
    private boolean isFadingIn = false;
    private boolean isFadingOut = false;
    private boolean isAnimating = false;
    private boolean helpButtonHovered = false;

    public AffinityScreen(Player player) {
        this(player, false, null);
    }
    
    public AffinityScreen(Player player, boolean showBackButton) {
        this(player, showBackButton, null);
    }
    
    public AffinityScreen(Player player, boolean showBackButton, Screen parentScreen) {
        super(Component.translatable("ars_affinity.screen.affinity.title"));
        this.player = player;
        this.showBackButton = showBackButton;
        this.parentScreen = parentScreen;
    }

    private ResourceLocation getConstellationTexture(SpellSchool school) {
        String schoolName = school.getId();
        return ArsAffinity.prefix("textures/gui/c_" + schoolName + ".png");
    }

    private void updateConstellationAnimation() {
        // Check if hovered school changed and we're not already animating
        if (hoveredSchool != previousHoveredSchool && !isAnimating) {
            long currentTime = System.currentTimeMillis();
            
            if (hoveredSchool != null) {
                // Started hovering a new school
                hoverStartTime = currentTime;
                fadeStartTime = currentTime;
                isFadingIn = true;
                isFadingOut = false;
                isAnimating = true;
                previousHoveredSchool = hoveredSchool;
            } else {
                // Stopped hovering - start fade out but keep previousHoveredSchool
                fadeStartTime = currentTime;
                isFadingIn = false;
                isFadingOut = true;
                isAnimating = true;
                // Don't update previousHoveredSchool here - keep it for fade-out
            }
        }
    }

    private void renderConstellation(GuiGraphics guiGraphics, int centerX, int centerY) {
        // Don't render if no school was ever hovered and we're not fading out
        if (hoveredSchool == null && !isFadingOut) return;
        
        long currentTime = System.currentTimeMillis();
        float alpha = 0.0f;
        
        if (isFadingIn) {
            long fadeElapsed = currentTime - fadeStartTime;
            float fadeProgress = Math.min(fadeElapsed / 200.0f, 1.0f); // 200ms = 0.2 seconds
            alpha = fadeProgress * 0.8f; // Max 80% opacity
            
            if (fadeProgress >= 1.0f) {
                isFadingIn = false;
                isAnimating = false;
            }
        } else if (isFadingOut) {
            long fadeElapsed = currentTime - fadeStartTime;
            float fadeProgress = Math.min(fadeElapsed / 200.0f, 1.0f); // 200ms = 0.2 seconds
            alpha = (1.0f - fadeProgress) * 0.8f; // Max 80% opacity
            
            
            if (fadeProgress >= 1.0f) {
                isFadingOut = false;
                isAnimating = false;
                previousHoveredSchool = null; // Clear the previous school after fade-out completes
            }
        } else {
            alpha = 0.8f; // 80% opacity when fully visible
        }
        
        if (alpha <= 0.0f) {
            // Reset fade-out state when alpha reaches 0
            if (isFadingOut) {
                isFadingOut = false;
                previousHoveredSchool = null;
            }
            return;
        }
        
        // Enable proper alpha blending
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(770, 771); // SRC_ALPHA, ONE_MINUS_SRC_ALPHA
        
        // Set alpha for constellation rendering (multiplies with texture's original alpha)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        
        // Use the previous school during fade-out, current school otherwise
        SpellSchool schoolToRender = hoveredSchool != null ? hoveredSchool : previousHoveredSchool;
        if (schoolToRender == null) return;
        
        // Render constellation centered
        ResourceLocation constellationTexture = getConstellationTexture(schoolToRender);
        int constellationSize = 128; // Adjust size as needed
        int constellationX = centerX - constellationSize / 2;
        int constellationY = centerY - constellationSize / 2;
        
        guiGraphics.blit(constellationTexture, constellationX, constellationY, 0, 0, 
                        constellationSize, constellationSize, constellationSize, constellationSize);
        
        // Reset color
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Don't call super.render() to avoid pause screen blur
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;

        // Update mouse position for parallax effect
        lastMouseX = mouseX;
        lastMouseY = mouseY;

        // Enable scissor test for background and schools to keep them within the panel
        guiGraphics.enableScissor(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        
        renderParallaxBackground(guiGraphics, centerX, centerY);
        
        // Update constellation animation
        updateConstellationAnimation();
        
        // Render constellation behind school icons
        renderConstellation(guiGraphics, centerX, centerY);
        
        renderSchoolOctagon(guiGraphics, centerX, centerY, mouseX, mouseY);
        
        guiGraphics.disableScissor();
        
        // Render the frame overlay outside the clipped area
        renderFrameOverlay(guiGraphics);
        
        // Render back button if opened from spell book
        if (showBackButton) {
            renderBackButtonIcon(guiGraphics, (int) mouseX, (int) mouseY);
        }
        
        // Render help button
        renderHelpButton(guiGraphics, (int) mouseX, (int) mouseY);
        
        // Render help tooltip if hovered
        if (helpButtonHovered) {
            renderHelpTooltip(guiGraphics, (int) mouseX, (int) mouseY);
        }
        
        if (hoveredSchool != null) {
            renderSchoolInfo(guiGraphics, centerX, centerY);
        }
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent blur effect
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;

        // Enable scissor test for background to keep it within the panel
        guiGraphics.enableScissor(panelX, panelY, panelX + panelWidth, panelY + panelHeight);
        
        renderParallaxBackground(guiGraphics, centerX, centerY);
        
        guiGraphics.disableScissor();
    }

    private void renderParallaxBackground(GuiGraphics guiGraphics, int centerX, int centerY) {
        int textureSize = 410;

        for (int x = -textureSize; x < width + textureSize; x += textureSize) {
            for (int y = -textureSize; y < height + textureSize; y += textureSize) {
                guiGraphics.blit(PERK_TREE_BACKGROUND, x, y, 0, 0, textureSize, textureSize, textureSize, textureSize);
            }
        }
    }

    private void renderSchoolOctagon(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        final int iconRadius = 62;
        final int numSchools = schools.size();
        
        hoveredSchool = null;

        for (int i = 0; i < numSchools; i++) {
            SpellSchool school = schools.get(i);
            double angleDeg = -45 + (i * 360.0 / numSchools);
            double angleRad = Math.toRadians(angleDeg);

            // Icon position
            int iconX = (int) (centerX + iconRadius * Math.cos(angleRad)) - ICON_SIZE / 2;
            int iconY = (int) ((centerY + iconRadius * Math.sin(angleRad)) - ICON_SIZE / 2) + 1;
            renderSchoolIcon(guiGraphics, school, iconX, iconY);

            // Calculate perk node bounds for hover detection
            int nodeX = iconX - (PERK_NODE_SIZE - ICON_SIZE) / 2;
            int nodeY = iconY - (PERK_NODE_SIZE - ICON_SIZE) / 2;
            
            if (mouseX >= nodeX && mouseX < nodeX + PERK_NODE_SIZE &&
                    mouseY >= nodeY && mouseY < nodeY + PERK_NODE_SIZE) {
                hoveredSchool = school;
            }
        }
    }


    private void renderSchoolIcon(GuiGraphics guiGraphics, SpellSchool school, int x, int y) {
        // Calculate center position for the perk node (24x24) with icon (16x16) centered
        int nodeX = x - (PERK_NODE_SIZE - ICON_SIZE) / 2;
        int nodeY = y - (PERK_NODE_SIZE - ICON_SIZE) / 2;
        
        // Get player's affinity data to check for available points
        var affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        boolean hasAvailablePoints = affinityData != null && affinityData.getAvailablePoints(school) > 0;
        
        // Render the perk background (no coloring)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(PERK_BACKGROUND_TEXTURE, nodeX, nodeY, 0, 0, PERK_NODE_SIZE, PERK_NODE_SIZE, PERK_NODE_SIZE, PERK_NODE_SIZE);
        
        // Only render the colored border if there are available points to spend
        if (hasAvailablePoints) {
            int schoolColor = SchoolColorHelper.getSchoolColor(school);
            RenderSystem.setShaderColor(
                (schoolColor >> 16 & 0xFF) / 255.0f,
                (schoolColor >> 8 & 0xFF) / 255.0f,
                (schoolColor & 0xFF) / 255.0f,
                1.0f
            );
            guiGraphics.blit(PERK_BORDER_TEXTURE, nodeX, nodeY, 0, 0, PERK_NODE_SIZE, PERK_NODE_SIZE, PERK_NODE_SIZE, PERK_NODE_SIZE);
        }
        
        // Reset color for icon rendering
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Render the school icon on top
        ResourceLocation iconTexture = school.getTexturePath();
        guiGraphics.blit(iconTexture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
    }
    
    private void renderFrameOverlay(GuiGraphics guiGraphics) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // Use BACKGROUND_HEIGHT since it's 194x194
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        // Frame texture dimensions: should match the panel size + border
        int frameWidth = BACKGROUND_HEIGHT + 4; // Add border width
        int frameHeight = BACKGROUND_HEIGHT + 4; // Add border height
        int frameX = panelX - 2; // Center the frame around the panel
        int frameY = panelY - 2; // Center the frame around the panel
        
        // Render the frame texture above other elements (Z > 0)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0.1f); // Above Z=0 where other elements render
        guiGraphics.blit(AFFINITY_FRAME, frameX, frameY, 0, 0, frameWidth, frameHeight, frameWidth, frameHeight);
        guiGraphics.pose().popPose();
    }
    
    private void renderSchoolInfo(GuiGraphics guiGraphics, int centerX, int centerY) {
        var affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData == null) return;
        
        int totalPoints = affinityData.getSchoolPoints(hoveredSchool);
        int maxPoints = PerkTreeManager.getMaxPointsForSchool(hoveredSchool);
        
        // Calculate progress towards next point
        float currentPercentage = affinityData.getSchoolPercentage(hoveredSchool);
        
        int progressPercent = 0;
        if (maxPoints > 0) {
            // If we have all points, show 100%
            if (totalPoints >= maxPoints) {
                progressPercent = 100;
            } else {
                // Use the same logic as the natural progression system
                int thresholdInterval = 100 / maxPoints; // Integer division like in addSchoolProgress
                
                // Calculate which point we're working towards (next point after current points)
                int nextPointThreshold = totalPoints; // We're working towards the next point
                int nextPointStart = nextPointThreshold * thresholdInterval;
                int nextPointEnd = (nextPointThreshold + 1) * thresholdInterval;
                
                // Calculate progress within the next point range
                if (nextPointEnd > nextPointStart && currentPercentage >= nextPointStart) {
                    float progressInNextPoint = (currentPercentage - nextPointStart) / (float)(nextPointEnd - nextPointStart);
                    progressPercent = (int) Math.floor(progressInNextPoint * 100);
                } else if (currentPercentage < nextPointStart) {
                    // If we haven't reached the next point threshold yet, show 0%
                    progressPercent = 0;
                }
            }
        }
        
        String schoolName = hoveredSchool.getTextComponent().getString();
        String progressText = progressPercent + "%";
        String perksText = totalPoints + "/" + maxPoints;
        
        int startY = centerY - (3 * this.font.lineHeight) / 2;
        
        // School name
        int nameWidth = this.font.width(schoolName);
        guiGraphics.drawString(this.font, schoolName, centerX - nameWidth / 2, startY, 0xFFFFFF, false);
        
        // Progress percentage
        int progressWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText, centerX - progressWidth / 2, startY + this.font.lineHeight, 0xFFFFFF, false);
        
        // Perks count
        int perksWidth = this.font.width(perksText);
        guiGraphics.drawString(this.font, perksText, centerX - perksWidth / 2, startY + (2 * this.font.lineHeight), 0xFFFFFF, false);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left click
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            final int iconRadius = 62;
            final int numSchools = schools.size();

            for (int i = 0; i < numSchools; i++) {
                SpellSchool school = schools.get(i);
                double angleDeg = -45 + (i * 360.0 / numSchools);
                double angleRad = Math.toRadians(angleDeg);

                // Icon position
                int iconX = (int) (centerX + iconRadius * Math.cos(angleRad)) - ICON_SIZE / 2;
                int iconY = (int) ((centerY + iconRadius * Math.sin(angleRad)) - ICON_SIZE / 2) + 1;

                // Calculate perk node bounds for click detection
                int nodeX = iconX - (PERK_NODE_SIZE - ICON_SIZE) / 2;
                int nodeY = iconY - (PERK_NODE_SIZE - ICON_SIZE) / 2;

                if (mouseX >= nodeX && mouseX < nodeX + PERK_NODE_SIZE &&
                    mouseY >= nodeY && mouseY < nodeY + PERK_NODE_SIZE) {
                    // Open PerkTreeScreen with the selected school
                    this.minecraft.setScreen(new PerkTreeScreen(player, school, this));
                    return true;
                }
            }
        }
        
        // Check if back button was clicked
        if (showBackButton && handleBackButtonClick((int) mouseX, (int) mouseY)) {
            return true;
        }
        
        // Check if help button was clicked
        if (handleHelpButtonClick((int) mouseX, (int) mouseY)) {
            return true;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    private void renderBackButtonIcon(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonWidth = DocAssets.ARROW_BACK.width();
        int buttonHeight = DocAssets.ARROW_BACK.height();
        int buttonX = panelX + 10; // Position relative to panel
        int buttonY = panelY + 10; // Position relative to panel
        
        // Determine which texture to use based on hover state
        boolean isHovered = mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
                           mouseY >= buttonY && mouseY < buttonY + buttonHeight;
        
        var backIcon = isHovered ? DocAssets.ARROW_BACK_HOVER : DocAssets.ARROW_BACK;
        
        // Render the back arrow icon above the frame (Z > 0)
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0.15f); // Above frame
        guiGraphics.blit(
            backIcon.location(), 
            buttonX, buttonY, 
            backIcon.u(), backIcon.v(), 
            buttonWidth, buttonHeight, 
            backIcon.width(), backIcon.height()
        );
        guiGraphics.pose().popPose();
    }
    
    private boolean handleBackButtonClick(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonWidth = DocAssets.ARROW_BACK.width();
        int buttonHeight = DocAssets.ARROW_BACK.height();
        int buttonX = panelX + 10;
        int buttonY = panelY + 10;
        
        if (mouseX >= buttonX && mouseX < buttonX + buttonWidth && 
            mouseY >= buttonY && mouseY < buttonY + buttonHeight) {
            if (parentScreen != null) {
                this.minecraft.setScreen(parentScreen);
            } else {
                onClose();
            }
            return true;
        }
        
        return false;
    }
    
    private void renderHelpButton(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonSize = 16;
        int buttonX = panelX + panelWidth - buttonSize - 10; // Top right corner
        int buttonY = panelY + 10; // Top right corner
        
        // Determine if button is hovered
        helpButtonHovered = mouseX >= buttonX && mouseX < buttonX + buttonSize && 
                           mouseY >= buttonY && mouseY < buttonY + buttonSize;
        
        // Debug output
        if (helpButtonHovered) {
            System.out.println("Help button hovered at: " + mouseX + ", " + mouseY);
        }
        
        // Render only the "?" symbol with no background
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0.1f); // Above other elements
        int textColor = helpButtonHovered ? 0xFFFFFFFF : 0xFFCCCCCC; // White when hovered, light grey when not
        guiGraphics.drawCenteredString(font, "?", buttonX + buttonSize / 2, buttonY + 4, textColor);
        guiGraphics.pose().popPose();
    }
    
    private void renderHelpTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Create tooltip text explaining the progression system
        List<Component> tooltipLines = List.of(
            Component.literal("§6Affinity Progression"),
            Component.literal(""),
            Component.literal("§7Casting spells builds affinity"),
            Component.literal("§7with their respective schools."),
            Component.literal(""),
            Component.literal("§7Higher affinity unlocks powerful"),
            Component.literal("§7perks and abilities related to"),
            Component.literal("§7that school's magic."),
            Component.literal(""),
            Component.literal("§7As you progress, you'll gain"),
            Component.literal("§7increased spell power and"),
            Component.literal("§7resistance for that school,"),
            Component.literal("§7plus unique passive abilities."),
            Component.literal(""),
            Component.literal("§7Click on a school to view"),
            Component.literal("§7detailed perks and abilities.")
        );
        
        // Convert Components to FormattedCharSequence for tooltip rendering
        List<net.minecraft.util.FormattedCharSequence> formattedLines = tooltipLines.stream()
            .map(component -> component.getVisualOrderText())
            .toList();
        
        // Render tooltip with higher Z-order
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000); // High Z-order to ensure visibility
        guiGraphics.renderTooltip(font, formattedLines, mouseX + 10, mouseY - 10);
        guiGraphics.pose().popPose();
    }
    
    private boolean handleHelpButtonClick(int mouseX, int mouseY) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelWidth = BACKGROUND_HEIGHT; // 194x194 panel
        int panelHeight = BACKGROUND_HEIGHT;
        int panelX = centerX - panelWidth / 2;
        int panelY = centerY - panelHeight / 2;
        
        int buttonSize = 16;
        int buttonX = panelX + panelWidth - buttonSize - 10; // Top right corner
        int buttonY = panelY + 10; // Top right corner
        
        if (mouseX >= buttonX && mouseX < buttonX + buttonSize && 
            mouseY >= buttonY && mouseY < buttonY + buttonSize) {
            // Show help tooltip - this will be handled by the render method
            return true;
        }
        
        return false;
    }

}
