package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveDehydratedEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        boolean hasWaterSchool = event.context.getSpell().unsafeList().stream()
            .anyMatch(part -> part.spellSchools.contains(SpellSchools.ELEMENTAL_WATER));
        if (!hasWaterSchool) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_DEHYDRATED, perk -> {
                if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                    player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, (int)(20 * amountPerk.amount), 0));
                    
                    ArsAffinity.LOGGER.info("Player {} cast Elemental Water spell - PASSIVE_DEHYDRATED applied confusion for {} seconds", 
                        player.getName().getString(), 
                        (int)(20 * amountPerk.amount));
                }
            });
        }
    }
} 