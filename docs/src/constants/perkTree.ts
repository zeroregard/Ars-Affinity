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
    primary: string; 
    hover: string; 
    allocated: string;
    gradient?: { from: string; to: string };
}> = {
    'manipulation': { primary: '#e99a58', hover: '#f2b366', allocated: '#10B981' }, // Orange
    'fire': { primary: '#f06666', hover: '#f58a8a', allocated: '#10B981' }, // Red
    'necromancy': { 
        primary: 'url(#necromancy-gradient)', 
        hover: 'url(#necromancy-gradient-hover)', 
        allocated: '#10B981',
        gradient: { from: '#282828', to: '#8b0606' }
    }, // Anima gradient
    'air': { primary: '#d4cf5a', hover: '#e0dc7a', allocated: '#10B981' }, // Yellow
    'conjuration': { primary: '#6ae3ce', hover: '#8ae9d6', allocated: '#10B981' }, // Teal
    'water': { primary: '#82a2ed', hover: '#a1baf0', allocated: '#10B981' }, // Blue
    'abjuration': { primary: '#eb7cce', hover: '#f099d9', allocated: '#10B981' }, // Pink
    'earth': { primary: '#62e296', hover: '#7ee8a8', allocated: '#10B981' } // Green
}

export const schools: School[] = [
    'manipulation',  // Right (0°)
    'fire',          // Bottom-right (45°)
    'necromancy',    // Bottom (90°)
    'air',           // Bottom-left (135°)
    'conjuration',   // Left (180°)
    'water',         // Top-left (225°)
    'abjuration',    // Top (270°)
    'earth'          // Top-right (315°)
]

