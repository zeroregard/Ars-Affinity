package com.github.ars_affinity.perk;

import java.util.Set;

public class ActiveAbilityHelper {
    
    private static final Set<AffinityPerkType> ACTIVE_ABILITY_TYPES = Set.of(
        AffinityPerkType.ACTIVE_GROUND_SLAM,
        AffinityPerkType.ACTIVE_ICE_BLAST,
        AffinityPerkType.ACTIVE_FIRE_DASH,
        AffinityPerkType.ACTIVE_AIR_DASH,
        AffinityPerkType.ACTIVE_GHOST_STEP,
        AffinityPerkType.ACTIVE_CURSE_FIELD,
        AffinityPerkType.ACTIVE_SANCTUARY,
        AffinityPerkType.ACTIVE_SWAP_ABILITY
    );
    
    public static boolean isActiveAbility(AffinityPerkType perkType) {
        return ACTIVE_ABILITY_TYPES.contains(perkType);
    }
    
    public static Set<AffinityPerkType> getActiveAbilityTypes() {
        return Set.copyOf(ACTIVE_ABILITY_TYPES);
    }
}
