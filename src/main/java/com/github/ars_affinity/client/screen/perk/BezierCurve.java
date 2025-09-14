package com.github.ars_affinity.client.screen.perk;

import java.util.ArrayList;
import java.util.List;

public class BezierCurve {
    private final List<Point> controlPoints;
    
    public BezierCurve(Point start, Point end) {
        this.controlPoints = new ArrayList<>();
        this.controlPoints.add(start);
        this.controlPoints.add(end);
    }
    
    public BezierCurve(Point start, Point control1, Point control2, Point end) {
        this.controlPoints = new ArrayList<>();
        this.controlPoints.add(start);
        this.controlPoints.add(control1);
        this.controlPoints.add(control2);
        this.controlPoints.add(end);
    }
    
    public List<Point> generatePoints(int segments) {
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i <= segments; i++) {
            float t = (float) i / segments;
            points.add(calculatePoint(t));
        }
        
        return points;
    }
    
    private Point calculatePoint(float t) {
        int n = controlPoints.size() - 1;
        float x = 0, y = 0;
        
        for (int i = 0; i <= n; i++) {
            float coefficient = binomialCoefficient(n, i) * (float) Math.pow(1 - t, n - i) * (float) Math.pow(t, i);
            x += coefficient * controlPoints.get(i).x;
            y += coefficient * controlPoints.get(i).y;
        }
        
        return new Point(x, y);
    }
    
    private int binomialCoefficient(int n, int k) {
        if (k > n - k) k = n - k;
        int result = 1;
        for (int i = 0; i < k; i++) {
            result = result * (n - i) / (i + 1);
        }
        return result;
    }
    
    public static class Point {
        public final float x, y;
        
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
        
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}
