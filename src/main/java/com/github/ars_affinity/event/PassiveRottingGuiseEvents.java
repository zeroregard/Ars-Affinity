package com.github.ars_affinity.event;

import com.github.ars_affinity.ArsAffinity;
import com.github.ars_affinity.perk.AffinityPerk;
import com.github.ars_affinity.perk.AffinityPerkHelper;
import com.github.ars_affinity.perk.AffinityPerkType;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;

public class PassiveRottingGuiseEvents {
    
    @SubscribeEvent
    public static void onLivingChangeTarget(LivingChangeTargetEvent event) {
        if (!(event.getOriginalAboutToBeSetTarget() instanceof Player player)) return;
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (event.getEntity().level().isClientSide()) return;
        
        // Check if the mob is undead
        if (!mob.getType().is(EntityTypeTags.UNDEAD)) return;
        
        // Exclude the Wither (using entity type check instead)
        if (mob.getType() == EntityType.WITHER) return;
        
        // Check if player has the rotting guise perk
        AffinityPerkHelper.applyActivePerk(player, AffinityPerkType.PASSIVE_ROTTING_GUISE, AffinityPerk.SimplePerk.class, perk -> {
            // Make the undead mob ignore the player
            event.setNewAboutToBeSetTarget(null);
            
            ArsAffinity.LOGGER.debug("Player {} with PASSIVE_ROTTING_GUISE made {} ignore them", 
                player.getName().getString(), mob.getType().getDescription().getString());
        });
    }
}
