import React, { useState } from 'react'
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