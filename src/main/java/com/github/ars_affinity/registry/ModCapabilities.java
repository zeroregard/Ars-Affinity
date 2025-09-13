package com.github.ars_affinity.registry;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.AirborneCapability;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;

public class ModCapabilities {
    
    public static final Capability<AirborneCapability> AIRBORNE_CAPABILITY = 
        CapabilityManager.get(new CapabilityToken<AirborneCapability>() {});
    
    public static void register() {
        ArsAffinity.LOGGER.info("Registering capabilities for Ars Affinity");
    }
}