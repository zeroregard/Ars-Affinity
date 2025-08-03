package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveSummonHealthEvents {
    
    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.shooter instanceof Player player)) return;
        if (event.world.isClientSide()) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            int conjurationTier = progress.getTier(SpellSchools.CONJURATION);
            if (conjurationTier > 0) {
                AffinityPerkHelper.applyHighestTierPerk(progress, conjurationTier, SpellSchools.CONJURATION, AffinityPerkType.PASSIVE_SUMMON_HEALTH, perk -> {
                    if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
                        if (event.summon.getLivingEntity() != null) {
                            int amplifier = Math.round(durationPerk.amount);
                            event.summon.getLivingEntity().addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, durationPerk.time, amplifier));
                            
                            ArsAffinity.LOGGER.info("Player {} summoned entity with PASSIVE_SUMMON_HEALTH perk ({}%) - adding health boost effect with amplifier {} for {} seconds", 
                                player.getName().getString(), (int)(durationPerk.amount * 100), amplifier, durationPerk.time / 20);
                        }
                    }
                });
            }
        }
    }
} 