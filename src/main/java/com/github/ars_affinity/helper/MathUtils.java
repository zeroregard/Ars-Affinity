package com.github.ars_affinity.helper;

import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class MathUtils {
    public static @Nullable EntityHitResult getLookedAtEntity(Entity entity, double range) {
        Vec3 eyePos = entity.getEyePosition(1.0f);
        Vec3 lookVec = entity.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.x * range, lookVec.y * range, lookVec.z * range);
    
        // Expand bounding box towards the look vector so it covers the ray path
        AABB searchBox = entity.getBoundingBox()
                .expandTowards(lookVec.scale(range))
                .inflate(1.0D, 1.0D, 1.0D);
    
        return traceEntities(entity, eyePos, targetPos, searchBox,
                (e) -> !e.isSpectator() && e.isPickable(), range);
    }
    
    public static @Nullable EntityHitResult traceEntities(Entity shooter, Vec3 startVec, Vec3 endVec,
                                                          AABB boundingBox, Predicate<Entity> filter, double range) {
        Level level = shooter.level();
        double closestDistSq = range * range; // ✅ now using squared distance
        Entity hitEntity = null;
        Vec3 hitVec = null;
    
        for (Entity target : level.getEntities(shooter, boundingBox, filter)) {
            AABB targetBox = target.getBoundingBox().inflate(target.getPickRadius());
            Optional<Vec3> clipResult = targetBox.clip(startVec, endVec);
    
            if (targetBox.contains(startVec)) {
                // If we're starting inside the entity
                hitEntity = target;
                hitVec = clipResult.orElse(startVec);
                closestDistSq = 0.0D;
            } else if (clipResult.isPresent()) {
                Vec3 hitPos = clipResult.get();
                double distSq = startVec.distanceToSqr(hitPos); // ✅ both are squared
    
                if (distSq < closestDistSq) {
                    if (target.getRootVehicle() == shooter.getRootVehicle() && !target.canRiderInteract()) {
                        // Skip own vehicle passengers unless inside
                        if (closestDistSq == 0.0D) {
                            hitEntity = target;
                            hitVec = hitPos;
                        }
                    } else {
                        hitEntity = target;
                        hitVec = hitPos;
                        closestDistSq = distSq;
                    }
                }
            }
        }
    
        return hitEntity == null ? null : new EntityHitResult(hitEntity, hitVec);
    }
}
