package com.github.ars_affinity.client.screen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkDescriptionHelper;
import com.github.ars_affinity.perk.AffinityPerkManager;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

    public AffinityScreen(Player player) {
        super(Component.translatable("ars_affinity.screen.affinity.title"));
        this.player = player;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

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

    private void renderBackgroundPanel(GuiGraphics guiGraphics, int centerX, int centerY) {
        int panelX = centerX - BACKGROUND_WIDTH / 2;
        int panelY = centerY - BACKGROUND_HEIGHT / 2;
        guiGraphics.blit(BACKGROUND_TEXTURE, panelX, panelY, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
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
        
        // Current tier
        int tier = getSchoolTier(player, school);
        if (tier > 0) {
            tooltip.add(Component.literal("Tier " + tier));
            tooltip.add(Component.literal("")); // Empty line
            
            // Current tier perks only (not all perks up to this tier)
            List<AffinityPerk> perks = AffinityPerkManager.getPerksForCurrentLevel(school, tier);
            if (!perks.isEmpty()) {
                tooltip.add(Component.literal("Current Perks:"));
                for (AffinityPerk perk : perks) {
                    String prefix = AffinityPerkDescriptionHelper.getPerkPrefix(perk);
                    MutableComponent description = AffinityPerkDescriptionHelper.getPerkDescription(perk);
                    
                    // Color the perk based on whether it's a buff or debuff
                    int color = perk.isBuff ? 0x55FF55 : 0xFF5555; // Green for buff, red for debuff
                    Component perkComponent = Component.literal(prefix).withStyle(style -> style.withColor(color))
                            .append(description.withStyle(style -> style.withColor(color)));
                    
                    tooltip.add(perkComponent);
                }
            }
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

    // ---- Helpers you must define elsewhere ----

    private int getSchoolTier(Player player, SpellSchool school) {
        // Return 1, 2, or 3 depending on player's progression in this school
        return SchoolAffinityProgressHelper.getTier(player, school); // Implement this
    }
}
