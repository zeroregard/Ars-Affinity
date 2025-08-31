package com.github.ars_affinity.potion.affinity_increase;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public abstract class AbstractAffinityIncreaseEffect extends MobEffect {
    
    private final SpellSchool targetSchool;
    private final String schoolName;
    
    protected AbstractAffinityIncreaseEffect(SpellSchool targetSchool, String schoolName, int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.targetSchool = targetSchool;
        this.schoolName = schoolName;
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            SchoolAffinityProgress affinityProgress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (affinityProgress != null) {
                float currentAffinity = affinityProgress.getAffinity(targetSchool);
                float newAffinity = Math.min(1.0f, currentAffinity + 0.10f); // Increase by 10%
                affinityProgress.setAffinity(targetSchool, newAffinity);
                
                ArsAffinity.LOGGER.info("{} AFFINITY - Applied {} affinity potion effect for player: {} - affinity: {} -> {}", 
                    schoolName.toUpperCase(), schoolName, player.getName().getString(), currentAffinity, newAffinity);
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
        return "effect.ars_affinity." + schoolName + "_affinity";
    }
}