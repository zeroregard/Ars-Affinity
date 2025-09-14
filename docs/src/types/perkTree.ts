export type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

export interface PerkNode {
    id: string
    perk: string
    tier: number
    pointCost: number
    category: 'PASSIVE' | 'ACTIVE'
    prerequisites?: string[]
    // Configurable perk values
    amount?: number
    time?: number
    cooldown?: number
    manaCost?: number
    damage?: number
    freezeTime?: number
    radius?: number
    dashLength?: number
    dashDuration?: number
    health?: number
    hunger?: number
}

export interface PerkTreeData {
    perks: PerkNode[]
}

export interface ViewportState {
    x: number
    y: number
    zoom: number
}

export interface NodePosition {
    x: number
    y: number
    tier: number
    index: number
    school: School
}

export interface SchoolTreeData {
    school: School
    data: PerkTreeData
    nodePositions: Map<string, NodePosition>
    centerX: number
    centerY: number
    angle: number
}

export interface SchoolIconProps {
    school: School
    centerX: number
    centerY: number
    angle: number
}

