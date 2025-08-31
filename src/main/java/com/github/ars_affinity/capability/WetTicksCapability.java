package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.capabilities.EntityCapability;

public class WetTicksCapability {
    
    public static final EntityCapability<WetTicks, Void> WET_TICKS = 
        EntityCapability.createVoid(
            ResourceLocation.fromNamespaceAndPath(ArsAffinity.MOD_ID, "wet_ticks"),
            WetTicks.class
        );
    
    private WetTicksCapability() {
        // Utility class, prevent instantiation
    }
}