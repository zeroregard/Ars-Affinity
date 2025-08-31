package com.github.ars_affinity.datagen;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ArsAffinityBrewingDataGen implements DataProvider {
    
    private static final Map<SpellSchool, String> SCHOOL_TO_ESSENCE = new HashMap<>();
    private static final Map<SpellSchool, String> SCHOOL_TO_NAME = new HashMap<>();
    
    static {
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE, "ars_nouveau:fire_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER, "ars_nouveau:water_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH, "ars_nouveau:earth_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR, "ars_nouveau:air_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION, "ars_nouveau:abjuration_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY, "ars_nouveau:necromancy_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION, "ars_nouveau:conjuration_essence");
        SCHOOL_TO_ESSENCE.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION, "ars_nouveau:manipulation_essence");
        
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE, "fire");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER, "water");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH, "earth");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR, "air");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION, "abjuration");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY, "anima");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION, "conjuration");
        SCHOOL_TO_NAME.put(com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION, "manipulation");
    }
    
    private final PackOutput packOutput;
    private final ExistingFileHelper existingFileHelper;
    
    public ArsAffinityBrewingDataGen(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        this.packOutput = packOutput;
        this.existingFileHelper = existingFileHelper;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return generateBrewingRecipes(output);
    }
    
    private CompletableFuture<?> generateBrewingRecipes(CachedOutput output) {
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            generateBrewingRecipe(output, school);
        }
        return CompletableFuture.completedFuture(null);
    }
    
    private void generateBrewingRecipe(CachedOutput output, SpellSchool school) {
        String schoolName = SCHOOL_TO_NAME.get(school);
        String essenceItem = SCHOOL_TO_ESSENCE.get(school);
        
        if (essenceItem == null) {
            ArsAffinity.LOGGER.warn("Could not generate brewing recipe for school: {}", schoolName);
            return;
        }
        
        // Generate Level 1 recipe: mundane potion + essence = level 1 potion
        generateLevel1Recipe(output, schoolName, essenceItem);
        
        // Generate Level 2 recipe: level 1 potion + essence = level 2 potion
        generateLevel2Recipe(output, schoolName, essenceItem);
    }
    
    private void generateLevel1Recipe(CachedOutput output, String schoolName, String essenceItem) {
        // Create brewing recipe JSON for Level 1
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:brewing");
        
        // Input potion (mundane potion)
        JsonObject input = new JsonObject();
        input.addProperty("item", "minecraft:potion");
        recipe.add("input", input);
        
        // Ingredient (essence)
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", essenceItem);
        recipe.add("ingredient", ingredient);
        
        // Output potion (Level 1)
        JsonObject outputPotion = new JsonObject();
        outputPotion.addProperty("item", "ars_affinity:" + schoolName + "_affinity_level_1");
        recipe.add("output", outputPotion);
        
        // Save the Level 1 recipe
        Path recipePath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("recipes")
            .resolve(schoolName + "_affinity_level_1_potion.json");
            
        try {
            DataProvider.saveStable(output, recipe, recipePath);
            ArsAffinity.LOGGER.info("Generated Level 1 brewing recipe for {}: {}", schoolName, recipePath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save Level 1 brewing recipe for {}: {}", schoolName, e.getMessage());
        }
    }
    
    private void generateLevel2Recipe(CachedOutput output, String schoolName, String essenceItem) {
        // Create brewing recipe JSON for Level 2
        JsonObject recipe = new JsonObject();
        recipe.addProperty("type", "minecraft:brewing");
        
        // Input potion (Level 1 potion)
        JsonObject input = new JsonObject();
        input.addProperty("item", "ars_affinity:" + schoolName + "_affinity_level_1");
        recipe.add("input", input);
        
        // Ingredient (essence)
        JsonObject ingredient = new JsonObject();
        ingredient.addProperty("item", essenceItem);
        recipe.add("ingredient", ingredient);
        
        // Output potion (Level 2)
        JsonObject outputPotion = new JsonObject();
        outputPotion.addProperty("item", "ars_affinity:" + schoolName + "_affinity_level_2");
        recipe.add("output", outputPotion);
        
        // Save the Level 2 recipe
        Path recipePath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("recipes")
            .resolve(schoolName + "_affinity_level_2_potion.json");
            
        try {
            DataProvider.saveStable(output, recipe, recipePath);
            ArsAffinity.LOGGER.info("Generated Level 2 brewing recipe for {}: {}", schoolName, recipePath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save Level 2 brewing recipe for {}: {}", schoolName, recipePath);
        }
    }
    
    @Override
    public String getName() {
        return "Ars Affinity Brewing Recipes";
    }
}