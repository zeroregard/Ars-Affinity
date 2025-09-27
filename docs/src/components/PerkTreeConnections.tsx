import React from 'react'
import { SchoolTreeData } from '../types/perkTree'

interface PerkTreeConnectionsProps {
    schoolTree: SchoolTreeData
}

export const PerkTreeConnections: React.FC<PerkTreeConnectionsProps> = ({ schoolTree }) => {
    return (
        <>
            {schoolTree.data.perks.map(node => {
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
            }).flat()}
        </>
    )
}
