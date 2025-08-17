package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public class ActiveAbilityManager {
    
    // Map of school to active ability perk type
    private static final Map<SpellSchool, AffinityPerkType> SCHOOL_ABILITY_MAP = new HashMap<>();
    
    static {
        SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_WATER, AffinityPerkType.ACTIVE_ICE_BLAST);
        SCHOOL_ABILITY_MAP.put(SpellSchools.ELEMENTAL_EARTH, AffinityPerkType.ACTIVE_GROUND_SLAM);
        SCHOOL_ABILITY_MAP.put(SpellSchools.MANIPULATION, AffinityPerkType.ACTIVE_SWAP_ABILITY);
        // Add more schools and their abilities here as they are implemented
    }
    
    public static void triggerActiveAbility(ServerPlayer player) {
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return;
        }

        // Find the first school with an active ability
        AffinityPerk activePerk = null;
        
        for (Map.Entry<SpellSchool, AffinityPerkType> entry : SCHOOL_ABILITY_MAP.entrySet()) {
            SpellSchool school = entry.getKey();
            AffinityPerkType perkType = entry.getValue();
            
            int tier = progress.getTier(school);
            if (tier > 0) {
                // Check if player has this perk
                final AffinityPerk[] foundPerk = {null};
                AffinityPerkHelper.applyHighestTierPerk(progress, tier, school, perkType, perk -> {
                    foundPerk[0] = perk;
                });
                
                if (foundPerk[0] != null) {
                    activePerk = foundPerk[0];
                    break;
                }
            }
        }
        
        if (activePerk == null || !(activePerk instanceof AffinityPerk.ActiveAbilityPerk)) {
            return;
        }

        AffinityPerk.ActiveAbilityPerk abilityPerk = (AffinityPerk.ActiveAbilityPerk) activePerk;
        
        // Route to the appropriate ability helper
        switch (abilityPerk.perk) {
            case ACTIVE_ICE_BLAST:
                IceBlastHelper.executeAbility(player, abilityPerk);
                break;
            case ACTIVE_SWAP_ABILITY:
                SwapAbilityHelper.executeAbility(player, abilityPerk);
                break;
            case ACTIVE_GROUND_SLAM:
                GroundSlamHelper.executeAbility(player, abilityPerk);
                break;
            default:
                ArsAffinity.LOGGER.warn("Unknown active ability perk type: {}", abilityPerk.perk);
                break;
        }
    }
} 