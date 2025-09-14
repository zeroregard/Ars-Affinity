import React, { useState, useEffect, useRef, useCallback } from 'react'
import { PerkStringRenderer } from './utils/PerkStringRenderer'
import { titleCase } from './utils/string'
import { toRomanNumeral } from './utils/romanNumerals'
import { School, PerkNode, ViewportState, SchoolTreeData } from './types/perkTree'
import { NODE_SIZE, SCHOOL_SPACING, MIN_ZOOM, MAX_ZOOM, DEFAULT_ZOOM, ZOOM_SPEED, SCHOOL_COLORS, schools } from './constants/perkTree'
import SchoolIcon from './components/SchoolIcon'
import { usePerkTreeData } from './hooks/usePerkTreeData'




function UnifiedPerkTreeViewer() {
    const schoolTrees = usePerkTreeData(schools)
    const [hoveredNode, setHoveredNode] = useState<{ node: PerkNode, school: School } | null>(null)
    const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 })
    const [viewport, setViewport] = useState<ViewportState>({ x: 0, y: 0, zoom: DEFAULT_ZOOM })
    const [isDragging, setIsDragging] = useState(false)
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
    const containerRef = useRef<HTMLDivElement>(null)


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
