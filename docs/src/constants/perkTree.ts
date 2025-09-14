import { School } from '../types/perkTree'

export const NODE_SIZE = 32
export const NODE_SPACING = 60
export const TIER_SPACING = 80
export const SCHOOL_SPACING = 150
export const MIN_ZOOM = 2.0  // 100%
export const MAX_ZOOM = 8.0   // 400% maximum zoom
export const DEFAULT_ZOOM = 2.0  // 100%
export const ZOOM_SPEED = 0.1

export const SCHOOL_COLORS: Record<School, { 
    primary: string
    secondary: string
    hover: string
}> = {
    'abjuration': { primary: '#3b82f6', secondary: '#1e40af', hover: '#60a5fa' },
    'air': { primary: '#06b6d4', secondary: '#0891b2', hover: '#22d3ee' },
    'conjuration': { primary: '#8b5cf6', secondary: '#7c3aed', hover: '#a78bfa' },
    'earth': { primary: '#84cc16', secondary: '#65a30d', hover: '#a3e635' },
    'fire': { primary: '#ef4444', secondary: '#dc2626', hover: '#f87171' },
    'manipulation': { primary: '#f59e0b', secondary: '#d97706', hover: '#fbbf24' },
    'necromancy': { primary: '#8b0606', secondary: '#282828', hover: '#a00a0a' },
    'water': { primary: '#0ea5e9', secondary: '#0284c7', hover: '#38bdf8' }
}

export const schools: School[] = [
    'fire',          // Right (0°)
    'manipulation',  // Top-right (45°)
    'necromancy',    // Bottom (90°)
    'air',           // Bottom-left (135°)
    'conjuration',   // Left (180°)
    'water',         // Top-left (225°)
    'abjuration',    // Top (270°)
    'earth'          // Top-right (315°)
]

