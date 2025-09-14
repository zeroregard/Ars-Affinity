package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkDescriptionHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.perk.PerkAllocation;
import com.github.ars_affinity.perk.PerkAllocationManager;
import com.github.ars_affinity.perk.PerkCategory;
import com.github.ars_affinity.perk.PerkNode;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
            tooltip.add(Component.literal("✓ Allocated").withStyle(Style.EMPTY.withColor(0x00FF00)));
        } else if (PerkAllocationManager.canAllocate(player, node.getId())) {
            tooltip.add(Component.literal("Click to allocate").withStyle(Style.EMPTY.withColor(0x0088FF)));
        } else {
            tooltip.add(Component.literal("Prerequisites not met").withStyle(Style.EMPTY.withColor(0xFF6666)));
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
                return new AffinityPerk.LichFeastPerk(perkType, node.getAmount(), node.getTime(), isBuff);
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
}
