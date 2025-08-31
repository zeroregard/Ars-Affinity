package com.github.ars_affinity.potion;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FireAffinityEffect extends MobEffect {
    
    public FireAffinityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFf06666); // Fire school color
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            SchoolAffinityProgress affinityProgress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (affinityProgress != null) {
                float currentAffinity = affinityProgress.getAffinity(SpellSchools.ELEMENTAL_FIRE);
                float newAffinity = Math.min(1.0f, currentAffinity + 0.10f); // Increase by 10%
                affinityProgress.setAffinity(SpellSchools.ELEMENTAL_FIRE, newAffinity);
                
                ArsAffinity.LOGGER.info("FIRE AFFINITY - Applied fire affinity potion effect for player: {} - affinity: {} -> {}", 
                    player.getName().getString(), currentAffinity, newAffinity);
            }
        }
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // Only apply once when the effect is first applied
        return duration == 1;
    }
    
    @Override
    public boolean isBeneficial() {
        return true;
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.fire_affinity";
    }
}