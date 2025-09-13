package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.capability.PlayerAffinityData;
import com.github.ars_affinity.perk.PerkAllocation;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Map;

public class PerkInfoPanel {
    private final SpellSchool school;
    private final PlayerAffinityData affinityData;
    private final Map<String, PerkAllocation> allocatedPerks;
    
    public PerkInfoPanel(SpellSchool school, PlayerAffinityData affinityData, Map<String, PerkAllocation> allocatedPerks) {
        this.school = school;
        this.affinityData = affinityData;
        this.allocatedPerks = allocatedPerks;
    }
    
    public void render(GuiGraphics guiGraphics, Font font, int width, int height) {
        int panelX = 10;
        int panelY = height - 80;
        int panelWidth = 200;
        int panelHeight = 70;
        
        guiGraphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);
        
        guiGraphics.drawString(font, school.getTextComponent(), panelX + 5, panelY + 5, 0xFFFFFF);
        
        int availablePoints = affinityData.getAvailablePoints(school);
        int totalPoints = affinityData.getSchoolPoints(school);
        guiGraphics.drawString(font, "Available: " + availablePoints, panelX + 5, panelY + 20, 0xFFFFFF);
        guiGraphics.drawString(font, "Total: " + totalPoints, panelX + 5, panelY + 35, 0xFFFFFF);
        
        int allocatedCount = allocatedPerks.size();
        guiGraphics.drawString(font, "Perks: " + allocatedCount, panelX + 5, panelY + 50, 0xFFFFFF);
    }
}
