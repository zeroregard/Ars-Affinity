package com.github.ars_affinity.datagen;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.registry.RitualRegistry;
import com.hollingsworth.arsnouveau.common.datagen.ItemTagProvider;
import com.hollingsworth.arsnouveau.common.items.RitualTablet;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.concurrent.CompletableFuture;

public class RecipeDataGen extends RecipeProvider {
    
    public static Ingredient ARCHWOOD_LOG = Ingredient.of(ItemTagProvider.ARCHWOOD_LOG_TAG);
    
    public RecipeDataGen(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }
    
    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        // Create recipe for the ritual tablet using the same method as Ars Nouveau
        RitualTablet ritualTablet = getRitualItem("ritual_amnesia");
        if (ritualTablet != null) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ritualTablet)
                    .requires(ARCHWOOD_LOG)
                    .requires(Items.CLOCK)
                    .requires(Items.ENDER_PEARL)
                    .unlockedBy("has_archwood_log", InventoryChangeTrigger.TriggerInstance.hasItems(Items.CLOCK))
                    .save(recipeOutput, ArsAffinity.prefix("ritual_amnesia"));
        }
    }
    
    public static RitualTablet getRitualItem(String name) {
        return RitualRegistry.getRitualItemMap().get(ArsAffinity.prefix(name));
    }
}
