package com.github.ars_affinity.client.screen;

import com.hollingsworth.arsnouveau.api.documentation.DocClientUtils;
import com.hollingsworth.arsnouveau.api.documentation.DocAssets;
import com.hollingsworth.arsnouveau.api.documentation.entry.DocEntry;
import com.hollingsworth.arsnouveau.api.registry.DocumentationRegistry;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import com.hollingsworth.arsnouveau.client.gui.documentation.BaseDocScreen;
import com.hollingsworth.arsnouveau.client.gui.documentation.DocEntryButton;
import com.hollingsworth.arsnouveau.client.gui.documentation.PageHolderScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SchoolGlyphScreen extends BaseDocScreen {
    
    List<DocEntry> entries;
    List<DocEntryButton> buttons = new ArrayList<>();
    SpellSchool selectedSchool;
    private final Screen previousScreen; // TODO
    private final List<SpellSchool> schools;

    public SchoolGlyphScreen(Screen previousScreen) {
        super();
        this.previousScreen = previousScreen;
        this.schools = List.of(
                SpellSchools.ELEMENTAL_FIRE,
                SpellSchools.ELEMENTAL_WATER,
                SpellSchools.ELEMENTAL_EARTH,
                SpellSchools.ELEMENTAL_AIR,
                SpellSchools.ABJURATION,
                SpellSchools.NECROMANCY,
                SpellSchools.CONJURATION,
                SpellSchools.MANIPULATION
        );
        this.selectedSchool = schools.get(0);
        updateGlyphsForSchool();
    }

    private void updateGlyphsForSchool() {
        var entries = new ArrayList<>(GlyphRegistry.getSpellpartMap().values().stream()
                .filter(part -> part.spellSchools.contains(selectedSchool))
                .map(part -> DocumentationRegistry.getEntry(part.getRegistryName()))
                .filter(entry -> entry != null)
                .sorted((a, b) -> {
                    if (a.renderStack().getItem() instanceof com.hollingsworth.arsnouveau.common.items.Glyph glyph1 && 
                        b.renderStack().getItem() instanceof com.hollingsworth.arsnouveau.common.items.Glyph glyph2) {
                        int tierCompare = Integer.compare(glyph1.spellPart.getConfigTier().value, glyph2.spellPart.getConfigTier().value);
                        if (tierCompare != 0) return tierCompare;
                        return a.entryTitle().getString().compareTo(b.entryTitle().getString());
                    }
                    return a.entryTitle().getString().compareTo(b.entryTitle().getString());
                })
                .collect(Collectors.toList()));
        this.entries = new ArrayList<>(entries);
        this.arrowIndex = 0;
        if (this.entries.size() > 17) {
            maxArrowIndex = 1 + (this.entries.size() - 17) / 18;
        } else {
            maxArrowIndex = 0;
        }
    }

    @Override
    public void init() {
        super.init();
        initButtons();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        
        renderSchoolTabs(graphics, centerX, centerY, mouseX, mouseY);
        
        if (arrowIndex == 0) {
            DocClientUtils.drawHeader(selectedSchool.getTextComponent(), graphics, screenLeft + LEFT_PAGE_OFFSET, screenTop + PAGE_TOP_OFFSET, ONE_PAGE_WIDTH, mouseX, mouseY, partialTicks);
        }
    }

    private void renderSchoolTabs(GuiGraphics guiGraphics, int centerX, int centerY, int mouseX, int mouseY) {

        int startX = centerX + 144;
        int startY = centerY - 86;
        for (int i = 0; i < schools.size(); i++) {
            SpellSchool school = schools.get(i);
            int tabX = startX;
            int tabY = startY + (i * 22);
            
            boolean isSelected = school.equals(selectedSchool);
            boolean isHovered = mouseX >= tabX && mouseX < tabX + 18 && 
                               mouseY >= tabY && mouseY < tabY + 13;

            if (isSelected) {
                guiGraphics.blit(com.hollingsworth.arsnouveau.api.documentation.DocAssets.SPELL_TAB_ICON_SELECTED.location(), tabX, tabY, 0, 0, 18, 13, 18, 13);
            } else {
                guiGraphics.blit(com.hollingsworth.arsnouveau.api.documentation.DocAssets.SPELL_TAB_ICON.location(), tabX, tabY, 0, 0, 18, 13, 18, 13);
            }
            
 
            ResourceLocation iconTexture = school.getTexturePath();
            int iconSize = 16;
            int iconX = tabX;
            int iconY = tabY - 2;
            
            guiGraphics.blit(iconTexture, iconX, iconY, 0, 0, iconSize, iconSize, iconSize, iconSize);
            
            if (isHovered && !isSelected) {
                Component schoolName = school.getTextComponent();
                guiGraphics.renderTooltip(font, schoolName, mouseX, mouseY);
            }
        }
    }
    
    @Override
    public void onArrowIndexChange() {
        super.onArrowIndexChange();
        initButtons();
    }

    public void initButtons() {
        for (DocEntryButton button : buttons) {
            removeWidget(button);
        }
        buttons.clear();
        int offset = 17;
        if (arrowIndex == 0) {
            getLeftPageButtons(0, 8);
            getRightPageButtons(8, offset);
        } else {
            int offsetIndex = arrowIndex * 18 - 1;
            getLeftPageButtons(offsetIndex, offsetIndex + 9);
            getRightPageButtons(offsetIndex + 9, offsetIndex + 18);
        }
    }

    public void getLeftPageButtons(int from, int to) {
        List<DocEntry> sliced = entries.subList(from, Math.min(to, entries.size()));
        boolean offset = to - from == 8;
        for (int i = 0; i < sliced.size(); i++) {
            DocEntry entry = sliced.get(i);
            var button = new DocEntryButton(screenLeft + LEFT_PAGE_OFFSET, screenTop + PAGE_TOP_OFFSET + 3 + (16 * i) + (offset ? 16 : 0), entry, (b) -> {
                transition(new PageHolderScreen(entry));
            });
            addRenderableWidget(button);
            buttons.add(button);
        }
    }

    public void getRightPageButtons(int from, int to) {
        if (from > entries.size()) {
            return;
        }
        List<DocEntry> sliced = entries.subList(from, Math.min(to, entries.size()));
        for (int i = 0; i < sliced.size(); i++) {
            DocEntry entry = sliced.get(i);
            var button = new DocEntryButton(screenLeft + RIGHT_PAGE_OFFSET, screenTop + PAGE_TOP_OFFSET + 3 + 16 * i, entry, (b) -> {
                transition(new PageHolderScreen(entry));
            });
            addRenderableWidget(button);
            buttons.add(button);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int centerX = this.width / 2;
            int centerY = this.height / 2;
            // Use the exact same positioning as renderSchoolTabs
            int startX = centerX + 144; // Right side positioning
            int startY = centerY - 86; // Start from top, similar to Ars Nouveau's spacing

            for (int i = 0; i < schools.size(); i++) {
                SpellSchool school = schools.get(i);
                int tabX = startX;
                int tabY = startY + (i * 22); // 22 pixel spacing between tabs, like Ars Nouveau
                
                if (mouseX >= tabX && mouseX < tabX + 18 && 
                    mouseY >= tabY && mouseY < tabY + 13) {
                    selectedSchool = school;
                    updateGlyphsForSchool();
                    initButtons();
                    return true;
                }
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
}