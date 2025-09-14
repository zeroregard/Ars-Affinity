import { useState, useEffect } from 'react'
import { School, PerkTreeData, SchoolTreeData, NodePosition } from '../types/perkTree'
import { SCHOOL_SPACING, NODE_SPACING, TIER_SPACING } from '../constants/perkTree'

export const usePerkTreeData = (schools: School[]) => {
    const [schoolTrees, setSchoolTrees] = useState<SchoolTreeData[]>([])

    // Calculate node positions for a specific school
    const calculateNodePositions = (data: PerkTreeData, school: School, outwardX: number, outwardY: number) => {
        const positions = new Map<string, NodePosition>()
        
        // Calculate prerequisite depth for each node
        const nodeDepths = new Map<string, number>()
        
        // First, find all nodes with no prerequisites (depth 0)
        data.perks.forEach(node => {
            if (!node.prerequisites || node.prerequisites.length === 0) {
                nodeDepths.set(node.id, 0)
            }
        })
        
        // Then calculate depths for remaining nodes using BFS
        let changed = true
        while (changed) {
            changed = false
            data.perks.forEach(node => {
                if (!nodeDepths.has(node.id) && node.prerequisites) {
                    // Check if all prerequisites have their depths calculated
                    const prereqDepths = node.prerequisites
                        .map(prereqId => nodeDepths.get(prereqId))
                        .filter(depth => depth !== undefined)
                    
                    if (prereqDepths.length === node.prerequisites.length) {
                        // All prerequisites have depths, this node's depth is max + 1
                        const maxPrereqDepth = Math.max(...prereqDepths)
                        nodeDepths.set(node.id, maxPrereqDepth + 1)
                        changed = true
                    }
                }
            })
        }
        
        // Group nodes by their calculated depth
        const depthGroups = new Map<number, any[]>()
        data.perks.forEach(node => {
            const depth = nodeDepths.get(node.id) || 0
            if (!depthGroups.has(depth)) {
                depthGroups.set(depth, [])
            }
            depthGroups.get(depth)!.push(node)
        })

        // Sort nodes within each depth group
        depthGroups.forEach(nodes => {
            nodes.sort((a, b) => a.id.localeCompare(b.id))
        })

        // Calculate positions at a fixed radius from the global center
        // Position trees "outside" the circle (further from center than labels)
        const treeRadius = SCHOOL_SPACING * 1.5 // Distance from center for the tree (larger than label radius)
        
        // Calculate rotation direction based on school
        // Each school should grow in a specific direction
        const rotationAngle = getSchoolRotationAngle(school)
        
        depthGroups.forEach((nodes, depth) => {
            const startX = -(nodes.length - 1) * NODE_SPACING / 2
            nodes.forEach((node, index) => {
                // Position nodes in local coordinates first (before rotation)
                let localX = startX + index * NODE_SPACING
                let localY = depth * TIER_SPACING
                
                // Apply rotation to local coordinates
                const cos = Math.cos(rotationAngle)
                const sin = Math.sin(rotationAngle)
                const rotatedX = localX * cos - localY * sin
                const rotatedY = localX * sin + localY * cos
                
                // Calculate the tree center at the correct radius
                const treeCenterX = outwardX * treeRadius
                const treeCenterY = outwardY * treeRadius
                
                // Position nodes relative to the tree center
                positions.set(node.id, {
                    x: treeCenterX + rotatedX,
                    y: treeCenterY + rotatedY,
                    tier: depth, // Use calculated depth as tier
                    index,
                    school
                })
            })
        })

        return positions
    }

    // Get the rotation angle for each school so they grow in the correct direction
    const getSchoolRotationAngle = (school: School): number => {
        switch (school) {
            case 'manipulation': return -Math.PI / 2  // Grow right (was 0, now -90°)
            case 'fire': return -Math.PI / 4          // Grow bottom-right (was 45°, now -45°)
            case 'necromancy': return 0               // Grow down (was 90°, now 0°)
            case 'air': return Math.PI / 4            // Grow bottom-left (was 135°, now 45°)
            case 'conjuration': return Math.PI / 2    // Grow left (was 180°, now 90°)
            case 'water': return 3 * Math.PI / 4      // Grow top-left (was 225°, now 135°)
            case 'abjuration': return Math.PI         // Grow up (was 270°, now 180°)
            case 'earth': return -3 * Math.PI / 4     // Grow top-right (was 315°, now -135°)
            default: return 0
        }
    }

    // Load all perk tree data
    useEffect(() => {
        const loadAllPerkTrees = async () => {
            const treePromises = schools.map(async (school) => {
                try {
                    const response = await fetch(`/config/ars_affinity/perk_trees/${school}.json`)
                    if (response.ok) {
                        const data = await response.json()
                        return { school, data }
                    }
                } catch (error) {
                    console.error(`Error loading ${school} perk tree:`, error)
                }
                return null
            })

            const results = await Promise.all(treePromises)
            const validTrees = results.filter((tree): tree is { school: School, data: PerkTreeData } => tree !== null)
            
            const schoolTreesData = validTrees.map(({ school, data }, index) => {
                // Calculate circular positions (starting from right at 0 degrees)
                const angle = (index * Math.PI * 2) / validTrees.length
                const labelRadius = SCHOOL_SPACING * 1.2 // Radius for labels
                const centerX = Math.cos(angle) * labelRadius
                const centerY = Math.sin(angle) * labelRadius
                
                // Calculate outward direction vector for positioning trees
                const outwardX = Math.cos(angle)
                const outwardY = Math.sin(angle)
                
                // Calculate node positions for this school
                const nodePositions = calculateNodePositions(data, school, outwardX, outwardY)
                
                return {
                    school,
                    data,
                    nodePositions,
                    centerX,
                    centerY,
                    angle
                }
            })

            // For circular layout, the content center is at (0, 0)
            // To center content on screen: translate so content center appears at screen center
            const screenWidth = window.innerWidth
            const screenHeight = window.innerHeight
            const screenCenterX = screenWidth / 2 / 2.0 // DEFAULT_ZOOM
            const screenCenterY = screenHeight / 2 / 2.0 // DEFAULT_ZOOM
            
            // Translate to put content center at screen center
            const centerX = screenCenterX
            const centerY = screenCenterY
            
            setSchoolTrees(schoolTreesData)
        }

        loadAllPerkTrees()
    }, [])

    return schoolTrees
}
