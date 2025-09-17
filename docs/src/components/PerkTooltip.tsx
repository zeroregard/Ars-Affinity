import React from 'react'
import { PerkStringRenderer } from '../utils/PerkStringRenderer'
import { titleCase } from '../utils/string'
import { toRomanNumeral } from '../utils/romanNumerals'
import { School, PerkNode } from '../types/perkTree'

interface PerkTooltipProps {
    hoveredNode: { node: PerkNode, school: School } | null
    mousePosition: { x: number, y: number }
}

const formatPerkName = (nodeId: string) => {
    const parts = nodeId.split('_')
    return parts.slice(1).map(part => 
        part.match(/\d+/) ? toRomanNumeral(parseInt(part)) : titleCase(part)
    ).join(' ')
}

const getPerkDataForRenderer = (node: PerkNode) => {
    const baseData: any = {
        perk: node.perk,
        isBuff: true,
    }

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

export const PerkTooltip: React.FC<PerkTooltipProps> = ({ hoveredNode, mousePosition }) => {
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