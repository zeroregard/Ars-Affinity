package com.github.ars_affinity.perk;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.nbt.CompoundTag;

public class PerkAllocation {
    private final PerkNode node;
    private final int pointsInvested;
    private final boolean isActive;
    
    public PerkAllocation(PerkNode node) {
        this.node = node;
        this.pointsInvested = node.getPointCost();
        this.isActive = true;
    }
    
    public PerkAllocation(PerkNode node, int dynamicCost) {
        this.node = node;
        this.pointsInvested = dynamicCost;
        this.isActive = true;
    }
    
    public PerkAllocation(PerkNode node, int pointsInvested, boolean isActive) {
        this.node = node;
        this.pointsInvested = pointsInvested;
        this.isActive = isActive;
    }
    
    // Getters
    public PerkNode getNode() { return node; }
    public String getNodeId() { return node.getId(); }
    public AffinityPerkType getPerkType() { return node.getPerkType(); }
    public SpellSchool getSchool() { return node.getSchool(); }
    public int getTier() { return node.getTier(); }
    public int getPointsInvested() { return pointsInvested; }
    public boolean isActive() { return isActive; }
    public PerkCategory getCategory() { return node.getCategory(); }
    
    // Utility methods
    public String getDisplayName() {
        return node.getDisplayName();
    }
    
    public String getDescription() {
        return node.getDescription();
    }
    
    // Get the effective level for this allocation
    public int getEffectiveLevel() {
        return node.getTier();
    }
    
    // Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("nodeId", node.getId());
        tag.putInt("pointsInvested", pointsInvested);
        tag.putBoolean("isActive", isActive);
        return tag;
    }
    
    public static PerkAllocation deserializeNBT(CompoundTag tag) {
        String nodeId = tag.getString("nodeId");
        int pointsInvested = tag.getInt("pointsInvested");
        boolean isActive = tag.getBoolean("isActive");
        
        // Note: We'll need to reconstruct the PerkNode from the nodeId
        // This will be handled by the PerkTreeManager
        PerkNode node = PerkTreeManager.getNode(nodeId);
        if (node == null) {
            ArsAffinity.LOGGER.error("Failed to find PerkNode for ID: {}", nodeId);
            return null;
        }
        
        return new PerkAllocation(node, pointsInvested, isActive);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PerkAllocation that = (PerkAllocation) obj;
        return node.equals(that.node);
    }
    
    @Override
    public int hashCode() {
        return node.hashCode();
    }
    
    @Override
    public String toString() {
        return "PerkAllocation{" +
                "node=" + node.getId() +
                ", pointsInvested=" + pointsInvested +
                ", tier=" + node.getTier() +
                ", isActive=" + isActive +
                '}';
    }
}