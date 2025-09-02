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

public class CuriosDataGen implements DataProvider {
    
    private final PackOutput packOutput;
    private final ExistingFileHelper existingFileHelper;
    
    public CuriosDataGen(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        this.packOutput = packOutput;
        this.existingFileHelper = existingFileHelper;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(() -> {
            generateCuriosEntitiesData(output);
            generateCuriosSlotsData(output);
        });
    }
    
    private void generateCuriosEntitiesData(CachedOutput output) {
        JsonObject data = new JsonObject();
        
        // Entities that can use curios
        JsonArray entities = new JsonArray();
        entities.add("minecraft:player");
        data.add("entities", entities);
        
        // Slots available
        JsonArray slots = new JsonArray();
        slots.add("charm");
        data.add("slots", slots);
        
        // Save the curios entities data
        Path dataPath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("curios")
            .resolve("entities")
            .resolve("ars_affinity.json");
            
        try {
            DataProvider.saveStable(output, data, dataPath);
            ArsAffinity.LOGGER.info("Generated curios entities data: {}", dataPath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save curios entities data: {}", e.getMessage());
        }
    }
    
    private void generateCuriosSlotsData(CachedOutput output) {
        JsonObject data = new JsonObject();
        
        // Slot configuration
        data.addProperty("size", 1);
        data.addProperty("locked", false);
        data.addProperty("cosmetic", false);
        data.addProperty("damage_enabled", false);
        data.addProperty("drop_rule", "default");
        data.addProperty("priority", 0);
        data.addProperty("render", true);
        data.addProperty("icon", "curios:slot_empty_charm");
        
        // Save the curios slots data
        Path dataPath = packOutput.getOutputFolder()
            .resolve("data")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("curios")
            .resolve("slots")
            .resolve("charm.json");
            
        try {
            DataProvider.saveStable(output, data, dataPath);
            ArsAffinity.LOGGER.info("Generated curios slots data: {}", dataPath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save curios slots data: {}", e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "Ars Affinity Curios Data";
    }
}
