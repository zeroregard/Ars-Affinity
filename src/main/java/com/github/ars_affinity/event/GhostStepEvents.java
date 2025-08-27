package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class GhostStepEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        // Check if the spell contains abjuration school glyphs
        boolean hasAbjurationSchool = event.context.getSpell().unsafeList().stream()
            .anyMatch(part -> part.spellSchools.contains(SpellSchools.ABJURATION));
        if (!hasAbjurationSchool) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            // O(1) perk lookup using the new perk index
            AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_GHOST_STEP, perk -> {
                if (perk instanceof AffinityPerk.GhostStepPerk ghostStepPerk) {
                    // Apply ghost step effect
                    // This would typically involve invisibility and healing
                    ArsAffinity.LOGGER.info("Player {} cast abjuration spell - PASSIVE_GHOST_STEP active (heal: {}, invis: {}s, cd: {}s)", 
                        player.getName().getString(), 
                        ghostStepPerk.amount, 
                        ghostStepPerk.time / 20, 
                        ghostStepPerk.cooldown / 20);
                }
            });
        }
    }
}