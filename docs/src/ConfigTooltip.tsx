import React, { useState, useEffect } from 'react'

type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

interface ConfigTooltipProps {
    hoveredSchool: School | null
    hoveredTier: number | null
    mousePosition: { x: number, y: number }
}

function ConfigTooltip({ 
    hoveredSchool, 
    hoveredTier, 
    mousePosition
}: ConfigTooltipProps) {
    const [configData, setConfigData] = useState<any>(null)
    const [loading, setLoading] = useState(false)

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
                left: `${mousePosition.x + 15}px`, 
                top: `${mousePosition.y - 10}px`, 
                zIndex: 1000,
                pointerEvents: 'none'
            }}>
                <div style={{
                    width: '400px',
                    height: '300px',
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
                    textShadow: '2px 2px 0px rgba(62, 62, 62, 1)'
                }}>
                    <h2 style={{ 
                        margin: '0 0 10px 0', 
                        fontSize: '18px', 
   
                       
                    }}>
                        {hoveredSchool}
                    </h2>
                    <h3 style={{ fontSize: '16px' }}> Tier {hoveredTier}</h3>
                    {loading && <div style={{ color: 'white' }}>Loading...</div>}
                    {configData && (
                        <pre style={{ 
                            fontSize: '12px', 
                            margin: 0, 
                            whiteSpace: 'pre-wrap',
                            fontFamily: 'Minecraft, monospace',
                            color: 'white'
                        }}>
                            {JSON.stringify(configData, null, 2)}
                        </pre>
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

export default ConfigTooltip 