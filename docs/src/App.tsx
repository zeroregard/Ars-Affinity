import React from 'react'
import './App.css'

const schools: string[] = [
  'abjuration',
  'air',
  'conjuration',
  'earth',
  'fire',
  'manipulation',
  'necromancy',
  'water'
]

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
          {schools.map((school) => (
            <div key={school} className="school-container">
              <div className="tiers-container">
                {[1, 2, 3].map((tier) => (
                  <img
                    key={`${school}_tier${tier}`}
                    src={`/tiers/${school}_tier${tier}.png`}
                    alt={`${school} tier ${tier}`}
                    className="tier-bar"
                  />
                ))}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

export default App 