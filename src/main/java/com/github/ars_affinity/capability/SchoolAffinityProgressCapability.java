package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class SchoolAffinityProgressCapability {
    
    public static final EntityCapability<SchoolAffinityProgress, Void> SCHOOL_AFFINITY_PROGRESS = 
        EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "school_affinity_progress"),
            SchoolAffinityProgress.class
        );
    
    private SchoolAffinityProgressCapability() {
        // Utility class, prevent instantiation
    }
} 