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
        // Add some curve to make it look more organic
        int offsetX = (int) ((toY - fromY) * 0.3f);
        int offsetY = (int) ((fromX - toX) * 0.3f);
        
        BezierCurve.Point control1 = new BezierCurve.Point(fromX + offsetX, fromY + offsetY);
        BezierCurve.Point control2 = new BezierCurve.Point(toX + offsetX, toY + offsetY);
        
        return new BezierCurve(
            new BezierCurve.Point(fromX, fromY),
            control1,
            control2,
            new BezierCurve.Point(toX, toY)
        );
    }
    
    public List<BezierCurve.Point> getPathPoints(int segments) {
        return curve.generatePoints(segments);
    }
    
    public ConnectionType getType() { return type; }
    public ConnectionStyle getStyle() { return style; }
}
