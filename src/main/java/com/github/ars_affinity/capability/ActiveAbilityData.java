package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class ActiveAbilityData implements INBTSerializable<CompoundTag> {
    
    private AffinityPerkType activeAbilityType = null;
    private boolean isDirty = false;
    
    public ActiveAbilityData() {
    }
    
    public AffinityPerkType getActiveAbilityType() {
        return activeAbilityType;
    }
    
    public void setActiveAbilityType(AffinityPerkType abilityType) {
        if (this.activeAbilityType != abilityType) {
            this.activeAbilityType = abilityType;
            markDirty();
        }
    }
    
    public void clearActiveAbility() {
        if (this.activeAbilityType != null) {
            this.activeAbilityType = null;
            markDirty();
        }
    }
    
    public boolean hasActiveAbility() {
        return activeAbilityType != null;
    }
    
    public boolean isActiveAbility(AffinityPerkType abilityType) {
        return activeAbilityType == abilityType;
    }
    
    private void markDirty() {
        this.isDirty = true;
    }
    
    public boolean isDirty() {
        return isDirty;
    }
    
    public void setDirty(boolean dirty) {
        this.isDirty = dirty;
    }
    
    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        
        if (activeAbilityType != null) {
            tag.putString("activeAbilityType", activeAbilityType.name());
        }
        
        return tag;
    }
    
    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("activeAbilityType")) {
            String abilityTypeName = tag.getString("activeAbilityType");
            try {
                this.activeAbilityType = AffinityPerkType.valueOf(abilityTypeName);
            } catch (IllegalArgumentException e) {
                ArsAffinity.LOGGER.warn("Unknown active ability type: {}", abilityTypeName);
                this.activeAbilityType = null;
            }
        } else {
            this.activeAbilityType = null;
        }
    }
}
