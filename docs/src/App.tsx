import React from 'react'
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
    height: 32
}, {
    radius: 85,
    height: 36
}, {
    radius: 100,
    height: 40
}]

function App(): React.JSX.Element {

    return (
        <div className="app">
            <div className="background-container">
                <img
                    src="/affinity_bg.png"
                    alt="Affinity Background"
                    className="affinity-bg"
                />
                <div className="schools-overlay">
                    {schools.map((school) => (<SchoolDisplay school={school} />))}
                </div>
                <div className="schools-overlay" style={{ position: 'absolute', left: 'calc(50%)', top: 'calc(50% - 8px)', width: '100%', height: '100%' }}>
                    {tierSettings.map(tierSetting => 
                        schools.map((school, index) => {
                            // Calculate position in clockwise circle starting from 12 o'clock
                            const angle = (index * 360 / schools.length) - 90 // Start from 12 o'clock
                            const angleRad = (angle * Math.PI) / 180
                            const width = 12;
                            const height = tierSetting.height;
                            
                            const x =  (tierSetting.radius * Math.cos(angleRad)) - (width / 2)
                            const y =  (tierSetting.radius * Math.sin(angleRad)) - (height / 2)
                            
                            return (
                                <button style={{ width: `${width}px`, transform: `rotate(${angle}deg)`, height: `${height}px`, backgroundColor: 'red', position: 'absolute', left: `${x}px`, top: `${y}px` }}></button>
                            )
                        })
                    )}
                </div>
            </div>
        </div>
    )
}

function SchoolDisplay({ school, style }: { school: School, style?: React.CSSProperties }) {
    return (
        <div key={school} className="school-container" style={style}>
            <div className="tiers-container">
                {[1, 2, 3].map((tier) => (
                    <img
                        key={`${school}_tier${tier}`}
                        src={`/tiers/${school}_tier${tier}.png`}
                        alt={`${school} tier ${tier}`}
                        className="tier-bar"
                        style={{
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