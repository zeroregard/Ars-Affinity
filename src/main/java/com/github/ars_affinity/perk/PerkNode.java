package com.github.ars_affinity.perk;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;

public class PerkNode {
    private final String id;
    private final AffinityPerkType perkType;
    private final SpellSchool school;
    private final int tier;
    private final int pointCost;
    private final List<String> prerequisites;
    private final PerkCategory category;
    private final int level;
    
    public PerkNode(String id, AffinityPerkType perkType, SpellSchool school, int tier, 
                   int pointCost, List<String> prerequisites, PerkCategory category, 
                   int level) {
        this.id = id;
        this.perkType = perkType;
        this.school = school;
        this.tier = tier;
        this.pointCost = pointCost;
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.category = category;
        this.level = level;
    }
    
    // Getters
    public String getId() { return id; }
    public AffinityPerkType getPerkType() { return perkType; }
    public SpellSchool getSchool() { return school; }
    public int getTier() { return tier; }
    public int getPointCost() { return pointCost; }
    public List<String> getPrerequisites() { return new ArrayList<>(prerequisites); }
    public PerkCategory getCategory() { return category; }
    public int getLevel() { return level; }
    
    // Utility methods
    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }
    
    public boolean isRootNode() {
        return prerequisites.isEmpty();
    }
    
    public String getDisplayName() {
        return "ars_affinity.perk." + perkType.name() + "_" + level;
    }
    
    public String getDescription() {
        return "ars_affinity.perk." + perkType.name() + "_" + level + ".desc";
    }
    
    // Generate a unique identifier for this specific level of the perk
    public String getUniqueId() {
        return school.getId().toString().toUpperCase().replace(":", "_") + "_" + perkType.name() + "_" + level;
    }
    
    // Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("perkType", perkType.name());
        tag.putString("school", school.getId().toString());
        tag.putInt("tier", tier);
        tag.putInt("pointCost", pointCost);
        tag.putString("category", category.name());
        tag.putInt("level", level);
        
        ListTag prerequisitesTag = new ListTag();
        for (String prerequisite : prerequisites) {
            prerequisitesTag.add(net.minecraft.nbt.StringTag.valueOf(prerequisite));
        }
        tag.put("prerequisites", prerequisitesTag);
        
        return tag;
    }
    
    public static PerkNode deserializeNBT(CompoundTag tag) {
        String id = tag.getString("id");
        AffinityPerkType perkType = AffinityPerkType.valueOf(tag.getString("perkType"));
        SpellSchool school = parseSpellSchool(tag.getString("school"));
        int tier = tag.getInt("tier");
        int pointCost = tag.getInt("pointCost");
        PerkCategory category = PerkCategory.valueOf(tag.getString("category"));
        int level = tag.getInt("level");
        
        List<String> prerequisites = new ArrayList<>();
        ListTag prerequisitesTag = tag.getList("prerequisites", Tag.TAG_STRING);
        for (Tag prerequisiteTag : prerequisitesTag) {
            if (prerequisiteTag instanceof net.minecraft.nbt.StringTag stringTag) {
                prerequisites.add(stringTag.getAsString());
            }
        }
        
        return new PerkNode(id, perkType, school, tier, pointCost, prerequisites, 
                           category, level);
    }
    
    private static SpellSchool parseSpellSchool(String schoolId) {
        return switch (schoolId) {
            case "ars_nouveau:elemental_fire" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_FIRE;
            case "ars_nouveau:elemental_water" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_WATER;
            case "ars_nouveau:elemental_earth" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_EARTH;
            case "ars_nouveau:elemental_air" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ELEMENTAL_AIR;
            case "ars_nouveau:abjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.ABJURATION;
            case "ars_nouveau:necromancy" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.NECROMANCY;
            case "ars_nouveau:conjuration" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.CONJURATION;
            case "ars_nouveau:manipulation" -> com.hollingsworth.arsnouveau.api.spell.SpellSchools.MANIPULATION;
            default -> throw new IllegalArgumentException("Unknown spell school: " + schoolId);
        };
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PerkNode perkNode = (PerkNode) obj;
        return id.equals(perkNode.id);
    }
    
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public String toString() {
        return "PerkNode{" +
                "id='" + id + '\'' +
                ", perkType=" + perkType +
                ", school=" + school.getId() +
                ", tier=" + tier +
                ", pointCost=" + pointCost +
                ", category=" + category +
                ", level=" + level +
                '}';
    }
}