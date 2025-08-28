package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.github.ars_affinity.registry.ModPotions;
import com.hollingsworth.arsnouveau.api.event.SpellCastEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class PassiveManipulationSicknessEvents {
    
    @SubscribeEvent
    public static void onSpellCast(SpellCastEvent event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        boolean hasManipulationSchool = event.context.getSpell().unsafeList().stream()
            .anyMatch(part -> part.spellSchools.contains(SpellSchools.MANIPULATION));
        if (!hasManipulationSchool) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) return;
        
        AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_MANIPULATION_SICKNESS, perk -> {
            if (perk instanceof AffinityPerk.ManipulationSicknessPerk sicknessPerk) {
                // Apply sickness effect
                player.addEffect(new MobEffectInstance(ModPotions.MANIPULATION_SICKNESS_EFFECT, sicknessPerk.duration, 0, false, true, true));
                
                // Apply hunger cost
                FoodData foodData = player.getFoodData();
                int currentFood = foodData.getFoodLevel();
                int newFood = Math.max(0, currentFood - sicknessPerk.hunger);
                foodData.setFoodLevel(newFood);
                
                ArsAffinity.LOGGER.debug("Player {} cast manipulation spell - PASSIVE_MANIPULATION_SICKNESS applied (duration: {}s, hunger cost: {})", 
                    player.getName().getString(), 
                    sicknessPerk.duration / 20, 
                    sicknessPerk.hunger);
            }
        });
    }
}
