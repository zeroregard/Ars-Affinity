import React from 'react'
import { SchoolIconProps } from '../types/perkTree'

const SchoolIcon: React.FC<SchoolIconProps> = ({ school, centerX, centerY, angle }) => {
    const iconRadius = 122
    const iconSize = 32
    const circleRadius = 20
    const iconX = centerX + (iconRadius * Math.cos(angle)) - (iconSize / 2)
    const iconY = centerY + (iconRadius * Math.sin(angle)) - (iconSize / 2)
    const circleX = centerX + (iconRadius * Math.cos(angle))
    const circleY = centerY + (iconRadius * Math.sin(angle))

    return (
        <g>
            {/* Background circle */}
            <circle
                cx={circleX}
                cy={circleY}
                r={circleRadius}
                fill="rgba(0, 0, 0, 1)"
                stroke="rgba(255, 255, 255, 0.3)"
                strokeWidth="2"
            />
            {/* School icon */}
            <image
                href={`/icons/${school}_tooltip.png`}
                x={iconX}
                y={iconY}
                width={iconSize}
                height={iconSize}
                imageRendering="pixelated"
            />
        </g>
    )
}

export default SchoolIcon

