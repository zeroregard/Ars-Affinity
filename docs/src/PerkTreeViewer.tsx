import React, { useState, useEffect, useRef, useCallback } from 'react'
import { PerkStringRenderer } from './utils/PerkStringRenderer'
import { titleCase } from './utils/string'

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

interface PerkTreeViewerProps {
    school: School
    onClose: () => void
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
}

const NODE_SIZE = 48
const NODE_SPACING = 80
const TIER_SPACING = 120
const MIN_ZOOM = 0.5
const MAX_ZOOM = 2.0
const ZOOM_SPEED = 0.1

function PerkTreeViewer({ school, onClose }: PerkTreeViewerProps) {
    const [perkTreeData, setPerkTreeData] = useState<PerkTreeData | null>(null)
    const [hoveredNode, setHoveredNode] = useState<PerkNode | null>(null)
    const [viewport, setViewport] = useState<ViewportState>({ x: 0, y: 0, zoom: 1.0 })
    const [isDragging, setIsDragging] = useState(false)
    const [dragStart, setDragStart] = useState({ x: 0, y: 0 })
    const dragSensitivity = 1.5 // Increase this value to make dragging more sensitive
    const [nodePositions, setNodePositions] = useState<Map<string, NodePosition>>(new Map())
    const containerRef = useRef<HTMLDivElement>(null)

    // Load perk tree data
    useEffect(() => {
        const loadPerkTree = async () => {
            try {
                const response = await fetch(`/config/ars_affinity/perk_trees/${school}.json`)
                if (response.ok) {
                    const data = await response.json()
                    setPerkTreeData(data)
                    calculateNodePositions(data)
                }
            } catch (error) {
                console.error('Error loading perk tree:', error)
            }
        }

        loadPerkTree()
    }, [school])

    // Calculate node positions based on tiers and prerequisites
    const calculateNodePositions = (data: PerkTreeData) => {
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

        // Calculate positions
        tiers.forEach((nodes, tier) => {
            const startX = -(nodes.length - 1) * NODE_SPACING / 2
            nodes.forEach((node, index) => {
                positions.set(node.id, {
                    x: startX + index * NODE_SPACING,
                    y: tier * TIER_SPACING,
                    tier,
                    index
                })
            })
        })

        setNodePositions(positions)
    }

    // Handle mouse wheel zoom
    const handleWheel = useCallback((e: React.WheelEvent) => {
        e.preventDefault()
        const delta = e.deltaY > 0 ? -ZOOM_SPEED : ZOOM_SPEED
        setViewport(prev => ({
            ...prev,
            zoom: Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, prev.zoom + delta))
        }))
    }, [])

    // Handle mouse down for dragging
    const handleMouseDown = useCallback((e: React.MouseEvent) => {
        if (e.target === containerRef.current) {
            setIsDragging(true)
            setDragStart({ x: e.clientX - viewport.x, y: e.clientY - viewport.y })
        }
    }, [viewport.x, viewport.y])

    // Handle mouse move for dragging
    const handleMouseMove = useCallback((e: React.MouseEvent) => {
        if (isDragging) {
            setViewport(prev => ({
                ...prev,
                x: (e.clientX - dragStart.x) * dragSensitivity,
                y: (e.clientY - dragStart.y) * dragSensitivity
            }))
        }
    }, [isDragging, dragStart])

    // Handle mouse up
    const handleMouseUp = useCallback(() => {
        setIsDragging(false)
    }, [])

    // Handle node click
    const handleNodeClick = useCallback((node: PerkNode) => {
        console.log('Clicked node:', node.id)
        // TODO: Implement perk allocation/deallocation
    }, [])

    // Render connection lines between nodes
    const renderConnections = () => {
        if (!perkTreeData) return null

        return perkTreeData.perks.map(node => {
            return node.prerequisites?.map(prereqId => {
                const prereqPos = nodePositions.get(prereqId)
                const nodePos = nodePositions.get(node.id)
                
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

    // Render individual perk nodes
    const renderNodes = () => {
        if (!perkTreeData) return null

        return perkTreeData.perks.map(node => {
            const position = nodePositions.get(node.id)
            if (!position) return null

            const isHovered = hoveredNode?.id === node.id
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
                        onClick={() => handleNodeClick(node)}
                        onMouseEnter={() => setHoveredNode(node)}
                        onMouseLeave={() => setHoveredNode(null)}
                    />
                    <text
                        x={position.x}
                        y={position.y + 4}
                        textAnchor="middle"
                        fill="white"
                        fontSize="12"
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

        const position = nodePositions.get(hoveredNode.id)
        if (!position) return null

        const screenX = (position.x + viewport.x) * viewport.zoom
        const screenY = (position.y + viewport.y) * viewport.zoom

        return (
            <div
                style={{
                    position: 'absolute',
                    left: `${screenX + NODE_SIZE}px`,
                    top: `${screenY - 20}px`,
                    zIndex: 1000,
                    pointerEvents: 'none'
                }}
            >
                <div className="perk-tooltip">
                    <div style={{ fontWeight: 'bold', marginBottom: '4px' }}>
                        {formatPerkName(hoveredNode.id)}
                    </div>
                    <div style={{ color: '#ccc', marginBottom: '8px' }}>
                        Cost: {hoveredNode.pointCost} points
                    </div>
                    <div style={{ marginBottom: '8px' }}>
                        <PerkStringRenderer perk={{ perk: hoveredNode.perk }} />
                    </div>
                    {hoveredNode.prerequisites && hoveredNode.prerequisites.length > 0 && (
                        <div style={{ color: '#aaa', fontSize: '12px' }}>
                            Prerequisites: {hoveredNode.prerequisites.length}
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

    if (!perkTreeData) {
        return (
            <div className="flex items-center justify-center h-screen">
                <div className="text-white">Loading {titleCase(school)} perk tree...</div>
            </div>
        )
    }

    return (
        <div className="perk-tree-container">
            {/* Header */}
            <div className="absolute top-4 left-4 z-10 flex gap-4">
                <button
                    onClick={onClose}
                    className="zoom-button"
                >
                    Back
                </button>
                <div className="zoom-display">
                    {titleCase(school)} Perk Tree
                </div>
            </div>

            {/* Zoom controls */}
            <div className="zoom-controls">
                <button
                    onClick={() => setViewport(prev => ({ ...prev, zoom: Math.min(MAX_ZOOM, prev.zoom + 0.2) }))}
                    className="zoom-button"
                >
                    +
                </button>
                <button
                    onClick={() => setViewport(prev => ({ ...prev, zoom: Math.max(MIN_ZOOM, prev.zoom - 0.2) }))}
                    className="zoom-button"
                >
                    -
                </button>
                <div className="zoom-display">
                    {Math.round(viewport.zoom * 100)}%
                </div>
            </div>

            {/* Main viewport */}
            <div
                ref={containerRef}
                className="w-full h-full"
                onWheel={handleWheel}
                onMouseDown={handleMouseDown}
                onMouseMove={handleMouseMove}
                onMouseUp={handleMouseUp}
                onMouseLeave={handleMouseUp}
            >
                <svg
                    className="perk-tree-svg"
                    width="100%"
                    height="100%"
                    style={{
                        transform: `translate(${viewport.x}px, ${viewport.y}px) scale(${viewport.zoom})`,
                    }}
                >
                    {/* Grid background */}
                    <defs>
                        <pattern id="grid" width="50" height="50" patternUnits="userSpaceOnUse">
                            <path d="M 50 0 L 0 0 0 50" fill="none" stroke="#374151" strokeWidth="0.5" opacity="0.3"/>
                        </pattern>
                    </defs>
                    <rect width="100%" height="100%" fill="url(#grid)" />

                    {/* Connections */}
                    {renderConnections()}

                    {/* Nodes */}
                    {renderNodes()}
                </svg>
            </div>

            {/* Tooltip */}
            {renderTooltip()}
        </div>
    )
}

export default PerkTreeViewer
