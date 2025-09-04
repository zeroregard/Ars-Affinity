package com.github.ars_affinity.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ArsAffinityDataGenerators {
    
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        PackOutput packOutput = event.getGenerator().getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        
        // Register our data generators
        event.getGenerator().addProvider(event.includeServer(), new RecipeDataGen(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeServer(), new CuriosDataGen(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeClient(), new ItemModelDataGen(packOutput, existingFileHelper));
        
        // Brewing recipes are registered through RegisterBrewingRecipesEvent in ModPotions
    }
}