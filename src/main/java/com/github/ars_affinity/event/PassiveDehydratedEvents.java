package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.ManaRegenCalcEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveDehydratedEvents {

    @SubscribeEvent
    public static void onManaRegenCalc(ManaRegenCalcEvent event) {
        // PASSIVE_DEHYDRATED removed - no longer needed in new system
        // All code commented out
    }
} 