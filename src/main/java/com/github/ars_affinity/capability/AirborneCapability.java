package com.github.ars_affinity.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Capability for tracking how long an entity has been airborne
 */
public class AirborneCapability implements INBTSerializable<CompoundTag> {
    
    private long lastGroundTime = -1;
    private long totalAirborneTime = 0;
    private boolean isAirborne = false;
    
    /**
     * Call this every tick to update airborne status
     */
    public void tick(LivingEntity entity) {
        if (entity.onGround()) {
            if (isAirborne) {
                // Just landed, reset
                isAirborne = false;
                lastGroundTime = -1;
                totalAirborneTime = 0;
            }
        } else {
            if (!isAirborne) {
                // Just became airborne
                isAirborne = true;
                lastGroundTime = entity.level().getGameTime();
            }
            // Update total airborne time
            if (lastGroundTime > 0) {
                totalAirborneTime = entity.level().getGameTime() - lastGroundTime;
            }
        }
    }
    
    /**
     * Get the current airborne time in ticks
     */
    public long getAirborneTime() {
        return isAirborne ? totalAirborneTime : 0;
    }
    
    /**
     * Check if the entity is currently airborne
     */
    public boolean isAirborne() {
        return isAirborne;
    }
    
    /**
     * Reset the airborne tracking
     */
    public void reset() {
        isAirborne = false;
        lastGroundTime = -1;
        totalAirborneTime = 0;
    }
    
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("lastGroundTime", lastGroundTime);
        tag.putLong("totalAirborneTime", totalAirborneTime);
        tag.putBoolean("isAirborne", isAirborne);
        return tag;
    }
    
    @Override
    public void deserializeNBT(CompoundTag tag) {
        lastGroundTime = tag.getLong("lastGroundTime");
        totalAirborneTime = tag.getLong("totalAirborneTime");
        isAirborne = tag.getBoolean("isAirborne");
    }
}