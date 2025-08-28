package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import com.hollingsworth.arsnouveau.api.event.SpellDamageEvent;
import com.hollingsworth.arsnouveau.api.spell.SpellSchools;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MobPacificationEvents {
    
    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent.Post event) {
        if (!(event.context.getCaster() instanceof com.hollingsworth.arsnouveau.api.spell.wrapped_caster.PlayerCaster playerCaster)) return;
        var player = playerCaster.player;
        if (player.level().isClientSide()) return;
        
        // Only trigger if damage was actually dealt
        if (event.damage <= 0) return;
        
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress != null) {
            AffinityPerkHelper.applyActivePerk(progress, AffinityPerkType.PASSIVE_MOB_PACIFICATION, perk -> {
                if (perk instanceof AffinityPerk.EntityBasedPerk entityPerk) {
                    // Check if the target entity is in the pacification list
                    if (event.target instanceof LivingEntity targetEntity) {
                        String targetId = targetEntity.getType().toString();
                        if (entityPerk.entities.contains(targetId)) {
                            ArsAffinity.LOGGER.info("Player {} dealt {} damage to {} - PASSIVE_MOB_PACIFICATION active", 
                                player.getName().getString(), event.damage, targetId);
                        }
                    }
                }
            });
        }
    }
} 