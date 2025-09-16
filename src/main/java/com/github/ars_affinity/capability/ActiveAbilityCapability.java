package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class ActiveAbilityCapability {
    
    public static final EntityCapability<ActiveAbilityData, Void> ACTIVE_ABILITY_DATA = 
        EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "active_ability_data"),
            ActiveAbilityData.class
        );
    
    private ActiveAbilityCapability() {
        // Utility class, prevent instantiation
    }
}
