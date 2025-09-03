package com.github.ars_affinity.datagen;

import com.github.ars_affinity.ArsAffinity;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class RecipeDataGen implements DataProvider {
    
    private final PackOutput packOutput;
    private final ExistingFileHelper existingFileHelper;
    
    public RecipeDataGen(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        this.packOutput = packOutput;
        this.existingFileHelper = existingFileHelper;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(() -> {
            generateAffinityAnchorCharmRecipe(output);
            generateAffinityAnchorCharmChargingRecipe(output);
            generateBrewingRecipes(output);
        });
    }
    
    private void generateAffinityAnchorCharmRecipe(CachedOutput output) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "ars_nouveau:enchanting_apparatus");
        recipe.addProperty("keepNbtOfReagent", false);
        
        // Pedestal items - all school essences
        JsonArray pedestalItems = new JsonArray();
        String[] essences = {
            "ars_nouveau:fire_essence",
            "ars_nouveau:water_essence", 
            "ars_nouveau:earth_essence",
            "ars_nouveau:air_essence",
            "ars_nouveau:abjuration_essence",
            "ars_nouveau:conjuration_essence",
            "ars_nouveau:manipulation_essence",
            "minecraft:wither_skeleton_skull" // TODO: "ars_elemental:anima_essence"
        };
        
        for (String essence : essences) {
            JsonObject item = new JsonObject();
            item.addProperty("item", essence);
            pedestalItems.add(item);
        }
        recipe.add("pedestalItems", pedestalItems);
        
        // Reagent - glass bottle
        JsonObject reagent = new JsonObject();
        reagent.addProperty("item", "minecraft:glass_bottle");
        recipe.add("reagent", reagent);
        
        // Result - affinity anchor charm
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "ars_affinity:affinity_anchor_charm");
        recipe.add("result", result);
        
        recipe.addProperty("sourceCost", 0);
        
        // Save the recipe
        Path recipePath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("recipe")
            .resolve("affinity_anchor_charm.json");
            
        try {
            DataProvider.saveStable(output, recipe, recipePath);
            ArsAffinity.LOGGER.info("Generated affinity anchor charm recipe: {}", recipePath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save affinity anchor charm recipe: {}", e.getMessage());
        }
    }
    
    private void generateAffinityAnchorCharmChargingRecipe(CachedOutput output) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "ars_nouveau:enchanting_apparatus");
        recipe.addProperty("keepNbtOfReagent", true);
        
        // Pedestal items - source gems
        JsonArray pedestalItems = new JsonArray();
        for (int i = 0; i < 8; i++) {
            JsonObject item = new JsonObject();
            item.addProperty("item", "ars_nouveau:source_gem");
            pedestalItems.add(item);
        }
        recipe.add("pedestalItems", pedestalItems);
        
        // Reagent - affinity anchor charm
        JsonObject reagent = new JsonObject();
        reagent.addProperty("item", "ars_affinity:affinity_anchor_charm");
        recipe.add("reagent", reagent);
        
        // Result - charged affinity anchor charm
        JsonObject result = new JsonObject();
        result.addProperty("count", 1);
        result.addProperty("id", "ars_affinity:affinity_anchor_charm");
        recipe.add("result", result);
        
        recipe.addProperty("sourceCost", 0);
        
        // Save the recipe
        Path recipePath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("recipe")
            .resolve("charm_charging")
            .resolve("affinity_anchor_charm.json");
            
        try {
            DataProvider.saveStable(output, recipe, recipePath);
            ArsAffinity.LOGGER.info("Generated affinity anchor charm charging recipe: {}", recipePath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save affinity anchor charm charging recipe: {}", e.getMessage());
        }
    }
    
    private void generateBrewingRecipes(CachedOutput output) {
        // Generate brewing recipes for each school that has an essence
        String[][] schoolEssences = {
            {"fire", "ars_nouveau:fire_essence"},
            {"water", "ars_nouveau:water_essence"},
            {"earth", "ars_nouveau:earth_essence"},
            {"air", "ars_nouveau:air_essence"},
            {"abjuration", "ars_nouveau:abjuration_essence"},
            {"conjuration", "ars_nouveau:conjuration_essence"},
            {"manipulation", "ars_nouveau:manipulation_essence"},
            {"anima", "minecraft:wither_skeleton_skull"} // TODO: "ars_elemental:anima_essence"
        };
        
        for (String[] schoolEssence : schoolEssences) {
            String schoolName = schoolEssence[0];
            String essenceItem = schoolEssence[1];
            generateBrewingRecipe(output, schoolName, essenceItem);
        }
    }
    
    private void generateBrewingRecipe(CachedOutput output, String schoolName, String essenceItem) {
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:brewing");
        
        // Input potion (awkward potion) - using item ID directly
        JsonObject input = new JsonObject();
        input.addProperty("item", "minecraft:awkward_potion");
        recipe.add("input", input);
        
        // Ingredient (essence)
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", essenceItem);
        recipe.add("ingredient", ingredient);
        
        // Output potion (affinity potion) - using item ID directly
        JsonObject outputPotion = new JsonObject();
        outputPotion.addProperty("item", "ars_affinity:" + schoolName + "_affinity");
        recipe.add("output", outputPotion);
        
        // Save the brewing recipe
        Path recipePath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("recipe")
            .resolve(schoolName + "_affinity_potion.json");
            
        try {
            DataProvider.saveStable(output, recipe, recipePath);
            ArsAffinity.LOGGER.info("Generated brewing recipe for {}: {}", schoolName, recipePath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save brewing recipe for {}: {}", schoolName, e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "Ars Affinity Recipes";
    }
}
