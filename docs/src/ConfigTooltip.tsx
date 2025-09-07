import { useState, useEffect, useRef } from 'react'
import PerkRenderer from './PerkRenderer'
import { titleCase } from './utils/string'

type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

interface ConfigTooltipProps {
    hoveredSchool: School | null
    hoveredTier: number | null
    hoveredElementPosition: { x: number, y: number, width: number, height: number } | null
}

function ConfigTooltip({ 
    hoveredSchool, 
    hoveredTier, 
    hoveredElementPosition
}: ConfigTooltipProps) {
    const [configData, setConfigData] = useState<any>(null)
    const [loading, setLoading] = useState(false)
    const [tooltipPosition, setTooltipPosition] = useState({ x: 0, y: 0 })
    const tooltipRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        const loadConfig = async () => {
            if (!hoveredSchool || !hoveredTier) {
                setConfigData(null)
                return
            }

            setLoading(true)
            try {
                const response = await fetch(`/config/ars_affinity/perks/${hoveredSchool}/${hoveredTier}.json`)
                if (response.ok) {
                    const data = await response.json()
                    setConfigData(data)
                } else {
                    console.warn(`Config file not found: ${hoveredSchool}/${hoveredTier}.json`)
                    setConfigData(null)
                }
            } catch (error) {
                console.error('Error loading config:', error)
                setConfigData(null)
            } finally {
                setLoading(false)
            }
        }

        loadConfig()
    }, [hoveredSchool, hoveredTier])

    useEffect(() => {
        if (!hoveredSchool || !hoveredTier || !hoveredElementPosition) return

        const TOOLTIP_WIDTH = 400
        const MARGIN = 16
        const SCREEN_PADDING = 16

        const calculateTooltipPosition = () => {
            const tooltipHeight = tooltipRef.current?.offsetHeight || 0
            const elementCenter = {
                x: hoveredElementPosition.x + (hoveredElementPosition.width / 2),
                y: hoveredElementPosition.y + (hoveredElementPosition.height / 2)
            }

            // Calculate horizontal position (centered on element)
            let x = elementCenter.x - (TOOLTIP_WIDTH / 2)

            // Calculate vertical position based on element location
            const isElementInLowerHalf = elementCenter.y > window.innerHeight / 2
            let y = isElementInLowerHalf
                ? hoveredElementPosition.y - tooltipHeight - MARGIN
                : hoveredElementPosition.y + hoveredElementPosition.height + MARGIN

            // Constrain to screen bounds
            x = Math.max(SCREEN_PADDING, Math.min(x, window.innerWidth - TOOLTIP_WIDTH - SCREEN_PADDING))
            
            // Handle vertical overflow with fallback positioning
            if (y < SCREEN_PADDING) {
                y = hoveredElementPosition.y + hoveredElementPosition.height + MARGIN
            }
            if (y + tooltipHeight > window.innerHeight - SCREEN_PADDING) {
                y = hoveredElementPosition.y - tooltipHeight - MARGIN
            }
            if (y < SCREEN_PADDING) {
                y = SCREEN_PADDING
            }

            setTooltipPosition({ x, y })
        }

        calculateTooltipPosition()
    }, [hoveredElementPosition, hoveredSchool, hoveredTier, configData])


    if (!hoveredSchool || !hoveredTier) return null

    return (
        <>
            <style>
                {`
                    @font-face {
                        font-family: 'Minecraft';
                        src: url('/font/minecraft.otf') format('opentype');
                        font-weight: normal;
                        font-style: normal;
                    }
                `}
            </style>
            <div style={{ 
                position: 'fixed', 
                left: `${tooltipPosition.x}px`, 
                top: `${tooltipPosition.y}px`, 
                zIndex: 1000,
                pointerEvents: 'none'
            }}>
                <div 
                    ref={tooltipRef}
                    style={{
                        width: '400px',
                        minHeight: '200px',
                        maxHeight: '80vh',
                        backgroundColor: 'rgba(30, 15, 30, 0.9)',
                        border: '7px solid transparent',
                        borderImage: 'url(/tooltip.png) 3',
                        borderImageSlice: '2',
                        borderImageRepeat: 'stretch',
                        borderRadius: '16px',
                        color: 'white',
                        padding: '15px',
                        boxSizing: 'border-box',
                        imageRendering: 'pixelated',
                        fontFamily: 'Minecraft, monospace',
                        fontSmooth: 'never',
                        WebkitFontSmoothing: 'none',
                        MozOsxFontSmoothing: 'none',
                        textRendering: 'optimizeSpeed',
                        textShadow: '2px 2px 0px rgba(62, 62, 62, 1)',
                        overflowY: 'auto'
                    }}
                >
                    <h2 style={{ 
                        margin: '0 0 4px 0', 
                        fontSize: '18px', 
                    }}>
                       <SchoolTitle school={hoveredSchool} />
                    </h2>
                    <h3 style={{ fontSize: '14px', color: '#ccc' }}> Tier {hoveredTier}</h3>
                    {configData && (
                        <div style={{ marginTop: '10px' }}>
                            {configData && Array.isArray(configData) && configData
                                .sort((a, b) => {
                                    // Sort buffs first (true), then negatives (false)
                                    if (a.isBuff && !b.isBuff) return -1
                                    if (!a.isBuff && b.isBuff) return 1
                                    return 0
                                })
                                .map((perk: any, index: number) => (
                                    <div key={index} style={{ marginBottom: '2px' }}>
                                        <PerkRenderer perk={perk} />
                                    </div>
                                ))}
                        </div>
                    )}
                    {!loading && !configData && (
                        <div style={{ color: '#ccc', fontStyle: 'italic' }}>
                        </div>
                    )}
                </div>
            </div>
        </>
    )
}

function SchoolTitle({ school }: { school: School }) {
    const schoolName = school === 'necromancy' ? 'Anima' : school
    return titleCase(schoolName)
}

export default ConfigTooltip 