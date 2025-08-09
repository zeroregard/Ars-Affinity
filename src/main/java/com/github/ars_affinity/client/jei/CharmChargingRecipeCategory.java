package com.github.ars_affinity.client.jei;

import com.github.ars_affinity.common.recipe.AnchorCharmChargingRecipe;
import com.hollingsworth.arsnouveau.client.jei.MultiInputCategory;
import com.hollingsworth.arsnouveau.setup.registry.BlockRegistry;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public class CharmChargingRecipeCategory extends MultiInputCategory<AnchorCharmChargingRecipe> {
    public IDrawable background;
    public IDrawable icon;

    public CharmChargingRecipeCategory(IGuiHelper helper) {
        super(helper, recipe -> {
            var stack = recipe.input().getDefaultInstance();
            return new MultiProvider(stack, List.of(), Ingredient.of(stack));
        });
        background = helper.createBlankDrawable(126, 108);
        icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(BlockRegistry.IMBUEMENT_BLOCK));
    }

    @Override
    public RecipeType<AnchorCharmChargingRecipe> getRecipeType() {
        return ModPlugin.CHARM_CHARGING_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("ars_affinity.charm_charging");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(AnchorCharmChargingRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        Font renderer = Minecraft.getInstance().font;
        guiGraphics.drawString(renderer, Component.translatable("ars_affinity.source_per_charge", recipe.costPerCharge()), 0, 100, 10, false);
    }
}