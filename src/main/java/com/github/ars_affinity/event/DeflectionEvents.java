package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class DeflectionEvents {
    
    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Projectile projectile)) return;
        if (!(event.getRayTraceResult().getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        if (player.hasEffect(ModPotions.DEFLECTION_COOLDOWN_EFFECT)) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_DEFLECTION, perk -> {
            if (perk instanceof AffinityPerk.DurationBasedPerk durationPerk) {
                event.setCanceled(true);
                
                player.addEffect(new MobEffectInstance(ModPotions.DEFLECTION_COOLDOWN_EFFECT, durationPerk.time, 0, false, true, true));
                
                Vec3 currentMotion = projectile.getDeltaMovement();
                Vec3 reversedMotion = currentMotion.scale(-1.0);
                projectile.setDeltaMovement(reversedMotion);
                
                Vec3 newPos = player.position().add(reversedMotion.normalize().scale(2.0));
                projectile.setPos(newPos.x, newPos.y, newPos.z);
                
                projectile.setOwner(player);
                
                ArsAffinity.LOGGER.debug("Player {} deflected projectile - PASSIVE_DEFLECTION active (cooldown: {}s)", 
                    player.getName().getString(), durationPerk.time / 20);
            }
        });
    }
} 