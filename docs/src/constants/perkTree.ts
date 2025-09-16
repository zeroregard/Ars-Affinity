import { School } from '../types/perkTree'

export const NODE_SIZE = 32
export const NODE_SPACING = 60
export const TIER_SPACING = 80
export const SCHOOL_SPACING = 150
export const MIN_ZOOM = 2.0
export const MAX_ZOOM = 8.0
export const DEFAULT_ZOOM = 2.0
export const ZOOM_SPEED = 0.1

export const SCHOOL_COLORS: Record<School, { 
    primary: string; 
    hover: string; 
    allocated: string;
    gradient?: { from: string; to: string };
}> = {
    'manipulation': { primary: '#e99a58', hover: '#f2b366', allocated: '#10B981' },
    'fire': { primary: '#f06666', hover: '#f58a8a', allocated: '#10B981' },
    'necromancy': { 
        primary: 'url(#necromancy-gradient)', 
        hover: 'url(#necromancy-gradient-hover)', 
        allocated: '#10B981',
        gradient: { from: '#282828', to: '#8b0606' }
    },
    'air': { primary: '#d4cf5a', hover: '#e0dc7a', allocated: '#10B981' },
    'conjuration': { primary: '#6ae3ce', hover: '#8ae9d6', allocated: '#10B981' },
    'water': { primary: '#82a2ed', hover: '#a1baf0', allocated: '#10B981' },
    'abjuration': { primary: '#eb7cce', hover: '#f099d9', allocated: '#10B981' },
    'earth': { primary: '#62e296', hover: '#7ee8a8', allocated: '#10B981' }
}

export const schools: School[] = [
    'manipulation',
    'fire',
    'necromancy',
    'air',
    'conjuration',
    'water',
    'abjuration',
    'earth'
]

