package com.github.ars_affinity.client.screen.perk;

import com.github.ars_affinity.perk.PerkNode;

import java.util.*;

public class PerkTreeLayout {
    private static final int NODE_SIZE = 24;
    private static final int COLUMN_SPACING = 80; // Horizontal spacing between columns
    private static final int VERTICAL_SPACING = 40; // Vertical spacing between nodes in same column
    
    private final Map<String, PerkNode> schoolPerks;
    private final Map<String, NodePosition> nodePositions;
    private final Map<Integer, List<PerkNode>> perksByTier; // Keep for compatibility
    
    public static class NodePosition {
        public final int x, y;
        public final int column, index;
        
        public NodePosition(int x, int y, int column, int index) {
            this.x = x;
            this.y = y;
            this.column = column;
            this.index = index;
        }
    }
    
    public PerkTreeLayout(Map<String, PerkNode> schoolPerks) {
        this.schoolPerks = schoolPerks;
        this.nodePositions = calculateDependencyBasedPositions();
        this.perksByTier = groupPerksByTier(); // Keep for compatibility
    }
    
    public Map<Integer, List<PerkNode>> getPerksByTier() {
        return perksByTier;
    }
    
    public int getNodeX(PerkNode node, int startX) {
        NodePosition pos = nodePositions.get(node.getId());
        return pos != null ? startX + pos.x : startX;
    }
    
    public int getNodeY(PerkNode node, int startY) {
        NodePosition pos = nodePositions.get(node.getId());
        return pos != null ? startY + pos.y : startY;
    }
    
    public int getStartX(int width, int scrollX) {
        // Center around the root node (column 0)
        return width / 2 + scrollX;
    }
    
    public int getStartY(int scrollY) {
        return 50 + scrollY;
    }
    
    public int getTreeMinX() {
        return nodePositions.values().stream()
            .mapToInt(pos -> pos.x)
            .min()
            .orElse(0) - NODE_SIZE / 2;
    }
    
    public int getTreeMaxX() {
        return nodePositions.values().stream()
            .mapToInt(pos -> pos.x)
            .max()
            .orElse(0) + NODE_SIZE / 2;
    }
    
    public int getTreeMinY() {
        return nodePositions.values().stream()
            .mapToInt(pos -> pos.y)
            .min()
            .orElse(0) - NODE_SIZE / 2;
    }
    
    public int getTreeMaxY() {
        return nodePositions.values().stream()
            .mapToInt(pos -> pos.y)
            .max()
            .orElse(0) + NODE_SIZE / 2;
    }
    
    public int getTreeWidth() {
        return getTreeMaxX() - getTreeMinX();
    }
    
    public int getTreeHeight() {
        return getTreeMaxY() - getTreeMinY();
    }
    
    public PerkNode getNodeAt(int mouseX, int mouseY, int startX, int startY) {
        for (Map.Entry<String, NodePosition> entry : nodePositions.entrySet()) {
            NodePosition pos = entry.getValue();
            int nodeX = startX + pos.x;
            int nodeY = startY + pos.y;
            
            if (mouseX >= nodeX && mouseX < nodeX + NODE_SIZE && 
                mouseY >= nodeY && mouseY < nodeY + NODE_SIZE) {
                return schoolPerks.get(entry.getKey());
            }
        }
        return null;
    }
    
    private Map<String, NodePosition> calculateDependencyBasedPositions() {
        Map<String, NodePosition> positions = new HashMap<>();
        Map<Integer, List<PerkNode>> columns = new HashMap<>();
        
        // Calculate dependency depth for each node
        Map<String, Integer> dependencyDepths = calculateDependencyDepths();
        
        // Group nodes by dependency depth (column)
        for (PerkNode node : schoolPerks.values()) {
            int column = dependencyDepths.get(node.getId());
            columns.computeIfAbsent(column, k -> new ArrayList<>()).add(node);
        }
        
        // Sort nodes within each column
        for (List<PerkNode> columnNodes : columns.values()) {
            columnNodes.sort(Comparator.comparing(PerkNode::getId));
        }
        
        // Calculate positions for each column
        for (Map.Entry<Integer, List<PerkNode>> columnEntry : columns.entrySet()) {
            int column = columnEntry.getKey();
            List<PerkNode> nodes = columnEntry.getValue();
            
            // Center nodes vertically within the column
            int startY = -(nodes.size() - 1) * VERTICAL_SPACING / 2;
            
            for (int i = 0; i < nodes.size(); i++) {
                PerkNode node = nodes.get(i);
                int x = column * COLUMN_SPACING;
                int y = startY + i * VERTICAL_SPACING;
                
                positions.put(node.getId(), new NodePosition(x, y, column, i));
            }
        }
        
        return positions;
    }
    
    private Map<String, Integer> calculateDependencyDepths() {
        Map<String, Integer> depths = new HashMap<>();
        Set<String> visited = new HashSet<>();
        
        // Find root nodes (no prerequisites)
        List<PerkNode> rootNodes = new ArrayList<>();
        for (PerkNode node : schoolPerks.values()) {
            if (node.getPrerequisites().isEmpty()) {
                rootNodes.add(node);
            }
        }
        
        // BFS to calculate dependency depths
        Queue<PerkNode> queue = new LinkedList<>();
        for (PerkNode root : rootNodes) {
            depths.put(root.getId(), 0);
            queue.offer(root);
        }
        
        while (!queue.isEmpty()) {
            PerkNode current = queue.poll();
            int currentDepth = depths.get(current.getId());
            
            // Find all nodes that depend on this node
            for (PerkNode node : schoolPerks.values()) {
                if (node.getPrerequisites().contains(current.getId()) && !visited.contains(node.getId())) {
                    depths.put(node.getId(), currentDepth + 1);
                    visited.add(node.getId());
                    queue.offer(node);
                }
            }
        }
        
        // Handle any remaining nodes (circular dependencies or orphaned nodes)
        for (PerkNode node : schoolPerks.values()) {
            if (!depths.containsKey(node.getId())) {
                depths.put(node.getId(), 0); // Place orphaned nodes in column 0
            }
        }
        
        return depths;
    }
    
    private Map<Integer, List<PerkNode>> groupPerksByTier() {
        Map<Integer, List<PerkNode>> tiers = new HashMap<>();
        
        for (PerkNode node : schoolPerks.values()) {
            tiers.computeIfAbsent(node.getTier(), k -> new ArrayList<>()).add(node);
        }
        
        for (List<PerkNode> tier : tiers.values()) {
            tier.sort(Comparator.comparing(PerkNode::getId));
        }
        
        return tiers;
    }
}

