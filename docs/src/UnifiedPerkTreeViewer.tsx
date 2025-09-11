import React, { useState, useEffect, useRef, useCallback } from 'react'
import PerkRenderer from './PerkRenderer'
import { titleCase } from './utils/string'

// School Icon Component
interface SchoolIconProps {
    school: School
    centerX: number
    centerY: number
    angle: number
}

const SchoolIcon: React.FC<SchoolIconProps> = ({ school, centerX, centerY, angle }) => {
    const iconRadius = 122
    const iconSize = 32
    const iconX = centerX + (iconRadius * Math.cos(angle)) - (iconSize / 2)
    const iconY = centerY + (iconRadius * Math.sin(angle)) - (iconSize / 2)

    return (
        <image
            href={`/icons/${school}_tooltip.png`}
            x={iconX}
            y={iconY}
            width={iconSize}
            height={iconSize}
            imageRendering="pixelated"
        />
    )
}

type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

interface PerkNode {
    id: string
    perk: string
    tier: number
    pointCost: number
    category: 'PASSIVE' | 'ACTIVE'
    level: number
    prerequisites?: string[]
}

interface PerkTreeData {
    perks: PerkNode[]
}

interface ViewportState {
    x: number
    y: number
    zoom: number
}

interface NodePosition {
    x: number
    y: number
    tier: number
    index: number
    school: School
}

interface SchoolTreeData {
    school: School
    data: PerkTreeData
    nodePositions: Map<string, NodePosition>
    centerX: number
    centerY: number
    angle: number
}

const NODE_SIZE = 32
const NODE_SPACING = 60
const TIER_SPACING = 80
const SCHOOL_SPACING = 64
const MIN_ZOOM = 0.5  // 100% (half of default)
const MAX_ZOOM = 4.0  // 400% (double of default)
const DEFAULT_ZOOM = 2.0  // 200% (new "100%")
const ZOOM_SPEED = 0.1

const schools: School[] = [
    'manipulation',  // Right (0°)
    'fire',          // Bottom-right (45°)
    'necromancy',    // Bottom (90°)
    'air',           // Bottom-left (135°)
    'conjuration',   // Left (180°)
    'water',         // Top-left (225°)
    'abjuration',    // Top (270°)
    'earth'          // Top-right (315°)
]

function UnifiedPerkTreeViewer() {
    const [schoolTrees, setSchoolTrees] = useState<SchoolTreeData[]>([])
    const [hoveredNode, setHoveredNode] = useState<{ node: PerkNode, school: School } | null>(null)
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })
    const [viewport, setViewport] = useState<ViewportState>({ x: 0, y: 0, zoom: DEFAULT_ZOOM })
    const [isDragging, setIsDragging] = useState(false)
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
    const containerRef = useRef<HTMLDivElement>(null)

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
            const screenCenterX = screenWidth / 2 / DEFAULT_ZOOM
            const screenCenterY = screenHeight / 2 / DEFAULT_ZOOM
            
            // Translate to put content center at screen center
            const centerX = screenCenterX
            const centerY = screenCenterY
            
            setViewport(prev => ({ ...prev, x: centerX, y: centerY, zoom: DEFAULT_ZOOM }))

            setSchoolTrees(schoolTreesData)
        }

        loadAllPerkTrees()
    }, [])

    // Calculate node positions for a specific school
    const calculateNodePositions = (data: PerkTreeData, school: School, outwardX: number, outwardY: number) => {
        const positions = new Map<string, NodePosition>()
        const tiers = new Map<number, PerkNode[]>()
        
        // Group nodes by tier
        data.perks.forEach(node => {
            if (!tiers.has(node.tier)) {
                tiers.set(node.tier, [])
            }
            tiers.get(node.tier)!.push(node)
        })

        // Sort nodes within each tier
        tiers.forEach(nodes => {
            nodes.sort((a, b) => a.id.localeCompare(b.id))
        })

        // Calculate positions at a fixed radius from the global center
        // Position trees "outside" the circle (further from center than labels)
        const treeRadius = SCHOOL_SPACING * 1.8 // Distance from center for the tree (larger than label radius)
        
        // Calculate rotation direction based on school
        // Each school should grow in a specific direction
        const rotationAngle = getSchoolRotationAngle(school)
        
        tiers.forEach((nodes, tier) => {
            const startX = -(nodes.length - 1) * NODE_SPACING / 2
            nodes.forEach((node, index) => {
                // Position nodes in local coordinates first (before rotation)
                let localX = startX + index * NODE_SPACING
                let localY = tier * TIER_SPACING
                
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
                    tier,
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

    // Handle mouse wheel zoom towards mouse cursor
    const handleWheel = useCallback((e: React.WheelEvent) => {
        e.preventDefault()
        const delta = e.deltaY > 0 ? -ZOOM_SPEED : ZOOM_SPEED
        const newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, viewport.zoom + delta))
        
        if (newZoom === viewport.zoom) return // No change in zoom
        
        // Get mouse position relative to the SVG
        const rect = e.currentTarget.getBoundingClientRect()
        const mouseX = e.clientX - rect.left
        const mouseY = e.clientY - rect.top
        
        // Convert mouse position to SVG coordinates
        const svgMouseX = (mouseX - viewport.x) / viewport.zoom
        const svgMouseY = (mouseY - viewport.y) / viewport.zoom
        
        // Calculate zoom factor (unused but kept for potential future use)
        // const zoomFactor = newZoom / viewport.zoom
        
        // Adjust viewport to zoom towards mouse position
        const newX = mouseX - svgMouseX * newZoom
        const newY = mouseY - svgMouseY * newZoom
        
        setViewport(prev => ({
            ...prev,
            x: newX,
            y: newY,
            zoom: newZoom
        }))
    }, [viewport])

    // Handle mouse down for dragging
    const handleMouseDown = useCallback((e: React.MouseEvent) => {
        // Start dragging if clicking on the background or SVG elements (but not on perk nodes)
        const target = e.target as Element
        const isPerkNode = target.classList.contains('perk-node') || target.closest('.perk-node')
        
        if (!isPerkNode) {
            setIsDragging(true)
            setDragStart({ x: e.clientX - viewport.x, y: e.clientY - viewport.y })
            e.preventDefault()
        }
    }, [viewport.x, viewport.y])

    // Handle mouse move for dragging
    const handleMouseMove = useCallback((e: React.MouseEvent) => {
        // Always update mouse position for tooltip positioning
        setMousePosition({ x: e.clientX, y: e.clientY })
        
        if (isDragging) {
            setViewport(prev => ({
                ...prev,
                x: e.clientX - dragStart.x,
                y: e.clientY - dragStart.y
            }))
            e.preventDefault()
        }
    }, [isDragging, dragStart])

    // Handle mouse up
    const handleMouseUp = useCallback(() => {
        setIsDragging(false)
    }, [])

    // Handle node click
    const handleNodeClick = useCallback((node: PerkNode, school: School) => {
        console.log('Clicked node:', node.id, 'in school:', school)
        // TODO: Implement perk allocation/deallocation
    }, [])

    // Render connections for a specific school
    const renderSchoolConnections = (schoolTree: SchoolTreeData) => {
        return schoolTree.data.perks.map(node => {
            return node.prerequisites?.map(prereqId => {
                const prereqPos = schoolTree.nodePositions.get(prereqId)
                const nodePos = schoolTree.nodePositions.get(node.id)
                
                if (!prereqPos || !nodePos) return null

                return (
                    <line
                        key={`${prereqId}-${node.id}`}
                        x1={prereqPos.x}
                        y1={prereqPos.y}
                        x2={nodePos.x}
                        y2={nodePos.y}
                        className="perk-connection"
                    />
                )
            }).filter(Boolean)
        }).flat()
    }

    // Render nodes for a specific school
    const renderSchoolNodes = (schoolTree: SchoolTreeData) => {
        return schoolTree.data.perks.map(node => {
            const position = schoolTree.nodePositions.get(node.id)
            if (!position) return null

            const isHovered = hoveredNode?.node.id === node.id && hoveredNode?.school === schoolTree.school
            const isAllocated = false // TODO: Check if perk is allocated
            const canAllocate = true // TODO: Check if perk can be allocated

            return (
                <g key={node.id}>
                    <circle
                        cx={position.x}
                        cy={position.y}
                        r={NODE_SIZE / 2}
                        fill={isAllocated ? '#4ade80' : canAllocate ? '#3b82f6' : '#6b7280'}
                        stroke={isHovered ? '#fbbf24' : '#374151'}
                        strokeWidth={isHovered ? 3 : 2}
                        className="perk-node"
                        onClick={() => handleNodeClick(node, schoolTree.school)}
                        onMouseEnter={() => setHoveredNode({ node, school: schoolTree.school })}
                        onMouseLeave={() => setHoveredNode(null)}
                    />
                    <text
                        x={position.x}
                        y={position.y + 3}
                        textAnchor="middle"
                        fill="white"
                        fontSize="10"
                        fontFamily="Minecraft, monospace"
                        pointerEvents="none"
                    >
                        {node.level}
                    </text>
                </g>
            )
        })
    }


    // Render tooltip
    const renderTooltip = () => {
        if (!hoveredNode) return null

        return (
            <div
                style={{
                    position: 'fixed',
                    left: `${mousePosition.x + 10}px`,
                    top: `${mousePosition.y - 10}px`,
                    zIndex: 1000,
                    pointerEvents: 'none'
                }}
            >
                <div className="perk-tooltip">
                    <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                        {formatPerkName(hoveredNode.node.id)} ({titleCase(hoveredNode.school)})
                    </div>
                    <div style={{ color: '#ccc', marginBottom: '8px' }}>
                        Cost: {hoveredNode.node.pointCost} points
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                        <PerkRenderer perk={getPerkDataForRenderer(hoveredNode.node)} />
                    </div>
                    {hoveredNode.node.prerequisites && hoveredNode.node.prerequisites.length > 0 && (
                        <div style={{ color: '#aaa', fontSize: '12px' }}>
                            Prerequisites: {hoveredNode.node.prerequisites.length}
                        </div>
                    )}
                </div>
            </div>
        )
    }

    const formatPerkName = (nodeId: string) => {
        const parts = nodeId.split('_')
        return parts.map(part => 
            part.match(/\d+/) ? toRomanNumeral(parseInt(part)) : titleCase(part)
        ).join(' ')
    }

    const toRomanNumeral = (num: number) => {
        const values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1]
        const symbols = ['M', 'CM', 'D', 'CD', 'C', 'XC', 'L', 'XL', 'X', 'IX', 'V', 'IV', 'I']
        let result = ''
        for (let i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                result += symbols[i]
                num -= values[i]
            }
        }
        return result
    }

    // Convert perk node data to the format expected by PerkRenderer
    const getPerkDataForRenderer = (node: PerkNode) => {
        const baseData = {
            perk: node.perk,
            isBuff: true, // Most perks are buffs
            amount: node.level, // Use level as the base amount
        }

        // Add specific values based on perk type
        switch (node.perk) {
            case 'PASSIVE_STONE_SKIN':
                return {
                    ...baseData,
                    time: 20 * node.level, // 1 second per level
                    cooldown: 20 * node.level, // 1 second cooldown per level
                }
            case 'ACTIVE_GROUND_SLAM':
                return {
                    ...baseData,
                    manaCost: 50 * node.level,
                    cooldown: 200 * node.level, // 10 seconds per level
                    damage: 5 * node.level,
                }
            case 'PASSIVE_GHOST_STEP':
                return {
                    ...baseData,
                    amount: 0.1 * node.level, // 10% per level
                    time: 20 * node.level, // 1 second per level
                    cooldown: 100 * node.level, // 5 seconds per level
                }
            case 'PASSIVE_SUMMONING_POWER':
                return {
                    ...baseData,
                    amount: 0.2 * node.level, // 20% per level
                }
            case 'PASSIVE_SUMMON_HEALTH':
                return {
                    ...baseData,
                    amount: 0.15 * node.level, // 15% per level
                }
            case 'PASSIVE_COLD_WALKER':
                return {
                    ...baseData,
                    amount: 0.1 * node.level, // 10% per level
                    time: 20 * node.level, // 1 second per level
                }
            case 'PASSIVE_HYDRATION':
                return {
                    ...baseData,
                    amount: 0.05 * node.level, // 5% per level
                }
            case 'ACTIVE_ICE_BLAST':
                return {
                    ...baseData,
                    manaCost: 40 * node.level,
                    cooldown: 300 * node.level, // 15 seconds per level
                    damage: 8 * node.level,
                    freezeTime: 20 * node.level, // 1 second freeze per level
                }
            case 'ACTIVE_CURSE_FIELD':
                return {
                    ...baseData,
                    manaCost: 60 * node.level,
                    cooldown: 400 * node.level, // 20 seconds per level
                    radius: 3 * node.level,
                    duration: 200 * node.level, // 10 seconds per level
                }
            case 'PASSIVE_LICH_FEAST':
                return {
                    ...baseData,
                    health: 0.1 * node.level, // 10% health per level
                    hunger: 0.05 * node.level, // 5% hunger per level
                }
            case 'PASSIVE_SUMMON_DEFENSE':
                return {
                    ...baseData,
                    amount: 0.25 * node.level, // 25% per level
                }
            default:
                // Default values for unknown perks
                return {
                    ...baseData,
                    time: 20 * node.level,
                    cooldown: 100 * node.level,
                }
        }
    }

    if (schoolTrees.length === 0) {
        return (
            <div style={{ 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center', 
                height: '100vh',
                color: 'white'
            }}>
                <div>Loading all perk trees...</div>
            </div>
        )
    }

    return (
        <div className={`perk-tree-container ${isDragging ? 'dragging' : ''}`}>
            {/* Header */}
            <div style={{ position: 'absolute', top: '16px', left: '16px', zIndex: 10 }}>
                <div className="zoom-display">
                    All Magic Schools - Perk Trees
                </div>
            </div>

            {/* Zoom controls */}
            <div className="zoom-controls">
                <button
                    onClick={() => {
                        const newZoom = Math.min(MAX_ZOOM, viewport.zoom + 0.2)
                        if (newZoom === viewport.zoom) return
                        
                        // Zoom towards screen center
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2
                        const screenCenterY = screenHeight / 2
                        
                        // Convert screen center to SVG coordinates
                        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
                        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
                        
                        // Adjust viewport to zoom towards screen center
                        const newX = screenCenterX - svgCenterX * newZoom
                        const newY = screenCenterY - svgCenterY * newZoom
                        
                        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
                    }}
                    className="zoom-button"
                >
                    +
                </button>
                <button
                    onClick={() => {
                        const newZoom = Math.max(MIN_ZOOM, viewport.zoom - 0.2)
                        if (newZoom === viewport.zoom) return
                        
                        // Zoom towards screen center
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2
                        const screenCenterY = screenHeight / 2
                        
                        // Convert screen center to SVG coordinates
                        const svgCenterX = (screenCenterX - viewport.x) / viewport.zoom
                        const svgCenterY = (screenCenterY - viewport.y) / viewport.zoom
                        
                        // Adjust viewport to zoom towards screen center
                        const newX = screenCenterX - svgCenterX * newZoom
                        const newY = screenCenterY - svgCenterY * newZoom
                        
                        setViewport(prev => ({ ...prev, x: newX, y: newY, zoom: newZoom }))
                    }}
                    className="zoom-button"
                >
                    -
                </button>
                <button
                    onClick={() => {
                        const schoolCount = schoolTrees.length
                        const cols = Math.min(4, schoolCount)
                        const rows = Math.ceil(schoolCount / 4)
                        const contentCenterX = -((cols - 1) * SCHOOL_SPACING) / 2
                        const contentCenterY = -((rows - 1) * SCHOOL_SPACING) / 2
                        
                        // To center content on screen: translate so content center appears at screen center
                        const screenWidth = window.innerWidth
                        const screenHeight = window.innerHeight
                        const screenCenterX = screenWidth / 2 / viewport.zoom
                        const screenCenterY = screenHeight / 2 / viewport.zoom
                        
                        // Translate to put content center at screen center
                        const centerX = screenCenterX - contentCenterX
                        const centerY = screenCenterY - contentCenterY
                        
                        setViewport(prev => ({ ...prev, x: centerX, y: centerY }))
                    }}
                    className="zoom-button"
                >
                    Center
                </button>
                <div className="zoom-display">
                    {Math.round((viewport.zoom / DEFAULT_ZOOM) * 100)}%
                </div>
            </div>

            {/* Main viewport */}
            <div
                ref={containerRef}
                style={{ 
                    width: '100%', 
                    height: '100%',
                    transform: `scale(${viewport.zoom})`,
                }}
                onWheel={handleWheel}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
                onMouseEnter={handleMouseMove}
            >
                <svg
                    className="perk-tree-svg"
                    width="100%"
                    height="100%"
                    viewBox="-2000 -2000 4000 4000"
                    onMouseDown={handleMouseDown}
                    onMouseMove={handleMouseMove}
                    onMouseUp={handleMouseUp}
                    onMouseLeave={handleMouseUp}
                >
                    {/* Grid background */}
                    <defs>
                        <pattern id="grid" width="200" height="200" patternUnits="userSpaceOnUse">
                            <path d="M 200 0 L 0 0 0 200" fill="none" stroke="#374151" strokeWidth="1" opacity="0.1"/>
                        </pattern>
                    </defs>
                    <rect x="-2000" y="-2000" width="4000" height="4000" fill="url(#grid)" />

                    {/* School icons */}
                    {schoolTrees.map(schoolTree => (
                        <SchoolIcon
                            key={`icon-${schoolTree.school}`}
                            school={schoolTree.school}
                            centerX={0}
                            centerY={0}
                            angle={schoolTree.angle}
                        />
                    ))}

                    {/* School trees - positioned at global coordinates */}
                    {schoolTrees.map(schoolTree => (
                        <g key={`tree-${schoolTree.school}`}>
                            {/* Connections */}
                            {renderSchoolConnections(schoolTree)}

                            {/* Nodes */}
                            {renderSchoolNodes(schoolTree)}
                        </g>
                    ))}
                </svg>
            </div>

            {/* Tooltip */}
            {renderTooltip()}
        </div>
    )
}

export default UnifiedPerkTreeViewer
