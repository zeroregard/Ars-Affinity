package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.client.screen.perk.PerkTreeScreen;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class AffinityScreen extends Screen {

    private static final int BACKGROUND_WIDTH = 194;
    private static final int BACKGROUND_HEIGHT = 194;
    private static final int ICON_SIZE = 16;

    private static final ResourceLocation BACKGROUND_TEXTURE = ArsAffinity.prefix("textures/gui/affinity_bg.png");

    private final Player player;
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

    public AffinityScreen(Player player) {
        super(Component.translatable("ars_affinity.screen.affinity.title"));
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Don't call super.render() to avoid pause screen blur
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        renderBackgroundPanel(guiGraphics, centerX, centerY);

        guiGraphics.drawString(
                this.font,
                this.title,
                centerX - this.font.width(this.title) / 2,
                centerY - BACKGROUND_HEIGHT / 2 + 18,
                0x7e7e7e,
                false
        );

        renderSchoolOctagon(guiGraphics, centerX, centerY, mouseX, mouseY);
        
        if (hoveredSchool != null) {
            renderSchoolInfo(guiGraphics, centerX, centerY);
        }
    }
    
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Override to prevent blur effect
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        renderBackgroundPanel(guiGraphics, centerX, centerY);
    }

    private void renderBackgroundPanel(GuiGraphics guiGraphics, int centerX, int centerY) {
        int panelX = centerX - BACKGROUND_WIDTH / 2;
        int panelY = centerY - BACKGROUND_HEIGHT / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private void renderSchoolOctagon(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        final int iconRadius = 56;
        final int numSchools = schools.size();
        
        hoveredSchool = null;

        for (int i = 0; i < numSchools; i++) {
            SpellSchool school = schools.get(i);
            double angleDeg = -45 + (i * 360.0 / numSchools);
            double angleRad = Math.toRadians(angleDeg);

            // Icon position
            int iconX = (int) (centerX + iconRadius * Math.cos(angleRad)) - ICON_SIZE / 2;
            int iconY = (int) ((centerY + iconRadius * Math.sin(angleRad)) - ICON_SIZE / 2) - 3;
            renderSchoolIcon(guiGraphics, school, iconX, iconY);

            if (mouseX >= iconX && mouseX < iconX + ICON_SIZE &&
                    mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                hoveredSchool = school;
            }
        }
    }


    private void renderSchoolIcon(GuiGraphics guiGraphics, SpellSchool school, int x, int y) {
        ResourceLocation iconTexture = school.getTexturePath();
        guiGraphics.blit(iconTexture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
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
        guiGraphics.drawString(this.font, schoolName, centerX - nameWidth / 2, startY, 0x333333, false);
        
        // Progress percentage
        int progressWidth = this.font.width(progressText);
        guiGraphics.drawString(this.font, progressText, centerX - progressWidth / 2, startY + this.font.lineHeight, 0x555555, false);
        
        // Perks count
        int perksWidth = this.font.width(perksText);
        guiGraphics.drawString(this.font, perksText, centerX - perksWidth / 2, startY + (2 * this.font.lineHeight), 0x555555, false);
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
            final int iconRadius = 56;
            final int numSchools = schools.size();

            for (int i = 0; i < numSchools; i++) {
                SpellSchool school = schools.get(i);
                double angleDeg = -45 + (i * 360.0 / numSchools);
                double angleRad = Math.toRadians(angleDeg);

                // Icon position
                int iconX = (int) (centerX + iconRadius * Math.cos(angleRad)) - ICON_SIZE / 2;
                int iconY = (int) ((centerY + iconRadius * Math.sin(angleRad)) - ICON_SIZE / 2) - 3;

                if (mouseX >= iconX && mouseX < iconX + ICON_SIZE &&
                    mouseY >= iconY && mouseY < iconY + ICON_SIZE) {
                    // Open PerkTreeScreen with the selected school
                    this.minecraft.setScreen(new PerkTreeScreen(player, school, this));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

}
