package com.github.ars_affinity.potion;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class AffinityConsumableCooldownEffect extends MobEffect {
    
    public AffinityConsumableCooldownEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFF808080); // Gray color
    }
    
    @Override
    public String getDescriptionId() {
        return "effect.ars_affinity.affinity_consumable_cooldown";
    }
}
