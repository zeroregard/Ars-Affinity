package com.github.ars_affinity.common.ability;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.registry.ModSounds;
import com.hollingsworth.arsnouveau.api.mana.IManaCap;
import com.hollingsworth.arsnouveau.setup.registry.CapabilityRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


import java.util.List;

public class GroundSlamHelper {
    public static void executeAbility(ServerPlayer player, AffinityPerk.ActiveAbilityPerk perk) {
        Level level = player.level();
        if (level.isClientSide()) {
            return;
        }
        IManaCap manaCap = player.getCapability(CapabilityRegistry.MANA_CAPABILITY);
        if (manaCap == null) {
            return;
        }

        if (!player.onGround()) {
            return;
        }

        double currentMana = manaCap.getCurrentMana();
        double maxMana = manaCap.getMaxMana();
        double requiredMana = perk.manaCost * maxMana;
    
        if (currentMana < requiredMana) {
            return;
        }

        if (player.hasEffect(ModPotions.GROUND_SLAM_COOLDOWN_EFFECT)) {
            return;
        }

        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModPotions.GROUND_SLAM_COOLDOWN_EFFECT, perk.cooldown, 0, false, true, true));

        double maxRadius = ArsAffinityConfig.GROUND_SLAM_MAX_RADIUS.get();
        double radius = maxRadius;
        double baseDamageAtCenter = 10.0;
        double baseKnockbackAtCenter = 1.5;
        double manaPercent = maxMana > 0 ? (currentMana / maxMana) : 0.0;
        double scaledDamageAtCenter = baseDamageAtCenter * (1.0 + manaPercent);

        Vec3 pos = player.position();
        AABB area = new AABB(pos.x - radius, pos.y - 2, pos.z - radius, pos.x + radius, pos.y + 2, pos.z + radius);
        List<Entity> entities = level.getEntities(player, area, e -> e instanceof LivingEntity && e != player && !e.isAlliedTo(player));

        for (Entity e : entities) {
            LivingEntity target = (LivingEntity) e;
            double dist = target.position().distanceTo(pos);
            if (dist > radius) continue;
            double proximityScale = 1.0 - (dist / radius);

            double damage = scaledDamageAtCenter * proximityScale;
            double kb = baseKnockbackAtCenter * proximityScale;

            target.hurt(level.damageSources().playerAttack(player), (float) damage);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 0, false, true, true));

            double dx = target.getX() - player.getX();
            double dz = target.getZ() - player.getZ();
            double horiz = Math.max(Math.hypot(dx, dz), 0.001);
            target.knockback(kb, -dx / horiz, -dz / horiz);
        }

        if (level instanceof ServerLevel sl) {
            for (int i = 0; i < 40; i++) {
                double ox = (level.random.nextDouble() - 0.5) * radius * 1.5;
                double oz = (level.random.nextDouble() - 0.5) * radius * 1.5;
                sl.sendParticles(ParticleTypes.POOF, pos.x + ox * 0.5, pos.y + 0.2, pos.z + oz * 0.5, 1, 0, 0, 0, 0.0);
            }
        }
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.GROUND_SLAM.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

        manaCap.removeMana((int) requiredMana);
    }

    
}