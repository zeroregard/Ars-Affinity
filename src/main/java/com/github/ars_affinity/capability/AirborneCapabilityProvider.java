package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModCapabilities;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.neoforged.neoforge.event.AttachCapabilitiesEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID)
public class AirborneCapabilityProvider implements ICapabilityProvider {
    
    private final AirborneCapability capability = new AirborneCapability();
    private final LazyOptional<AirborneCapability> lazyOptional = LazyOptional.of(() -> capability);
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<LivingEntity> event) {
        if (event.getObject() instanceof net.minecraft.world.entity.player.Player) {
            event.addCapability(
                new net.minecraft.resources.ResourceLocation(ArsAffinity.MOD_ID, "airborne_capability"),
                new AirborneCapabilityProvider()
            );
        }
    }
    
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ModCapabilities.AIRBORNE_CAPABILITY) {
            return lazyOptional.cast();
        }
        return LazyOptional.empty();
    }
    
    public static void attach(LivingEntity entity) {
        entity.getCapability(ModCapabilities.AIRBORNE_CAPABILITY).ifPresent(airborne -> {
            airborne.tick(entity);
        });
    }
    
    @Override
    public void invalidateCaps() {
        lazyOptional.invalidate();
    }
}