package com.github.ars_affinity.event;

import com.hollingsworth.arsnouveau.api.spell.SpellSchool;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public class SchoolAffinityPointAllocatedEvent extends Event {
    private final Player player;
    private final SpellSchool school;
    private final int pointsGained;
    private final int totalPoints;
    
    public SchoolAffinityPointAllocatedEvent(Player player, SpellSchool school, int pointsGained, int totalPoints) {
        this.player = player;
        this.school = school;
        this.pointsGained = pointsGained;
        this.totalPoints = totalPoints;
    }
    
    public Player getPlayer() {
        return player;
    }
    
    public SpellSchool getSchool() {
        return school;
    }
    
    public int getPointsGained() {
        return pointsGained;
    }
    
    public int getTotalPoints() {
        return totalPoints;
    }
    
    public boolean hasPointsGained() {
        return pointsGained > 0;
    }
}
