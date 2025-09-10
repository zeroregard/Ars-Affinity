package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster;
import net.neoforged.bus.api.SubscribeEvent;

public class PassivePacifistEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Pre event) {
        // PASSIVE_PACIFIST removed - no longer needed in new system
        // All code commented out
    }
}