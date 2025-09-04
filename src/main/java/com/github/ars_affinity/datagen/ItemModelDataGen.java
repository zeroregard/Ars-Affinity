package com.github.ars_affinity.datagen;

import com.github.ars_affinity.ArsAffinity;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class ItemModelDataGen implements DataProvider {
    
    private final PackOutput packOutput;
    private final ExistingFileHelper existingFileHelper;
    
    public ItemModelDataGen(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        this.packOutput = packOutput;
        this.existingFileHelper = existingFileHelper;
    }
    
    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return CompletableFuture.runAsync(() -> {
            generateAffinityAnchorCharmModel(output);
        });
    }
    
    private void generateAffinityAnchorCharmModel(CachedOutput output) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "item/generated");
        
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", ArsAffinity.MOD_ID + ":item/affinity_anchor_charm");
        model.add("textures", textures);
        
        // Save the item model
        Path modelPath = packOutput.getOutputFolder()
            .resolve("assets")
            .resolve(ArsAffinity.MOD_ID)
            .resolve("models")
            .resolve("item")
            .resolve("affinity_anchor_charm.json");
            
        try {
            DataProvider.saveStable(output, model, modelPath);
            ArsAffinity.LOGGER.info("Generated affinity anchor charm item model: {}", modelPath);
        } catch (Exception e) {
            ArsAffinity.LOGGER.error("Failed to save affinity anchor charm item model: {}", e.getMessage());
        }
    }
    
    @Override
    public String getName() {
        return "Ars Affinity Item Models";
    }
}
