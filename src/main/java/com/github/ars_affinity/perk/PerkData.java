package com.github.ars_affinity.perk;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

/**
 * Data class representing an active perk for a player.
 * Contains the perk itself along with metadata about which school and tier it came from.
 */
public class PerkData {
    public final AffinityPerk perk;
    public final SpellSchool sourceSchool;
    public final int sourceTier;
    
    public PerkData(AffinityPerk perk, SpellSchool sourceSchool, int sourceTier) {
        this.perk = perk;
        this.sourceSchool = sourceSchool;
        this.sourceTier = sourceTier;
    }
    
    @Override
    public String toString() {
        return "PerkData{perk=" + perk.perk + ", school=" + sourceSchool + ", tier=" + sourceTier + "}";
    }
}