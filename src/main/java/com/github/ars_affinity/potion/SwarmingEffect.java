package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

public class SwarmingEffect extends MobEffect {
    
    public SwarmingEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF0000); // Red color
    }
    
        @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel serverLevel) {
          
            for (int i = 0; i < 8; i++) {
                double angle = (entity.tickCount * 0.2 + i * Math.PI / 4) % (2 * Math.PI);
                double radius = 1.5 + Math.sin(entity.tickCount * 0.1) * 0.3;
                
                double x = entity.getX() + Math.cos(angle) * radius;
                double y = entity.getY() + 0.5 + Math.sin(entity.tickCount * 0.15) * 0.5;
                double z = entity.getZ() + Math.sin(angle) * radius;

  
                serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Apply effect every 5 ticks (4 times per second)
        return duration % 5 == 0;
    }
}
