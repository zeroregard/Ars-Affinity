import React from 'react'
import { useIntl } from 'react-intl'
import { titleCase } from './utils/string'

interface Perk {
    isBuff?: boolean
    amount?: number
    perk?: string
    entities?: string[]
}

interface PerkRendererProps {
    perk: Perk
}

function PerkRenderer({ perk }: PerkRendererProps) {
    const intl = useIntl()
    console.log('Perk data:', perk)
    
    // Try different possible perk ID fields
    const perkId = perk.perk;
    console.log('Perk ID:', perkId)
    
    if (!perkId) {
        return <span style={{ color: '#ff6b6b' }}>Invalid perk data: {JSON.stringify(perk)}</span>
    }
    
    const messageKey = `ars_affinity.perk.${perkId}`
    const message = intl.formatMessage({ id: messageKey })
    
    if (message === messageKey) {
        return <span style={{ color: '#ff6b6b' }}>Unknown perk: {perkId}</span>
    }

    let formattedMessage = message

    // Replace %d with the value
    if (perk.amount !== undefined) {
        const percentage = perk.amount * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    if(perk.entities) {
        formattedMessage = formattedMessage.replace(/%s/g, perk.entities.map(entity => titleCase(entity
            .replace('minecraft:', '')
            .replace('_', ' ')))
        .join(', '))
    }

    const color = perk.isBuff ? '#4ade80' : '#f87171' // green for positive, red for negative
    const prefix = perk.isBuff ? '+ ' : '- '

    return (
        <span style={{ 
            color, 
            fontFamily: 'Minecraft, monospace',
            fontSize: '14px',
            textShadow: '1px 1px 0px rgba(0, 0, 0, 0.8)',
            WebkitFontSmoothing: 'none',
            MozOsxFontSmoothing: 'none',
            textRendering: 'optimizeSpeed'
        }}>
            {prefix}{formattedMessage}
        </span>
    )
}



export default PerkRenderer 