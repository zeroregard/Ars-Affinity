package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkDescriptionHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.GlyphPrerequisiteHelper;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkCategory;
import com.github.ars_affinity.perk.PerkNode;
import com.github.ars_affinity.perk.PerkPrerequisiteChecker;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import com.hollingsworth.arsnouveau.client.gui.utils.RenderUtils;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerkTooltipRenderer {
    private final Player player;
    private final Map<String, PerkAllocation> allocatedPerks;
    private int width = 400; // Default screen width
    private int height = 300; // Default screen height
    
    public PerkTooltipRenderer(Player player, Map<String, PerkAllocation> allocatedPerks) {
        this.player = player;
        this.allocatedPerks = allocatedPerks;
    }
    
    public void setScreenDimensions(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
    private AbstractSpellPart getGlyphSpellPart(String glyphId) {
        try {
            if (glyphId == null || glyphId.isEmpty()) {
                return null;
            }
            
            String[] parts = glyphId.split(":", 2);
            ResourceLocation glyphLocation;
            if (parts.length == 2) {
                glyphLocation = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            } else {
                glyphLocation = ResourceLocation.fromNamespaceAndPath("minecraft", glyphId);
            }
            
            return GlyphRegistry.getSpellpartMap().get(glyphLocation);
        } catch (Exception e) {
            ArsAffinity.LOGGER.warn("Error getting glyph SpellPart for {}: {}", glyphId, e.getMessage());
        }
        return null;
    }
    
    public void renderNodeTooltip(GuiGraphics guiGraphics, Font font, PerkNode node, PerkAllocation allocation, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        AbstractSpellPart glyphPart = null;
        
        boolean isUnlocked = allocation != null || PerkAllocationManager.canAllocate(player, node.getId());
        boolean isActiveAbility = node.getPerkType().name().startsWith("ACTIVE_");
        int textColor = isUnlocked ? 0xFFFFFF : 0x888888;
        int descColor = isUnlocked ? 0xDDDDDD : 0x888888;
        
        String displayName = formatPerkName(node.getId());
        Component nameComponent;
        
        if (isActiveAbility) {
            // For active abilities, show with a special indicator in light blue
            nameComponent = Component.literal("✦ " + displayName).withStyle(Style.EMPTY.withColor(0x88CCFF));
        } else {
            nameComponent = Component.literal(displayName).withStyle(Style.EMPTY.withColor(textColor));
        }
        
        tooltip.add(nameComponent);
        
        // Add perk description
        AffinityPerk perk = createAffinityPerkFromNode(node);
        if (perk != null) {
            Component description = AffinityPerkDescriptionHelper.getPerkDescription(perk);
            tooltip.add(description.copy().withStyle(Style.EMPTY.withColor(descColor)));
        }
        
        if (allocation != null) {
            tooltip.add(Component.literal("Unlocked").withStyle(Style.EMPTY.withColor(0x66FF66)));
        } else if (PerkAllocationManager.canAllocate(player, node.getId())) {
            tooltip.add(Component.literal("Click to unlock").withStyle(Style.EMPTY.withColor(0x0088FF)));
        } else {
            // Use the detailed prerequisite checker
            PerkPrerequisiteChecker.PrerequisiteResult result = PerkPrerequisiteChecker.checkPrerequisites(player, node);
            if (!result.canAllocate() && result.hasReasons()) {
                // Check if we need to render glyph texture
                if (node.hasPrerequisiteGlyph()) {
                    glyphPart = getGlyphSpellPart(node.getPrerequisiteGlyph());
                }
                
                // Add the main prerequisite message
                tooltip.add(Component.translatable("ars_affinity.tooltip.prerequisites_not_met").withStyle(Style.EMPTY.withColor(0xFF6666)));
                
                // Add each reason as a separate line
                for (String reason : result.getReasons()) {
                    if (reason.contains("Glyph") && glyphPart != null) {
                        // Special handling for glyph prerequisites
                        String glyphName = GlyphPrerequisiteHelper.getGlyphDisplayName(node.getPrerequisiteGlyph());
                        Component glyphComponent = Component.literal("• Glyph ").withStyle(Style.EMPTY.withColor(0xFF6666))
                            .append(Component.literal("'" + glyphName + "' not unlocked").withStyle(Style.EMPTY.withColor(0xFF6666)));
                        tooltip.add(glyphComponent);
                    } else {
                        tooltip.add(Component.literal("• " + reason).withStyle(Style.EMPTY.withColor(0xFF6666)));
                    }
                }
            } else {
                tooltip.add(Component.literal("Prerequisites not met").withStyle(Style.EMPTY.withColor(0xFF6666)));
            }
        }
        
        // Render the tooltip
        guiGraphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }
    
    public ResourceLocation getPerkIcon(PerkNode node) {
        String perkType = node.getPerkType().name().toLowerCase();
        return ResourceLocation.fromNamespaceAndPath("ars_affinity", "textures/gui/perks/" + perkType + ".png");
    }
    
    private String formatPerkName(String nodeId) {
        String[] parts = nodeId.split("_");
        StringBuilder result = new StringBuilder();
        
        // Skip the first part (school name) and start from the second part
        for (int i = 1; i < parts.length; i++) {
            if (i > 1) result.append(" ");
            
            String part = parts[i];
            if (part.matches("\\d+")) {
                int num = Integer.parseInt(part);
                result.append(toRomanNumeral(num));
            } else {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1));
                }
            }
        }
        
        return result.toString();
    }
    
    private String toRomanNumeral(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(number);
        };
    }
    
    private AffinityPerk createAffinityPerkFromNode(PerkNode node) {
        AffinityPerkType perkType = node.getPerkType();
        boolean isBuff = node.getCategory() != PerkCategory.PASSIVE || 
                        perkType.name().contains("POWER") || 
                        perkType.name().contains("RESISTANCE") ||
                        perkType.name().contains("HEALTH") ||
                        perkType.name().contains("MANA_TAP") ||
                        perkType.name().contains("HEALING_AMPLIFICATION") ||
                        perkType.name().contains("COLD_WALKER") ||
                        perkType.name().contains("DEFLECTION") ||
                        perkType.name().contains("STONE_SKIN") ||
                        perkType.name().contains("HYDRATION") ||
                        perkType.name().contains("GHOST_STEP") ||
                        perkType.name().contains("ROTTING_GUISE") ||
                        perkType.name().contains("LICH_FEAST") ||
                        perkType.name().contains("SUMMON_DEFENSE") ||
                        perkType.name().contains("SUMMONING_POWER");
        
        switch (perkType) {
            case PASSIVE_FIRE_THORNS:
            case PASSIVE_MANA_TAP:
            case PASSIVE_HEALING_AMPLIFICATION:
            case PASSIVE_SOULSPIKE:
            case PASSIVE_SUMMONING_POWER:
            case PASSIVE_ABJURATION_POWER:
            case PASSIVE_AIR_POWER:
            case PASSIVE_EARTH_POWER:
            case PASSIVE_FIRE_POWER:
            case PASSIVE_MANIPULATION_POWER:
            case PASSIVE_ANIMA_POWER:
            case PASSIVE_WATER_POWER:
            case PASSIVE_ABJURATION_RESISTANCE:
            case PASSIVE_CONJURATION_RESISTANCE:
            case PASSIVE_AIR_RESISTANCE:
            case PASSIVE_EARTH_RESISTANCE:
            case PASSIVE_FIRE_RESISTANCE:
            case PASSIVE_MANIPULATION_RESISTANCE:
            case PASSIVE_ANIMA_RESISTANCE:
            case PASSIVE_WATER_RESISTANCE:
            case PASSIVE_COLD_WALKER:
                return new AffinityPerk.AmountBasedPerk(perkType, node.getAmount(), isBuff);
            case PASSIVE_SUMMON_HEALTH:
            case PASSIVE_SUMMON_DEFENSE:
            case PASSIVE_DEFLECTION:
            case PASSIVE_STONE_SKIN:
            case PASSIVE_HYDRATION:
                return new AffinityPerk.DurationBasedPerk(perkType, node.getAmount(), node.getTime(), isBuff);
            case PASSIVE_LICH_FEAST:
                // Lich Feast uses health and hunger values, not amount and time
                return new AffinityPerk.LichFeastPerk(perkType, node.getHealth(), node.getHunger(), isBuff);
            case ACTIVE_ICE_BLAST:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    node.getDamage(), node.getFreezeTime(), node.getRadius(), isBuff);
            case ACTIVE_SWAP_ABILITY:
            case ACTIVE_GROUND_SLAM:
            case ACTIVE_SANCTUARY:
            case ACTIVE_CURSE_FIELD:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    0.0f, 0, 0.0f, isBuff);
            case ACTIVE_AIR_DASH:
            case ACTIVE_FIRE_DASH:
            case ACTIVE_GHOST_STEP:
                return new AffinityPerk.ActiveAbilityPerk(perkType, node.getManaCost(), node.getCooldown(), 
                    0.0f, 0, 0.0f, node.getDashLength(), node.getDashDuration(), isBuff);
            case PASSIVE_GHOST_STEP:
                return new AffinityPerk.GhostStepPerk(perkType, node.getAmount(), node.getTime(), node.getCooldown(), isBuff);
            case PASSIVE_ROTTING_GUISE:
                return new AffinityPerk.SimplePerk(perkType, isBuff);
            default:
                return new AffinityPerk.SimplePerk(perkType, isBuff);
        }
    }
    
    public void renderGlyphPrerequisite(GuiGraphics guiGraphics, Font font, PerkNode node, int nodeX, int nodeY, int nodeSize, int mouseX, int mouseY) {
        if (!node.hasPrerequisiteGlyph()) {
            return;
        }
        
        AbstractSpellPart glyphPart = getGlyphSpellPart(node.getPrerequisiteGlyph());
        if (glyphPart == null) {
            return;
        }
        
        // Check if the glyph is unlocked
        boolean isGlyphUnlocked = GlyphPrerequisiteHelper.hasUnlockedGlyph(player, node.getPrerequisiteGlyph());
        
        // Render glyph texture to the left of the perk node
        int glyphX = nodeX - 12; // 20 pixels to the left
        int glyphY = nodeY + (nodeSize - 16) / 2 - 4;
        
        // Render background square behind the glyph
        int backgroundSize = 14;
        int backgroundX = glyphX + (16 - backgroundSize) / 2; // Center the background
        int backgroundY = glyphY + (16 - backgroundSize) / 2; // Center the background
        guiGraphics.fill(backgroundX, backgroundY, backgroundX + backgroundSize, backgroundY + backgroundSize, 0xFF041536);
        
        // Render with appropriate opacity
        guiGraphics.pose().pushPose();
        if (!isGlyphUnlocked) {
            guiGraphics.pose().translate(0, 0, 0.1); // Slight Z offset for visual separation
        }
        RenderUtils.drawSpellPart(glyphPart, guiGraphics, glyphX, glyphY, 16, !isGlyphUnlocked);
        guiGraphics.pose().popPose();
        
        // Check if mouse is hovering over the glyph texture
        if (mouseX >= glyphX && mouseX < glyphX + 16 && mouseY >= glyphY && mouseY < glyphY + 16) {
            // Show glyph name tooltip with status
            String glyphName = GlyphPrerequisiteHelper.getGlyphDisplayName(node.getPrerequisiteGlyph());
            Component tooltipText;
            
            if (isGlyphUnlocked) {
                // Just show the glyph name if unlocked
                tooltipText = Component.literal(glyphName);
            } else {
                // Show glyph name + "not unlocked yet" in red if not unlocked
                tooltipText = Component.literal(glyphName + " - not unlocked yet")
                    .withStyle(Style.EMPTY.withColor(0xFF6666));
            }
            
            List<Component> glyphTooltip = List.of(tooltipText);
            guiGraphics.renderComponentTooltip(font, glyphTooltip, mouseX, mouseY);
        }
    }
}
