package com.github.ars_affinity.perk;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class PerkReference {
    private final AffinityPerkType perkType;
    private final SpellSchool sourceSchool;
    private final int sourceTier;
    
    public PerkReference(AffinityPerkType perkType, SpellSchool sourceSchool, int sourceTier) {
        this.perkType = perkType;
        this.sourceSchool = sourceSchool;
        this.sourceTier = sourceTier;
    }
    
    public AffinityPerkType getPerkType() {
        return perkType;
    }
    
    public SpellSchool getSourceSchool() {
        return sourceSchool;
    }
    
    public int getSourceTier() {
        return sourceTier;
    }
    
    public String getRegistryKey() {
        return String.format("%s_%s_%d", 
            sourceSchool.getId().toString().toUpperCase().replace(":", "_"), 
            perkType.name(), 
            sourceTier);
    }
    
    public PerkData getPerkData() {
        return PerkRegistry.getPerk(getRegistryKey());
    }
    
    public boolean isValid() {
        return PerkRegistry.hasPerk(getRegistryKey());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        PerkReference that = (PerkReference) obj;
        return sourceTier == that.sourceTier &&
               perkType == that.perkType &&
               sourceSchool.equals(that.sourceSchool);
    }
    
    @Override
    public int hashCode() {
        int result = perkType.hashCode();
        result = 31 * result + sourceSchool.hashCode();
        result = 31 * result + sourceTier;
        return result;
    }
    
    @Override
    public String toString() {
        return String.format("PerkReference{type=%s, school=%s, tier=%d}", 
            perkType, sourceSchool.getId().toString().replace(":", "_"), sourceTier);
    }
    
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("perkType", perkType.name());
        tag.putString("sourceSchool", getShortSchoolId(sourceSchool));
        tag.putInt("sourceTier", sourceTier);
        return tag;
    }
    
    public static PerkReference deserializeNBT(CompoundTag tag) {
        AffinityPerkType perkType = AffinityPerkType.valueOf(tag.getString("perkType"));
        String schoolId = tag.getString("sourceSchool");
        SpellSchool sourceSchool = getSpellSchoolFromId(schoolId);
        int sourceTier = tag.getInt("sourceTier");
        return new PerkReference(perkType, sourceSchool, sourceTier);
    }
    
    private static SpellSchool getSpellSchoolFromId(String id) {
        return switch (id) {
            // Full format (current)
            case "ars_nouveau:elemental_fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "ars_nouveau:elemental_water" -> SpellSchools.ELEMENTAL_WATER;
            case "ars_nouveau:elemental_earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "ars_nouveau:elemental_air" -> SpellSchools.ELEMENTAL_AIR;
            case "ars_nouveau:abjuration" -> SpellSchools.ABJURATION;
            case "ars_nouveau:necromancy" -> SpellSchools.NECROMANCY;
            case "ars_nouveau:conjuration" -> SpellSchools.CONJURATION;
            case "ars_nouveau:manipulation" -> SpellSchools.MANIPULATION;
            // Short format (legacy - for backward compatibility)
            case "fire" -> SpellSchools.ELEMENTAL_FIRE;
            case "water" -> SpellSchools.ELEMENTAL_WATER;
            case "earth" -> SpellSchools.ELEMENTAL_EARTH;
            case "air" -> SpellSchools.ELEMENTAL_AIR;
            default -> SpellSchools.ELEMENTAL_FIRE;
        };
    }
    
    private static String getShortSchoolId(SpellSchool school) {
        String schoolId = school.getId().toString();
        return switch (schoolId) {
            case "ars_nouveau:elemental_fire" -> "fire";
            case "ars_nouveau:elemental_water" -> "water";
            case "ars_nouveau:elemental_earth" -> "earth";
            case "ars_nouveau:elemental_air" -> "air";
            case "ars_nouveau:abjuration" -> "abjuration";
            case "ars_nouveau:necromancy" -> "necromancy";
            case "ars_nouveau:conjuration" -> "conjuration";
            case "ars_nouveau:manipulation" -> "manipulation";
            default -> schoolId;
        };
    }
}