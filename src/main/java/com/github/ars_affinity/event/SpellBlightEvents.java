package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.event.SpellResolveEvent;
import net.neoforged.bus.api.SubscribeEvent;

public class SpellBlightEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        // PASSIVE_BLIGHTED removed - no longer needed in new system
        // All code commented out
    }

    @SubscribeEvent
    public static void onSpellResolve(SpellResolveEvent.Post event) {
        // PASSIVE_BLIGHTED removed - no longer needed in new system
        // All code commented out
    }
}