import React, { useState, useEffect } from 'react'
import './App.css'

type School = 'abjuration' | 'air' | 'conjuration' | 'earth' | 'fire' | 'manipulation' | 'necromancy' | 'water'

const schools: School[] = [
    'abjuration',
    'earth',
    'manipulation',
    'fire',
    'necromancy',
    'air',
    'conjuration',
    'water'
]

const tierSettings = [{
    radius: 70,
    height: 36
}, {
    radius: 85,
    height: 40
}, {
    radius: 100,
    height: 44
}]

function App(): React.JSX.Element {
    const [hoveredSchool, setHoveredSchool] = useState<School | null>(null)
    const [hoveredTier, setHoveredTier] = useState<number | null>(null)
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

    return (
        <div className="app">
            <div className="background-container">
                <img
                    src="/affinity_bg.png"
                    alt="Affinity Background"
                    className="affinity-bg"
                />
                <div className="schools-overlay">
                    {schools.map((school) => (<SchoolDisplay key={school} school={school} hoveredSchool={hoveredSchool} hoveredTier={hoveredTier} />))}
                </div>
                <div className="schools-overlay" style={{ position: 'absolute', left: 'calc(50%)', top: 'calc(50% - 8px)', width: '100%', height: '100%' }}>
                    {tierSettings.map((tierSetting, tierIndex) =>
                        schools.map((school, index) => {
                            const angle = (index * 360 / schools.length) - 90
                            const angleRad = (angle * Math.PI) / 180
                            const width = 16;
                            const height = tierSetting.height;

                            const x = (tierSetting.radius * Math.cos(angleRad)) - (width / 2)
                            const y = (tierSetting.radius * Math.sin(angleRad)) - (height / 2)

                            const tier = tierIndex + 1

                            return (
                                <button
                                    key={`${school}_${tierSetting.radius}`}
                                    onClick={() => console.log('hello')}
                                    onMouseEnter={() => {
                                        setHoveredSchool(school)
                                        setHoveredTier(tier)
                                    }}
                                    onMouseLeave={() => {
                                        setHoveredSchool(null)
                                        setHoveredTier(null)
                                    }}
                                    style={{ 
                                        width: `${width}px`, 
                                        border: 'none',
                                        transform: `rotate(${angle}deg)`, 
                                        height: `${height}px`, 
                                        background: 'transparent',
                                        position: 'absolute', 
                                        left: `${x}px`, 
                                        top: `${y}px` 
                                    }}>
                                </button>
                            )
                        })
                    )}
                </div>
            </div>
            
            {/* Config Display */}
            <div style={{ 
                position: 'fixed', 
                top: '10px', 
                right: '10px', 
                backgroundColor: 'white', 
                padding: '15px', 
                border: '1px solid black',
                borderRadius: '5px',
                maxWidth: '400px',
                maxHeight: '300px',
                overflow: 'auto',
                boxShadow: '0 2px 10px rgba(0,0,0,0.1)'
            }}>
                <h3 style={{ margin: '0 0 10px 0', fontSize: '16px' }}>
                    {hoveredSchool && hoveredTier ? `${hoveredSchool} Tier ${hoveredTier}` : 'Hover over a school tier'}
                </h3>
                {loading && <div>Loading...</div>}
                {configData && (
                    <pre style={{ 
                        fontSize: '12px', 
                        margin: 0, 
                        whiteSpace: 'pre-wrap',
                        fontFamily: 'monospace'
                    }}>
                        {JSON.stringify(configData, null, 2)}
                    </pre>
                )}
                {!loading && !configData && hoveredSchool && hoveredTier && (
                    <div style={{ color: '#666', fontStyle: 'italic' }}>
                        No config file found for {hoveredSchool} tier {hoveredTier}
                    </div>
                )}
            </div>
        </div>
    )
}

function SchoolDisplay({ school, hoveredSchool, hoveredTier }: { school: School, hoveredSchool: School | null, hoveredTier: number | null }) {
    return (
        <div className="school-container">
            <div className="tiers-container">
                {[1, 2, 3].map((tier) => (
                    <img
                        key={`${school}_tier${tier}`}
                        src={`/tiers/${school}_tier${tier}.png`}
                        alt={`${school} tier ${tier}`}
                        className="tier-bar"
                        style={{
                            filter: hoveredSchool === school && hoveredTier === tier ? 'saturate(1)' : 'saturate(0)',
                            opacity: hoveredSchool === school && hoveredTier === tier ? 1 : 0.33,
                            width: '100%',
                            height: '100%',
                            imageRendering: 'pixelated'
                        }}
                    />
                ))}
            </div>
        </div>
    )
}

export default App 