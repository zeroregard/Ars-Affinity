package com.github.ars_affinity.mixin;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(targets = "net.minecraft.world.entity.projectile.ProjectileUtil")
public class ProjectileCollisionMixin {

    @Inject(
        method = "getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;F)Lnet/minecraft/world/phys/EntityHitResult;",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void onGetEntityHitResult(Entity projectile, Vec3 startVec, Vec3 endVec, 
            net.minecraft.world.phys.AABB boundingBox, Predicate<Entity> filter, float maxDistance, 
            CallbackInfoReturnable<EntityHitResult> cir) {
        
        EntityHitResult result = cir.getReturnValue();
        if (result == null) {
            return;
        }
        
        Entity hitEntity = result.getEntity();
        if (!(hitEntity instanceof Player player)) {
            return;
        }
        
        // Check if player is airborne and has mid-air phasing perk
        if (player.onGround()) {
            return;
        }
        
        // Check if player has the perk and is not on cooldown
        if (player.hasEffect(ModPotions.MID_AIR_PHASING_COOLDOWN_EFFECT)) {
            return;
        }
        
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_MID_AIR_PHASING, AffinityPerk.DurationBasedPerk.class, perk -> {
            // Player is phased - simulate a miss by returning null
            // This makes the projectile continue flying until it hits something else
            cir.setReturnValue(null);
            
            // Apply cooldown effect
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                ModPotions.MID_AIR_PHASING_COOLDOWN_EFFECT, 
                perk.time, 0, false, true, true));
            
            ArsAffinity.LOGGER.debug("Projectile phased through player {} (Mid-Air Phasing active, {} tick cooldown)", 
                player.getName().getString(), perk.time);
        });
    }
}