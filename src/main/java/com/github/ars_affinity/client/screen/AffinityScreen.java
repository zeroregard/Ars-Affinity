package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.client.screen.perk.PerkTreeScreen;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkTreeManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class AffinityScreen extends Screen {

    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 256;
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
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 256, 256);
    }

    private void renderSchoolOctagon(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        final int iconRadius = 60;
        final int numSchools = schools.size();

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
                List<Component> tooltip = createSchoolTooltip(school);
                guiGraphics.renderComponentTooltip(this.font, tooltip, mouseX, mouseY);
            }

            int tier = getSchoolTier(player, school);
            renderProgressionBars(guiGraphics, centerX, centerY, school, tier);
        }
    }

    private List<Component> createSchoolTooltip(SpellSchool school) {
        List<Component> tooltip = new java.util.ArrayList<>();
        
        // School name
        tooltip.add(school.getTextComponent());
        
        // Points info
        var affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData != null) {
            int availablePoints = affinityData.getAvailablePoints(school);
            int totalPoints = affinityData.getSchoolPoints(school);
            int allocatedCount = PerkAllocationManager.getAllocatedPerks(player, school).size();
            
            tooltip.add(Component.literal("Available Points: " + availablePoints));
            tooltip.add(Component.literal("Total Points: " + totalPoints));
            tooltip.add(Component.literal("Allocated Perks: " + allocatedCount));
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Click to open perk tree"));
        }
        
        return tooltip;
    }

    private void renderProgressionBars(GuiGraphics guiGraphics, int centerX, int centerY, SpellSchool school, int tier) {
        int panelX = centerX - BACKGROUND_WIDTH / 2;
        int panelY = centerY - BACKGROUND_HEIGHT / 2;

        String schoolName = school.getId().toLowerCase();
        for (int i = 0; i < tier && i < 3; i++) {
            ResourceLocation tierTexture = ArsAffinity.prefix("textures/gui/tiers/" + schoolName + "_tier" + (i + 1) + ".png");

            RenderSystem.enableBlend();
            guiGraphics.blit(tierTexture, panelX, panelY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        }
    }

    private void renderSchoolIcon(GuiGraphics guiGraphics, SpellSchool school, int x, int y) {
        ResourceLocation iconTexture = school.getTexturePath();
        guiGraphics.blit(iconTexture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
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
            final int iconRadius = 60;
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

    // ---- Helpers you must define elsewhere ----

    private int getSchoolTier(Player player, SpellSchool school) {
        // Calculate tier based on allocated perks
        var affinityData = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (affinityData == null) return 0;
        
        int allocatedCount = PerkAllocationManager.getAllocatedPerks(player, school).size();
        int totalPerks = PerkTreeManager.getTotalPerksForSchool(school);
        
        if (totalPerks == 0) return 0;
        
        // Calculate tier based on percentage of perks allocated
        float percentage = (float) allocatedCount / totalPerks;
        if (percentage >= 0.75f) return 3;
        if (percentage >= 0.5f) return 2;
        if (percentage >= 0.25f) return 1;
        return 0;
    }
}
