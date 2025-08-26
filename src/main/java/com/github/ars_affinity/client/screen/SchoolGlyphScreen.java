package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class SchoolGlyphScreen extends Screen {

    private static final int BACKGROUND_WIDTH = 194;
    private static final int BACKGROUND_HEIGHT = 194;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_WIDTH = 80;
    private static final int ICON_SIZE = 16;

    private static final ResourceLocation BACKGROUND_TEXTURE = ArsAffinity.prefix("textures/gui/affinity_bg.png");

    private final Player player;
    private final Screen previousScreen;
    private final SpellSchool selectedSchool;
    
    private final List<SpellSchool> schools = List.of(
            SpellSchools.ELEMENTAL_EARTH,
            SpellSchools.MANIPULATION,
            SpellSchools.ELEMENTAL_FIRE,
            SpellSchools.NECROMANCY,
            SpellSchools.ELEMENTAL_AIR,
            SpellSchools.CONJURATION,
            SpellSchools.ELEMENTAL_WATER,
            SpellSchools.ABJURATION
    );

    public SchoolGlyphScreen(Player player, Screen previousScreen, SpellSchool selectedSchool) {
        super(Component.translatable("ars_affinity.screen.school_glyph.title"));
        this.player = player;
        this.previousScreen = previousScreen;
        this.selectedSchool = selectedSchool;
    }

    @Override
    protected void init() {
        super.init();
        
        // Add back button
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.back"),
                button -> this.minecraft.setScreen(this.previousScreen)
        ).pos(10, 10).size(60, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        renderBackgroundPanel(guiGraphics, centerX, centerY);
        renderSchoolTabs(guiGraphics, centerX, centerY, mouseX, mouseY);
        
        // Render title
        guiGraphics.drawString(
                this.font,
                this.title,
                centerX - this.font.width(this.title) / 2,
                centerY - BACKGROUND_HEIGHT / 2 + 18,
                0x7e7e7e,
                false
        );
        
        // Render selected school name
        Component schoolName = selectedSchool.getTextComponent();
        guiGraphics.drawString(
                this.font,
                schoolName,
                centerX - this.font.width(schoolName) / 2,
                centerY - BACKGROUND_HEIGHT / 2 + 40,
                0xFFFFFF,
                false
        );
    }

    private void renderBackgroundPanel(GuiGraphics guiGraphics, int centerX, int centerY) {
        int panelX = centerX - BACKGROUND_WIDTH / 2;
        int panelY = centerY - BACKGROUND_HEIGHT / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    private void renderSchoolTabs(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {
        int startX = centerX - (schools.size() * TAB_WIDTH) / 2;
        int startY = centerY - BACKGROUND_HEIGHT / 2 - TAB_HEIGHT - 10;

        for (int i = 0; i < schools.size(); i++) {
            SpellSchool school = schools.get(i);
            int tabX = startX + i * TAB_WIDTH;
            int tabY = startY;
            
            boolean isSelected = school.equals(selectedSchool);
            boolean isHovered = mouseX >= tabX && mouseX < tabX + TAB_WIDTH && 
                               mouseY >= tabY && mouseY < tabY + TAB_HEIGHT;
            
            // Render tab background
            int tabColor = isSelected ? 0x4CAF50 : (isHovered ? 0x666666 : 0x444444);
            guiGraphics.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, tabColor);
            
            // Render tab border
            int borderColor = isSelected ? 0x8BC34A : 0x666666;
            guiGraphics.fill(tabX, tabY, tabX + TAB_WIDTH, tabY + 1, borderColor); // Top
            guiGraphics.fill(tabX, tabY, tabX + 1, tabY + TAB_HEIGHT, borderColor); // Left
            guiGraphics.fill(tabX + TAB_WIDTH - 1, tabY, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, borderColor); // Right
            guiGraphics.fill(tabX, tabY + TAB_HEIGHT - 1, tabX + TAB_WIDTH, tabY + TAB_HEIGHT, borderColor); // Bottom
            
            // Render school icon
            ResourceLocation iconTexture = school.getTexturePath();
            int iconX = tabX + (TAB_WIDTH - ICON_SIZE) / 2;
            int iconY = tabY + (TAB_HEIGHT - ICON_SIZE) / 2;
            guiGraphics.blit(iconTexture, iconX, iconY, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            
            // Handle tab clicks
            if (isHovered && this.minecraft != null) {
                if (this.minecraft.mouseHandler.isLeftPressed()) {
                    this.minecraft.setScreen(new SchoolGlyphScreen(player, previousScreen, school));
                }
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}