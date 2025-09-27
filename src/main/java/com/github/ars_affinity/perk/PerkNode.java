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
    private final String prerequisiteGlyph;
    private final PerkCategory category;
    
    // Configurable perk values
    private final float amount;
    private final int time;
    private final int cooldown;
    private final float manaCost;
    private final float damage;
    private final int freezeTime;
    private final float radius;
    private final float dashLength;
    private final float dashDuration;
    private final float health;
    private final float hunger;
    
    public PerkNode(String id, AffinityPerkType perkType, SpellSchool school, int tier, 
                   int pointCost, List<String> prerequisites, String prerequisiteGlyph, PerkCategory category,
                   float amount, int time, int cooldown, float manaCost, float damage,
                   int freezeTime, float radius, float dashLength, float dashDuration,
                   float health, float hunger) {
        this.id = id;
        this.perkType = perkType;
        this.school = school;
        this.tier = tier;
        this.pointCost = pointCost;
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.prerequisiteGlyph = prerequisiteGlyph;
        this.category = category;
        this.amount = amount;
        this.time = time;
        this.cooldown = cooldown;
        this.manaCost = manaCost;
        this.damage = damage;
        this.freezeTime = freezeTime;
        this.radius = radius;
        this.dashLength = dashLength;
        this.dashDuration = dashDuration;
        this.health = health;
        this.hunger = hunger;
    }
    
    // Getters
    public String getId() { return id; }
    public AffinityPerkType getPerkType() { return perkType; }
    public SpellSchool getSchool() { return school; }
    public int getTier() { return tier; }
    public int getPointCost() { return pointCost; }
    public List<String> getPrerequisites() { return new ArrayList<>(prerequisites); }
    public String getPrerequisiteGlyph() { return prerequisiteGlyph; }
    public PerkCategory getCategory() { return category; }
    
    // Configurable perk value getters
    public float getAmount() { return amount; }
    public int getTime() { return time; }
    public int getCooldown() { return cooldown; }
    public float getManaCost() { return manaCost; }
    public float getDamage() { return damage; }
    public int getFreezeTime() { return freezeTime; }
    public float getRadius() { return radius; }
    public float getDashLength() { return dashLength; }
    public float getDashDuration() { return dashDuration; }
    public float getHealth() { return health; }
    public float getHunger() { return hunger; }
    
    // Utility methods
    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }
    
    public boolean hasPrerequisiteGlyph() {
        return prerequisiteGlyph != null && !prerequisiteGlyph.isEmpty();
    }
    
    public boolean isRootNode() {
        return prerequisites.isEmpty();
    }
    
    public String getDisplayName() {
        return "ars_affinity.perk." + perkType.name();
    }
    
    public String getDescription() {
        return "ars_affinity.perk." + perkType.name() + ".desc";
    }
    
    // Generate a unique identifier for this specific tier of the perk
    public String getUniqueId() {
        return school.getId().toString().toUpperCase().replace(":", "_") + "_" + perkType.name() + "_" + tier;
    }
    
    // Serialization
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        tag.putString("perkType", perkType.name());
        tag.putString("school", school.getId().toString());
        tag.putInt("tier", tier);
        tag.putInt("pointCost", pointCost);
        tag.putString("prerequisiteGlyph", prerequisiteGlyph != null ? prerequisiteGlyph : "");
        tag.putString("category", category.name());
        tag.putFloat("amount", amount);
        tag.putInt("time", time);
        tag.putInt("cooldown", cooldown);
        tag.putFloat("manaCost", manaCost);
        tag.putFloat("damage", damage);
        tag.putInt("freezeTime", freezeTime);
        tag.putFloat("radius", radius);
        tag.putFloat("dashLength", dashLength);
        tag.putFloat("dashDuration", dashDuration);
        tag.putFloat("health", health);
        tag.putFloat("hunger", hunger);
        
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
        String prerequisiteGlyph = tag.getString("prerequisiteGlyph");
        if (prerequisiteGlyph.isEmpty()) prerequisiteGlyph = null;
        PerkCategory category = PerkCategory.valueOf(tag.getString("category"));
        float amount = tag.getFloat("amount");
        int time = tag.getInt("time");
        int cooldown = tag.getInt("cooldown");
        float manaCost = tag.getFloat("manaCost");
        float damage = tag.getFloat("damage");
        int freezeTime = tag.getInt("freezeTime");
        float radius = tag.getFloat("radius");
        float dashLength = tag.getFloat("dashLength");
        float dashDuration = tag.getFloat("dashDuration");
        float health = tag.getFloat("health");
        float hunger = tag.getFloat("hunger");
        
        List<String> prerequisites = new ArrayList<>();
        ListTag prerequisitesTag = tag.getList("prerequisites", Tag.TAG_STRING);
        for (Tag prerequisiteTag : prerequisitesTag) {
            if (prerequisiteTag instanceof net.minecraft.nbt.StringTag stringTag) {
                prerequisites.add(stringTag.getAsString());
            }
        }
        
        return new PerkNode(id, perkType, school, tier, pointCost, prerequisites, prerequisiteGlyph,
                           category, amount, time, cooldown, manaCost, damage,
                           freezeTime, radius, dashLength, dashDuration, health, hunger);
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
                ", amount=" + amount +
                '}';
    }
}