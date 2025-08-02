package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.capability.SchoolAffinityProgressHelper;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

@EventBusSubscriber(modid = ArsAffinity.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class MobPacificationEvents {

    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        // Only handle when a mob is trying to target a player
        if (!(event.getNewAboutToBeSetTarget() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        
        LivingEntity mob = event.getEntity();
        EntityType<?> type = mob.getType();
        String mobId = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        if (shouldPacifyMob(player, mobId)) {;
            event.setCanceled(true);
        }
    }
    
    private static boolean shouldPacifyMob(Player player, String mobId) {
        var progress = SchoolAffinityProgressHelper.getAffinityProgress(player);
        if (progress == null) {
            return false;
        }
        
        // Use a boolean flag to track if we found a matching perk
        boolean[] shouldPacify = {false};
        
        // Check all schools for mob pacification perks
        for (var school : com.github.ars_affinity.school.SchoolRelationshipHelper.ALL_SCHOOLS) {
            int tier = progress.getTier(school);
            
            if (tier > 0) {
                AffinityPerkHelper.applyPerks(progress, tier, school, perk -> {
                    
                    if (perk.perk == AffinityPerkType.PASSIVE_MOB_PACIFICATION && perk instanceof AffinityPerk.EntityBasedPerk entityPerk) {
                        
                        if (entityPerk.entities != null && entityPerk.entities.contains(mobId)) {
                            shouldPacify[0] = true; // Found a matching perk
                        }
                    }
                });
            }
        }

        return shouldPacify[0];
    }
} 