package com.github.ars_affinity.event;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class TierChangeEvent extends Event {
    private final Player player;
    private final SpellSchool school;
    private final int oldTier;
    private final int newTier;
    
    public TierChangeEvent(Player player, SpellSchool school, int oldTier, int newTier) {
        this.player = player;
        this.school = school;
        this.oldTier = oldTier;
        this.newTier = newTier;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public SpellSchool getSchool() {
        return school;
    }
    
    public int getOldTier() {
        return oldTier;
    }
    
    public int getNewTier() {
        return newTier;
    }
    
    public boolean hasTierChanged() {
        return oldTier != newTier;
    }
} 