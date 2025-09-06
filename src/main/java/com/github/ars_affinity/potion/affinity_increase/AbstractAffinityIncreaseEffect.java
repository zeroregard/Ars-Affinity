package com.github.ars_affinity.potion.affinity_increase;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgress;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.config.ArsAffinityConfig;
import com.github.ars_affinity.registry.ModPotions;
import com.github.ars_affinity.school.SchoolRelationshipHelper;
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
    public void onEffectAdded(LivingEntity livingEntity, int amplifier) {
        if (livingEntity instanceof Player player) {
            // Check if the player has the cooldown effect
            if (ModPotions.hasAffinityCooldown(player)) {
                ArsAffinity.LOGGER.info("{} AFFINITY - Blocked {} affinity potion effect for player: {} due to cooldown", 
                    schoolName.toUpperCase(), schoolName, player.getName().getString());
                return;
            }
            
            SchoolAffinityProgress affinityProgress = SchoolAffinityProgressHelper.getAffinityProgress(player);
            if (affinityProgress != null) {
                // Calculate the increase amount
                float increaseAmount = ArsAffinityConfig.AFFINITY_POTION_INCREASE_PERCENTAGE.get().floatValue();
                
                // Create changes map with proper opposing school logic for potions
                java.util.Map<SpellSchool, Float> changes = calculatePotionAffinityChanges(targetSchool, increaseAmount);
                
                // Apply changes with proper opposing school logic
                affinityProgress.applyChanges(changes);
                
                ArsAffinity.LOGGER.info("{} AFFINITY - Applied {} affinity potion effect for player: {} - increased by {}%", 
                    schoolName.toUpperCase(), schoolName, player.getName().getString(), increaseAmount * 100.0f);
                
                int cooldownDurationTicks = ArsAffinityConfig.AFFINITY_CONSUMABLE_COOLDOWN_DURATION.get() * 20; // Convert seconds to ticks
                MobEffectInstance cooldownEffect = new MobEffectInstance(ModPotions.AFFINITY_CONSUMABLE_COOLDOWN_EFFECT, cooldownDurationTicks, 0, false, false, false);
                player.addEffect(cooldownEffect);
                
                ArsAffinity.LOGGER.info("{} AFFINITY - Applied cooldown effect to player: {} for {} seconds", 
                    schoolName.toUpperCase(), player.getName().getString(), ArsAffinityConfig.AFFINITY_CONSUMABLE_COOLDOWN_DURATION.get());
            }
        }
    }
    
    
    @Override
    public boolean isBeneficial() {
        return true;
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity." + schoolName + "_affinity";
    }
    
    private java.util.Map<SpellSchool, Float> calculatePotionAffinityChanges(SpellSchool targetSchool, float increaseAmount) {
        java.util.Map<SpellSchool, Float> changes = new java.util.HashMap<>();
        
        changes.put(targetSchool, increaseAmount);
        
        SpellSchool oppositeSchool = SchoolRelationshipHelper.getOppositeSchool(targetSchool);
        float opposingPenaltyPercentage = ArsAffinityConfig.OPPOSING_SCHOOL_PENALTY_PERCENTAGE.get().floatValue();
        

        int otherSchoolsCount = 0;
        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            if (school != targetSchool && school != oppositeSchool) {
                otherSchoolsCount++;
            }
        }
        

        for (SpellSchool school : SchoolRelationshipHelper.ALL_SCHOOLS) {
            if (school == targetSchool) {
                continue; // Skip target school
            }
            
            float penalty;
            if (school == oppositeSchool) {
                // Opposing school gets the configured penalty percentage
                penalty = -increaseAmount * opposingPenaltyPercentage;
            } else {
                // Other schools split the remaining penalty equally
                float remainingPenaltyPercentage = 1.0f - opposingPenaltyPercentage;
                penalty = -(increaseAmount * remainingPenaltyPercentage) / otherSchoolsCount;
            }
            
            changes.put(school, penalty);
        }
        
        return changes;
    }
}