package com.github.ars_affinity.common.item;

import com.github.ars_affinity.capability.PlayerAffinityDataHelper;
import com.github.ars_affinity.util.ChatMessageHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tablet of Amnesia - A consumable item that resets affinity progress in a specific school.
 * Must be augmented with an essence corresponding to the school to reset.
 */
public class TabletOfAmnesia extends Item {
    
    public TabletOfAmnesia() {
        super(new Item.Properties()
            .stacksTo(16)
            .food(new net.minecraft.world.food.FoodProperties.Builder()
                .nutrition(0)
                .saturationModifier(0.0f)
                .alwaysEdible()
                .build()));
    }
    
    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            return InteractionResultHolder.consume(stack);
        }
        
        // Determine which school this tablet is for based on the essence used
        SpellSchool targetSchool = getSchoolFromTablet(stack);
        if (targetSchool == null) {
            player.sendSystemMessage(Component.literal("§cThis tablet has no essence attached. Use an essence to determine which school to reset."));
            return InteractionResultHolder.fail(stack);
        }
        
        // Get player's affinity data
        var data = PlayerAffinityDataHelper.getPlayerAffinityData(player);
        if (data == null) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                ChatMessageHelper.sendSchoolResetFailureMessage(serverPlayer, targetSchool, "Failed to get affinity data for player");
            }
            return InteractionResultHolder.fail(stack);
        }
        
        // Check if player has any points in this school
        int currentPoints = data.getSchoolPoints(targetSchool);
        if (currentPoints == 0) {
            player.sendSystemMessage(Component.literal("§eYou have no affinity progress in " + formatSchoolName(targetSchool) + " to reset."));
            return InteractionResultHolder.fail(stack);
        }
        
        // Reset the school
        data.setSchoolPoints(targetSchool, 0);
        
        // Send success message
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            ChatMessageHelper.sendSchoolResetMessage(serverPlayer, targetSchool, currentPoints);
        }
        
        // Consume the tablet
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        
        return InteractionResultHolder.consume(stack);
    }
    
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        
        SpellSchool school = getSchoolFromTablet(stack);
        if (school != null) {
            String schoolName = formatSchoolName(school);
            tooltip.add(Component.literal("§7Resets " + schoolName + " affinity to 0 points"));
            tooltip.add(Component.literal("§8Right-click to consume"));
        } else {
            tooltip.add(Component.literal("§7Attach an essence to determine which school to reset"));
            tooltip.add(Component.literal("§8Right-click to consume"));
        }
        
        if (flag.hasShiftDown()) {
            tooltip.add(Component.literal("§7This tablet allows you to forget your affinity progress"));
            tooltip.add(Component.literal("§7in a specific school, making it easier to gain"));
            tooltip.add(Component.literal("§7points in other schools."));
        } else {
            tooltip.add(Component.literal("§8Hold §7Shift§8 for more information"));
        }
    }
    
    /**
     * Determine which school this tablet is for based on the essence used.
     * This would be set when the tablet is crafted with an essence.
     */
    private SpellSchool getSchoolFromTablet(ItemStack stack) {
        // For now, we'll use NBT data to store the school
        // In a full implementation, this would be set during crafting
        var customData = stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
        if (customData != null && customData.getUnsafe().contains("school")) {
            String schoolId = customData.getUnsafe().getString("school");
            return parseSchoolId(schoolId);
        }
        return null;
    }
    
    /**
     * Parse a school ID string to a SpellSchool.
     */
    private SpellSchool parseSchoolId(String schoolId) {
        return switch (schoolId.toLowerCase()) {
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            case "abjuration" -> SpellSchools.ABJURATION;
            case "conjuration" -> SpellSchools.CONJURATION;
            case "necromancy", "anima" -> SpellSchools.NECROMANCY;
            case "manipulation" -> SpellSchools.MANIPULATION;
            default -> null;
        };
    }
    
    /**
     * Format a school name for display.
     */
    private String formatSchoolName(SpellSchool school) {
        return school.getId().toString()
            .replace("ars_nouveau:", "")
            .replace("_", " ")
            .toLowerCase();
    }
    
    /**
     * Create a tablet for a specific school.
     * This would be used during crafting.
     */
    public static ItemStack createTabletForSchool(SpellSchool school) {
        ItemStack stack = new ItemStack(com.github.ars_affinity.registry.ModItems.TABLET_OF_AMNESIA.get());
        var tag = new net.minecraft.nbt.CompoundTag();
        tag.putString("school", school.getId().toString().replace("ars_nouveau:", ""));
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, CustomData.of(tag));
        return stack;
    }
}
