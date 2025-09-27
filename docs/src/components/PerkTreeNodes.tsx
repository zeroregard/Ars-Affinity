import React from 'react'
import { School, PerkNode, SchoolTreeData } from '../types/perkTree'
import { NODE_SIZE, SCHOOL_COLORS } from '../constants/perkTree'
import { toRomanNumeral } from '../utils/romanNumerals'

interface PerkTreeNodesProps {
    schoolTree: SchoolTreeData
    hoveredNode: { node: PerkNode, school: School } | null
    setHoveredNode: (node: { node: PerkNode, school: School } | null) => void
    handleNodeClick: (node: PerkNode, school: School) => void
}

export const PerkTreeNodes: React.FC<PerkTreeNodesProps> = ({ 
    schoolTree, 
    hoveredNode, 
    setHoveredNode, 
    handleNodeClick 
}) => {
    const schoolColors = SCHOOL_COLORS[schoolTree.school]
    
    return (
        <>
            {schoolTree.data.perks.map(node => {
                const position = schoolTree.nodePositions.get(node.id)
                if (!position) return null

                const isHovered = hoveredNode?.node.id === node.id && hoveredNode?.school === schoolTree.school
                const isAllocated = false
                const canAllocate = true

                let fillColor: string
                if (isAllocated) {
                    fillColor = schoolColors.allocated
                } else if (canAllocate) {
                    fillColor = 'rgba(0, 0, 0, 1)'
                } else {
                    fillColor = 'rgba(0, 0, 0, 1)'
                }

                const strokeColor = isHovered ? schoolColors.hover : '#374151'

                const iconSize = NODE_SIZE * 0.6
                const iconX = position.x - iconSize / 2
                const iconY = position.y - iconSize / 2

                const romanNumeral = toRomanNumeral(node.tier)
                const textSize = NODE_SIZE * 0.3
                const textX = position.x + (NODE_SIZE / 2) - textSize * 0.3
                const textY = position.y + (NODE_SIZE / 2) - textSize * 0.2

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
            })}
        </>
    )
}
