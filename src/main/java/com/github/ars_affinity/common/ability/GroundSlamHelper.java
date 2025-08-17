package com.github.ars_affinity.common.ability;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GroundSlamHelper {
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) return;

        double currentMana = manaCap.getCurrentMana();
        double requiredMana = perk.manaCost; // Using absolute mana, per manipulation pattern
        if (currentMana < requiredMana) return;

        // Block if already on cooldown
        if (player.hasEffect(ModPotions.GROUND_SLAM_COOLDOWN_EFFECT)) {
            return;
        }

        // Mark starting height regardless of being grounded to allow micro-hops
        double startY = player.getY();
        player.getPersistentData().putDouble("ars_affinity_ground_slam_start_y", startY);
        player.getPersistentData().putBoolean("ars_affinity_ground_slam_active", true);
        player.getPersistentData().putInt("ars_affinity_ground_slam_cooldown", perk.cooldown);
        player.getPersistentData().putInt("ars_affinity_ground_slam_mana_cost", (int) requiredMana);

        // Force downward velocity if in air to create a rapid drop
        if (!player.onGround()) {
            Vec3 vel = player.getDeltaMovement();
            double downward = Math.min(vel.y - 2.5, -1.5);
            player.setDeltaMovement(vel.x, downward, vel.z);
            player.hurtMarked = true;
        }

        ArsAffinity.LOGGER.info("Ground Slam primed for {}", player.getGameProfile().getName());
    }

    public static void onPlayerLanded(ServerPlayer player) {
        if (!player.getPersistentData().getBoolean("ars_affinity_ground_slam_active")) return;
        player.getPersistentData().remove("ars_affinity_ground_slam_active");

        Level level = player.level();
        if (level.isClientSide()) return;

        // Compute drop height
        double startY = player.getPersistentData().getDouble("ars_affinity_ground_slam_start_y");
        double dropDistance = Math.max(0.0, startY - player.getY());

        // Config values
        double maxDrop = ArsAffinityConfig.GROUND_SLAM_MAX_DROP_DISTANCE.get();
        double maxRadius = ArsAffinityConfig.GROUND_SLAM_MAX_RADIUS.get();

        // Clamp drop for scaling
        double clampedDrop = Math.min(dropDistance, maxDrop);
        double dropScale = 1.0 + (clampedDrop <= 0 ? 0.0 : Math.log(1.0 + clampedDrop) / Math.log(1.0 + maxDrop));

        double radius = maxRadius;

        double baseDamageAtCenter = 10.0;
        double baseKnockbackAtCenter = 1.5;

        Vec3 pos = player.position();
        AABB area = new AABB(pos.x - radius, pos.y - 2, pos.z - radius, pos.x + radius, pos.y + 2, pos.z + radius);
        List<Entity> entities = level.getEntities(player, area, e -> e instanceof LivingEntity && e != player && !e.isAlliedTo(player));

        for (Entity e : entities) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.position().distanceTo(pos);
            if (dist > radius) continue;
            double proximityScale = 1.0 - (dist / radius);

            double damage = baseDamageAtCenter * proximityScale * dropScale;
            double kb = baseKnockbackAtCenter * proximityScale * dropScale;

            target.hurt(level.damageSources().playerAttack(player), (float) damage);

            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double horiz = Math.max(Math.hypot(dx, dz), 0.001);
            target.knockback(kb, -dx / horiz, -dz / horiz);
        }

        player.resetFallDistance();

        if (level instanceof ServerLevel sl) {
            for (int i = 0; i < 40; i++) {
                double ox = (level.random.nextDouble() - 0.5) * radius * 1.5;
                double oz = (level.random.nextDouble() - 0.5) * radius * 1.5;
                double oy = level.random.nextDouble() * 0.5;
                sl.sendParticles(ParticleTypes.POOF, pos.x + ox * 0.5, pos.y + 0.2, pos.z + oz * 0.5, 1, 0, 0, 0, 0.0);
            }
        }
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 1.0f, 0.8f);

        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap != null) {
            int manaCost = player.getPersistentData().getInt("ars_affinity_ground_slam_mana_cost");
            if (manaCost > 0) manaCap.removeMana(manaCost);
        }

        int cooldown = player.getPersistentData().getInt("ars_affinity_ground_slam_cooldown");
        if (cooldown > 0) {
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModPotions.GROUND_SLAM_COOLDOWN_EFFECT, cooldown));
        }
    }
}