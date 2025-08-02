package com.github.ars_affinity.capability;

import com.github.ars_affinity.ArsAffinity;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

public class SchoolAffinityProgressHelper {
    
    private SchoolAffinityProgressHelper() {
        // Utility class, prevent instantiation
    }
    
    public static SchoolAffinityProgress getAffinityProgress(Player player) {
        if (player == null) {
            ArsAffinity.LOGGER.warn("Attempted to get affinity progress for null player");
            return null;
        }
        
        // Use our manual caching system directly instead of the capability system
        return SchoolAffinityProgressProvider.getAffinityProgress(player);
    }
    
    public static SchoolAffinityProgress applyAffinityChanges(Player player, Map<SpellSchool, Float> changes) {
        SchoolAffinityProgress progress = getAffinityProgress(player);
        if (progress != null) {
            progress.applyChanges(changes);
            ArsAffinity.LOGGER.debug("Applied affinity changes for player: {}", player.getName().getString());
            return progress;
        } else {
            ArsAffinity.LOGGER.warn("Could not get affinity progress for player: {}", player.getName().getString());
            return null;
        }
    }
    
    public static float getAffinity(Player player, SpellSchool school) {
        SchoolAffinityProgress progress = getAffinityProgress(player);
        if (progress != null) {
            return progress.getAffinity(school);
        }
        return 0.0f;
    }
    
    public static SpellSchool getPrimarySchool(Player player) {
        SchoolAffinityProgress progress = getAffinityProgress(player);
        if (progress != null) {
            return progress.getPrimarySchool();
        }
        return null;
    }
    
    public static int getTier(Player player, SpellSchool school) {
        SchoolAffinityProgress progress = getAffinityProgress(player);
        if (progress != null) {
            return progress.getTier(school);
        }
        return 0;
    }
} 