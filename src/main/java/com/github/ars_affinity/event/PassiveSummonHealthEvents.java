package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SummonEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;

public class PassiveSummonHealthEvents {
    
    @SubscribeEvent
    public static void onSummonEvent(SummonEvent event) {
        if (!(event.shooter instanceof Player player)) return;
        if (event.world.isClientSide()) return;
        if (event.summon.getLivingEntity() == null) {
            return;
        }
        
        // Check if player has the summon health perk
        if (AffinityPerkHelper.hasActivePerk(player, AffinityPerkType.PASSIVE_SUMMON_HEALTH)) {
            float amount = AffinityPerkHelper.getPerkAmount(player, AffinityPerkType.PASSIVE_SUMMON_HEALTH);
            int time = AffinityPerkHelper.getPerkTime(player, AffinityPerkType.PASSIVE_SUMMON_HEALTH);
            
            int amplifier = Math.round(amount);
            event.summon.getLivingEntity().addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, time, amplifier));
            
            ArsAffinity.LOGGER.debug("Player {} summoned entity with PASSIVE_SUMMON_HEALTH perk ({}%) - adding health boost effect with amplifier {} for {} seconds", 
                player.getName().getString(), (int)(amount * 100), amplifier, time / 20);
        }
    }
} 