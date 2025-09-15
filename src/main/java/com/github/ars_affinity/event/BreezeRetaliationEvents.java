package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.Random;

public class BreezeRetaliationEvents {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;

        // Get the attacker
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? 
            (LivingEntity) event.getSource().getEntity() : null;
        
        if (attacker == null) return;

        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_BREEZE_RETALIATION, AffinityPerk.AmountBasedPerk.class, amountPerk -> {
            if (RANDOM.nextFloat() < amountPerk.amount) {
                // Shoot a Breeze Charge at the melee attacker
                ServerLevel level = (ServerLevel) player.level();
                
                Vec3 playerPos = player.getEyePosition();
                Vec3 attackerPos = attacker.getEyePosition();
                Vec3 direction = attackerPos.subtract(playerPos).normalize();
                
                // Create WindCharge projectile
                WindCharge windCharge = new WindCharge(level, playerPos.x, playerPos.y, playerPos.z, direction);
                windCharge.setOwner(player);
                windCharge.setDeltaMovement(direction.scale(2.0)); // Moderate speed
                
                level.addFreshEntity(windCharge);
                
                ArsAffinity.LOGGER.info("Breeze Retaliation activated! Player {} shot Breeze Charge at attacker {} ({}% chance)", 
                    player.getName().getString(), attacker.getName().getString(), (int)(amountPerk.amount * 100));
            }
        });
    }
}