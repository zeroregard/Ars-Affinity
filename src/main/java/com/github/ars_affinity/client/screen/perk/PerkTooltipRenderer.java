package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkNode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PerkTooltipRenderer {
    private final Player player;
    private final Map<String, PerkAllocation> allocatedPerks;
    
    public PerkTooltipRenderer(Player player, Map<String, PerkAllocation> allocatedPerks) {
        this.player = player;
        this.allocatedPerks = allocatedPerks;
    }
    
    public void renderNodeTooltip(GuiGraphics guiGraphics, Font font, PerkNode node, PerkAllocation allocation, int mouseX, int mouseY) {
        List<Component> tooltip = new ArrayList<>();
        
        String displayName = formatPerkName(node.getId());
        tooltip.add(Component.literal(displayName).withStyle(style -> style.withColor(0xFFFFFF)));
        
        tooltip.add(Component.literal("Cost: " + node.getPointCost() + " points").withStyle(style -> style.withColor(0xAAAAAA)));
        
        if (node.getTier() > 1) {
            tooltip.add(Component.literal("Tier: " + node.getTier()).withStyle(style -> style.withColor(0xAAAAAA)));
        }
        
        if (allocation != null) {
            tooltip.add(Component.literal("✓ Allocated").withStyle(style -> style.withColor(0x00FF00)));
        } else if (PerkAllocationManager.canAllocate(player, node.getId())) {
            tooltip.add(Component.literal("Click to allocate").withStyle(style -> style.withColor(0x0088FF)));
        } else {
            tooltip.add(Component.literal("Prerequisites not met").withStyle(style -> style.withColor(0xFF6666)));
        }
        
        if (!node.getPrerequisites().isEmpty()) {
            tooltip.add(Component.literal(""));
            tooltip.add(Component.literal("Prerequisites:").withStyle(style -> style.withColor(0xAAAAAA)));
            for (String prereq : node.getPrerequisites()) {
                boolean prereqMet = allocatedPerks.containsKey(prereq);
                String prereqName = formatPerkName(prereq);
                Component prereqText = Component.literal("  " + (prereqMet ? "✓ " : "✗ ") + prereqName)
                    .withStyle(style -> style.withColor(prereqMet ? 0x00FF00 : 0xFF6666));
                tooltip.add(prereqText);
            }
        }
        
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
}
