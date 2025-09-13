package com.github.ars_affinity.mixin;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityRootedMixin {

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Only apply on server side
        if (entity.level().isClientSide()) return;
        
        // Check if entity has the rooted effect
        if (entity.hasEffect(ModPotions.ROOTED_EFFECT)) {
            // Apply downward velocity to pull the entity down
            Vec3 currentVelocity = entity.getDeltaMovement();
            Vec3 newVelocity = new Vec3(currentVelocity.x, Math.min(currentVelocity.y, -0.1), currentVelocity.z);
            entity.setDeltaMovement(newVelocity);
            
            // Force the entity to fall if they're not on the ground
            if (!entity.onGround()) {
                entity.fallDistance = 0.0f; // Reset fall distance to prevent fall damage
            }
        }
    }
}