package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FireThornsEvents {
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_FIRE_THORNS, perk -> {
            if (perk instanceof AffinityPerk.AmountBasedPerk amountPerk) {
                if (Math.random() < amountPerk.amount) {
                    LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
                    if (attacker != null && attacker != player) {
                        attacker.setSecondsOnFire(5);
                        
                        ArsAffinity.LOGGER.debug("Player {} was attacked - PASSIVE_FIRE_THORNS ignited attacker {} (chance: {}%)", 
                            player.getName().getString(), 
                            attacker.getName().getString(),
                            (int)(amountPerk.amount * 100));
                    }
                }
            }
        });
    }
} 