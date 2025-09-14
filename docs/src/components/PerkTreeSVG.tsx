import React from 'react'
import SchoolIcon from './SchoolIcon'
import { SchoolTreeData, PerkNode, School, ViewportState } from '../types/perkTree'
import { SCHOOL_COLORS, NODE_SIZE } from '../constants/perkTree'
import { toRomanNumeral } from '../utils/romanNumerals'

interface PerkTreeSVGProps {
    schoolTrees: SchoolTreeData[]
    viewport: ViewportState
    hoveredNode: { node: PerkNode, school: School } | null
    setHoveredNode: React.Dispatch<React.SetStateAction<{ node: PerkNode, school: School } | null>>
    handleNodeClick: (node: PerkNode, school: School) => void
    onMouseDown: (e: React.MouseEvent) => void
    onMouseMove: (e: React.MouseEvent) => void
    onMouseUp: () => void
}

const PerkTreeSVG: React.FC<PerkTreeSVGProps> = ({
    schoolTrees,
    viewport,
    hoveredNode,
    setHoveredNode,
    handleNodeClick,
    onMouseDown,
    onMouseMove,
    onMouseUp
}) => {
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

    return (
        <svg
            width="100%"
            height="100%"
            style={{ position: 'absolute', top: 0, left: 0, cursor: 'grab' }}
            onMouseDown={onMouseDown}
            onMouseMove={onMouseMove}
            onMouseUp={onMouseUp}
            onMouseLeave={onMouseUp}
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
    )
}

export default PerkTreeSVG

