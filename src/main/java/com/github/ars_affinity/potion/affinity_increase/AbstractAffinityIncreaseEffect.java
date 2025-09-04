package com.github.ars_affinity.potion.affinity_increase;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
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
            // Check if the player has the cooldown effect
            if (ModPotions.hasAffinityCooldown(player)) {
                ArsAffinity.LOGGER.info("{} AFFINITY - Blocked {} affinity potion effect for player: {} due to cooldown", 
                    schoolName.toUpperCase(), schoolName, player.getName().getString());
                return true;
            }
            
            SchoolAffinityProgress affinityProgress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (affinityProgress != null) {
                float currentAffinity = affinityProgress.getAffinity(targetSchool);
                float increaseAmount = ArsAffinityConfig.AFFINITY_POTION_INCREASE_PERCENTAGE.get().floatValue();
                float newAffinity = Math.min(1.0f, currentAffinity + increaseAmount);
                affinityProgress.setAffinity(targetSchool, newAffinity);
                
                ArsAffinity.LOGGER.info("{} AFFINITY - Applied {} affinity potion effect for player: {} - affinity: {} -> {}", 
                    schoolName.toUpperCase(), schoolName, player.getName().getString(), currentAffinity, newAffinity);
                
                int cooldownDurationTicks = ArsAffinityConfig.AFFINITY_CONSUMABLE_COOLDOWN_DURATION.get() * 20; // Convert seconds to ticks
                MobEffectInstance cooldownEffect = new MobEffectInstance(ModPotions.AFFINITY_CONSUMABLE_COOLDOWN_EFFECT, cooldownDurationTicks, 0, false, true, false);
                player.addEffect(cooldownEffect);
                
                ArsAffinity.LOGGER.info("{} AFFINITY - Applied cooldown effect to player: {} for {} seconds", 
                    schoolName.toUpperCase(), player.getName().getString(), ArsAffinityConfig.AFFINITY_CONSUMABLE_COOLDOWN_DURATION.get());
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