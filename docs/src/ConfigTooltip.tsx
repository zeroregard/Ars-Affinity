import { useState, useEffect, useRef } from 'react'
import PerkRenderer from './PerkRenderer'
import { titleCase } from './utils/string'

type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

interface ConfigTooltipProps {
    hoveredSchool: School | null
    hoveredTier: number | null
    mousePosition: { x: number, y: number }
    configCache: Record<string, any>
}

function ConfigTooltip({ 
    hoveredSchool, 
    hoveredTier, 
    mousePosition,
    configCache
}: ConfigTooltipProps) {
    const [configData, setConfigData] = useState<any>(null)
    const tooltipRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        if (!hoveredSchool || !hoveredTier) {
            setConfigData(null)
            return
        }

        const cacheKey = `${hoveredSchool}_${hoveredTier}`
        const cachedData = configCache[cacheKey]
        setConfigData(cachedData || null)
    }, [hoveredSchool, hoveredTier, configCache])

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
                left: `${mousePosition.x}px`, 
                top: `${mousePosition.y + 16}px`, 
                zIndex: 1000,
                pointerEvents: 'none',
                transform: 'translateX(-50%)'
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
                    {!configData && (
                        <div style={{ color: '#ccc', fontStyle: 'italic' }}>
                            Loading...
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