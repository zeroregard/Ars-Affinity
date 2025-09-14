import React, { useState, useEffect, useRef, useCallback } from 'react'
import { PerkStringRenderer } from './utils/PerkStringRenderer'
import { titleCase } from './utils/string'
import { toRomanNumeral } from './utils/romanNumerals'
import { School, PerkNode, PerkTreeData, ViewportState, NodePosition, SchoolTreeData, SchoolIconProps } from './types/perkTree'
import { NODE_SIZE, NODE_SPACING, TIER_SPACING, SCHOOL_SPACING, MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM, ZOOM_SPEED, SCHOOL_COLORS, schools } from './constants/perkTree'

// School Icon Component

const SchoolIcon: React.FC<SchoolIconProps> = ({ school, centerX, centerY, angle }) => {
    const iconRadius = 122
    const iconSize = 32
    const circleRadius = 20
    const iconX = centerX + (iconRadius * Math.cos(angle)) - (iconSize / 2)
    const iconY = centerY + (iconRadius * Math.sin(angle)) - (iconSize / 2)
    const circleX = centerX + (iconRadius * Math.cos(angle))
    const circleY = centerY + (iconRadius * Math.sin(angle))

    return (
        <g>
            {/* Background circle */}
            <circle
                cx={circleX}
                cy={circleY}
                r={circleRadius}
                fill="rgba(0, 0, 0, 1)"
                stroke="rgba(255, 255, 255, 0.3)"
                strokeWidth="2"
            />
            {/* School icon */}
            <image
                href={`/icons/${school}_tooltip.png`}
                x={iconX}
                y={iconY}
                width={iconSize}
                height={iconSize}
                imageRendering="pixelated"
            />
        </g>
    )
}



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
        const depthGroups = new Map<number, PerkNode[]>()
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

    // Handle mouse wheel zoom towards mouse cursor
    const handleWheel = useCallback((e: WheelEvent) => {
        e.preventDefault()
        const delta = e.deltaY > 0 ? -ZOOM_SPEED : ZOOM_SPEED
        const newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, viewport.zoom + delta))
        
        if (newZoom === viewport.zoom) return // No change in zoom
        
        // Get mouse position relative to the container
        const rect = containerRef.current?.getBoundingClientRect()
        if (!rect) return
        
        const mouseX = e.clientX - rect.left
        const mouseY = e.clientY - rect.top
        
        // Convert mouse position to SVG coordinates
        const svgMouseX = (mouseX - viewport.x) / viewport.zoom
        const svgMouseY = (mouseY - viewport.y) / viewport.zoom
        
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

    // Add wheel event listener with passive: false
    useEffect(() => {
        const container = containerRef.current
        if (!container) return

        container.addEventListener('wheel', handleWheel, { passive: false })
        
        return () => {
            container.removeEventListener('wheel', handleWheel)
        }
    }, [handleWheel])

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
        const schoolColors = SCHOOL_COLORS[schoolTree.school]
        
        return schoolTree.data.perks.map(node => {
            const position = schoolTree.nodePositions.get(node.id)
            if (!position) return null

            const isHovered = hoveredNode?.node.id === node.id && hoveredNode?.school === schoolTree.school
            const isAllocated = false // TODO: Check if perk is allocated
            const canAllocate = true // TODO: Check if perk can be allocated

            // Choose color based on state
            let fillColor: string
            if (isAllocated) {
                fillColor = schoolColors.allocated
            } else if (canAllocate) {
                fillColor = 'rgba(0, 0, 0, 1)' // Completely black background
            } else {
                fillColor = 'rgba(0, 0, 0, 1)' // Completely black background for unavailable
            }

            // Choose stroke color
            const strokeColor = isHovered ? schoolColors.hover : '#374151'

            // Calculate icon size and position
            const iconSize = NODE_SIZE * 0.6 // 60% of node size
            const iconX = position.x - iconSize / 2
            const iconY = position.y - iconSize / 2

            // Calculate Roman numeral position (bottom right of node)
            const romanNumeral = toRomanNumeral(node.tier)
            const textSize = NODE_SIZE * 0.3 // 30% of node size
            const textX = position.x + (NODE_SIZE / 2) - textSize * 0.3 // Right side with small padding
            const textY = position.y + (NODE_SIZE / 2) - textSize * 0.2 // Bottom side with small padding

            return (
                <g key={node.id}>
                    {/* Double outline for ACTIVE_ perks */}
                    {node.perk.startsWith('ACTIVE_') && (
                        <circle
                            cx={position.x}
                            cy={position.y}
                            r={NODE_SIZE / 2 + 4}
                            fill="none"
                            stroke={strokeColor}
                            strokeWidth={isHovered ? 4 : 3}
                            className="perk-node-outline"
                        />
                    )}
                    <circle
                        cx={position.x}
                        cy={position.y}
                        r={NODE_SIZE / 2}
                        fill={fillColor}
                        stroke={strokeColor}
                        strokeWidth={isHovered ? 3 : 2}
                        className="perk-node"
                        onClick={() => handleNodeClick(node, schoolTree.school)}
                        onMouseEnter={() => setHoveredNode({ node, school: schoolTree.school })}
                        onMouseLeave={() => setHoveredNode(null)}
                    />
                    {/* Node icon */}
                    <image
                        href={`/icons/${node.perk.toLowerCase()}.png`}
                        x={iconX}
                        y={iconY}
                        width={iconSize}
                        height={iconSize}
                        imageRendering="pixelated"
                        className="perk-node-icon"
                        style={{ pointerEvents: 'none' }}
                    />
                    {/* Roman numeral for non-active abilities */}
                    {node.category !== 'ACTIVE' && (
                        <text
                            x={textX}
                            y={textY}
                            fontSize={textSize}
                            fill="white"
                            textAnchor="end"
                            dominantBaseline="baseline"
                            style={{ 
                                pointerEvents: 'none',
                                fontWeight: 'bold',
                                textShadow: '1px 1px 2px rgba(0,0,0,0.8)'
                            }}
                        >
                            {romanNumeral}
                        </text>
                    )}
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
                        {formatPerkName(hoveredNode.node.id)}
                    </div>
                    <div style={{ color: '#ccc', marginBottom: '8px' }}>
                        Cost: {hoveredNode.node.pointCost} points
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                        <PerkStringRenderer perk={getPerkDataForRenderer(hoveredNode.node)} />
                    </div>
                </div>
            </div>
        )
    }

    const formatPerkName = (nodeId: string) => {
        const parts = nodeId.split('_')
        // Skip the first part (school name) to avoid duplication
        return parts.slice(1).map(part => 
            part.match(/\d+/) ? toRomanNumeral(parseInt(part)) : titleCase(part)
        ).join(' ')
    }


    // Convert perk node data to the format expected by PerkRenderer
    const getPerkDataForRenderer = (node: PerkNode) => {
        const baseData: any = {
            perk: node.perk,
            isBuff: true, // Most perks are buffs
        }

        // Only include properties that actually exist in the PerkNode
        if (node.amount !== undefined) baseData.amount = node.amount
        if (node.time !== undefined) baseData.time = node.time
        if (node.cooldown !== undefined) baseData.cooldown = node.cooldown
        if (node.manaCost !== undefined) baseData.manaCost = node.manaCost
        if (node.damage !== undefined) baseData.damage = node.damage
        if (node.freezeTime !== undefined) baseData.freezeTime = node.freezeTime
        if (node.radius !== undefined) baseData.radius = node.radius
        if (node.dashLength !== undefined) baseData.dashLength = node.dashLength
        if (node.dashDuration !== undefined) baseData.dashDuration = node.dashDuration
        if (node.health !== undefined) baseData.health = node.health
        if (node.hunger !== undefined) baseData.hunger = node.hunger

        return baseData
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
            {/* Background */}
            <div className="perk-tree-background"></div>
            
            {/* Header */}
            <div style={{ position: 'absolute', top: '16px', left: '16px', zIndex: 10 }}>
                <div className="zoom-display">
                    Ars Affinity - Perk Trees
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
                }}
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
                    <g transform={`translate(${viewport.x / viewport.zoom}, ${viewport.y / viewport.zoom}) scale(${viewport.zoom})`}>
                    {/* Grid background and gradients */}
                    <defs>
                        <pattern id="grid" width="200" height="200" patternUnits="userSpaceOnUse">
                            <path d="M 200 0 L 0 0 0 200" fill="none" stroke="#374151" strokeWidth="1" opacity="0.1"/>
                        </pattern>
                        
                        {/* Necromancy (Anima) gradient */}
                        <linearGradient id="necromancy-gradient" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#8b0606"/>
                            <stop offset="100%" stopColor="#282828"/>
                        </linearGradient>
                        
                        {/* Necromancy (Anima) hover gradient */}
                        <linearGradient id="necromancy-gradient-hover" x1="0%" y1="0%" x2="100%" y2="100%">
                            <stop offset="0%" stopColor="#a00a0a"/>
                            <stop offset="100%" stopColor="#404040"/>
                        </linearGradient>
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
                    </g>
                </svg>
            </div>

            {/* Tooltip */}
            {renderTooltip()}
        </div>
    )
}

export default UnifiedPerkTreeViewer
