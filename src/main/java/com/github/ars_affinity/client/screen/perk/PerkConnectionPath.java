package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;
import com.hollingsworth.arsnouveau.api.spell.SpellSchool;

import java.util.List;

public class PerkConnectionPath {
    private final BezierCurve curve;
    private final ConnectionType type;
    private final ConnectionStyle style;
    
    public PerkConnectionPath(PerkNode from, PerkNode to, SpellSchool school, int fromX, int fromY, int toX, int toY) {
        this.type = determineConnectionType(from, to);
        this.style = ConnectionStyle.forType(type, school);
        this.curve = createCurve(fromX, fromY, toX, toY);
    }
    
    private ConnectionType determineConnectionType(PerkNode from, PerkNode to) {
        // For now, we'll use a simple heuristic
        // In a full implementation, this would check player state, prerequisites, etc.
        if (from.getTier() < to.getTier()) {
            return ConnectionType.PREREQUISITE;
        } else if (from.getTier() == to.getTier()) {
            return ConnectionType.AVAILABLE;
        } else {
            return ConnectionType.LOCKED;
        }
    }
    
    private BezierCurve createCurve(int fromX, int fromY, int toX, int toY) {
        // Create straight line by using only start and end points
        return new BezierCurve(
            new BezierCurve.Point(fromX, fromY),
            new BezierCurve.Point(toX, toY)
        );
    }
    
    public List<BezierCurve.Point> getPathPoints(int segments) {
        return curve.generatePoints(segments);
    }
    
    public ConnectionType getType() { return type; }
    public ConnectionStyle getStyle() { return style; }
}
