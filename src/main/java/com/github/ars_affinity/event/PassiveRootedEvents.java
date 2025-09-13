package com.github.ars_affinity.event;

import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class PassiveRootedEvents {
    
    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        // Check if player has the Rooted perk
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_ROOTED, AffinityPerk.class, perk -> {
            // Get the attacker
            var damageSource = event.getSource();
            LivingEntity attacker = null;
            
            if (damageSource.getEntity() instanceof LivingEntity livingEntity) {
                attacker = livingEntity;
            } else if (damageSource.getDirectEntity() instanceof LivingEntity livingEntity) {
                attacker = livingEntity;
            }
            
            if (attacker != null && attacker != player) {
                // Apply the rooted effect to the attacker for 3 seconds (60 ticks)
                // This will continuously pull them downward
                MobEffectInstance rootedEffect = new MobEffectInstance(
                    ModPotions.ROOTED_EFFECT, 
                    60, // 3 seconds
                    0, // No amplifier
                    false, // Not ambient
                    true, // Visible particles
                    true // Show icon
                );
                
                attacker.addEffect(rootedEffect);
            }
        });
    }
}