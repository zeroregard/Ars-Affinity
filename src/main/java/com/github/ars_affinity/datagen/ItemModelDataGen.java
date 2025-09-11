package com.github.ars_affinity.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

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
            // No item models to generate
        });
    }
    
    @Override
    public String getName() {
        return "Ars Affinity Item Models";
    }
}
