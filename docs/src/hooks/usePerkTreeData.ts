import { useState, useEffect } from 'react'
import { School, PerkTreeData, SchoolTreeData, NodePosition } from '../types/perkTree'
import { SCHOOL_SPACING, NODE_SPACING, TIER_SPACING } from '../constants/perkTree'

export const usePerkTreeData = (schools: School[]) => {
    const [schoolTrees, setSchoolTrees] = useState<SchoolTreeData[]>([])

    const calculateNodePositions = (data: PerkTreeData, school: School, outwardX: number, outwardY: number) => {
        const positions = new Map<string, NodePosition>()
        
        const nodeDepths = new Map<string, number>()
        
        data.perks.forEach(node => {
            if (!node.prerequisites || node.prerequisites.length === 0) {
                nodeDepths.set(node.id, 0)
            }
        })
        
        let changed = true
        while (changed) {
            changed = false
            data.perks.forEach(node => {
                if (!nodeDepths.has(node.id) && node.prerequisites) {
                    const prereqDepths = node.prerequisites
                        .map(prereqId => nodeDepths.get(prereqId))
                        .filter(depth => depth !== undefined)
                    
                    if (prereqDepths.length === node.prerequisites.length) {
                        const maxPrereqDepth = Math.max(...prereqDepths)
                        nodeDepths.set(node.id, maxPrereqDepth + 1)
                        changed = true
                    }
                }
            })
        }
        
        const depthGroups = new Map<number, any[]>()
        data.perks.forEach(node => {
            const depth = nodeDepths.get(node.id) || 0
            if (!depthGroups.has(depth)) {
                depthGroups.set(depth, [])
            }
            depthGroups.get(depth)!.push(node)
        })

        depthGroups.forEach(nodes => {
            nodes.sort((a, b) => a.id.localeCompare(b.id))
        })

        const treeRadius = SCHOOL_SPACING * 1.5
        
        const rotationAngle = getSchoolRotationAngle(school)
        
        depthGroups.forEach((nodes, depth) => {
            const startX = -(nodes.length - 1) * NODE_SPACING / 2
            nodes.forEach((node, index) => {
                let localX = startX + index * NODE_SPACING
                let localY = depth * TIER_SPACING
                
                const cos = Math.cos(rotationAngle)
                const sin = Math.sin(rotationAngle)
                const rotatedX = localX * cos - localY * sin
                const rotatedY = localX * sin + localY * cos
                
                const treeCenterX = outwardX * treeRadius
                const treeCenterY = outwardY * treeRadius
                
                positions.set(node.id, {
                    x: treeCenterX + rotatedX,
                    y: treeCenterY + rotatedY,
                    tier: depth,
                    index,
                    school
                })
            })
        })

        return positions
    }

    const getSchoolRotationAngle = (school: School): number => {
        switch (school) {
            case 'manipulation': return -Math.PI / 2
            case 'fire': return -Math.PI / 4
            case 'necromancy': return 0
            case 'air': return Math.PI / 4
            case 'conjuration': return Math.PI / 2
            case 'water': return 3 * Math.PI / 4
            case 'abjuration': return Math.PI
            case 'earth': return -3 * Math.PI / 4
            default: return 0
        }
    }

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
                const angle = (index * Math.PI * 2) / validTrees.length
                const labelRadius = SCHOOL_SPACING * 1.2
                const centerX = Math.cos(angle) * labelRadius
                const centerY = Math.sin(angle) * labelRadius
                
                const outwardX = Math.cos(angle)
                const outwardY = Math.sin(angle)
                
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

            const screenWidth = window.innerWidth
            const screenHeight = window.innerHeight
            const screenCenterX = screenWidth / 2 / 2.0
            const screenCenterY = screenHeight / 2 / 2.0
            
            const centerX = screenCenterX
            const centerY = screenCenterY
            
            setSchoolTrees(schoolTreesData)
        }

        loadAllPerkTrees()
    }, [])

    return schoolTrees
}
