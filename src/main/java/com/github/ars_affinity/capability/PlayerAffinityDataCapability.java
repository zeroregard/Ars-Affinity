package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class PlayerAffinityDataCapability {
    
    public static final EntityCapability<PlayerAffinityData, Void> PLAYER_AFFINITY_DATA = 
        EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "player_affinity_data"),
            PlayerAffinityData.class
        );
    
    private PlayerAffinityDataCapability() {
        // Utility class, prevent instantiation
    }
}
