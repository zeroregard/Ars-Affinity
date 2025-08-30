package com.github.ars_affinity.capability;

import net.minecraft.nbt.CompoundTag;

public class WetTicks {
    private int wetTicks = 0;
    
    public int getWetTicks() {
        return wetTicks;
    }
    
    public void setWetTicks(int wetTicks) {
        this.wetTicks = Math.max(0, wetTicks);
    }
    
    public void addWetTicks(int amount) {
        this.wetTicks = Math.max(0, this.wetTicks + amount);
    }
    
    public void resetWetTicks() {
        this.wetTicks = 0;
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("wetTicks", wetTicks);
        return tag;
    }
    
    public void deserializeNBT(CompoundTag tag) {
        this.wetTicks = tag.getInt("wetTicks");
    }
}