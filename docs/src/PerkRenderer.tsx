import React from 'react'
import { useIntl } from 'react-intl'
import { titleCase } from './utils/string'

interface Perk {
    isBuff?: boolean
    amount?: number
    time?: number
    perk?: string
    entities?: string[]
    health?: number
    hunger?: number
}

interface PerkRendererProps {
    perk: Perk
}

function PerkRenderer({ perk }: PerkRendererProps) {
    const intl = useIntl()
    const perkId = perk.perk;
    
    if (!perkId) {
        return <span style={{ color: '#ff6b6b' }}>Invalid perk data: {JSON.stringify(perk)}</span>
    }
    
    const messageKey = `ars_affinity.perk.${perkId}`
    const message = intl.formatMessage({ id: messageKey })
    
    if (message === messageKey) {
        return <span style={{ color: '#ff6b6b' }}>Unknown perk: {perkId}</span>
    }

    let formattedMessage = message

    if (perk.time && perk.amount) {
        formattedMessage = formattedMessage
            .replace(/%s/g, perk.amount.toString())
            .replace(/%d/g, (perk.time/20).toString())
        
    }
    else if (perk.amount !== undefined) {
        const percentage = perk.amount * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    else if(perk.health && perk.hunger) {
        formattedMessage = formattedMessage.replace(/%.1f/g, perk.health.toString()).replace(/%.2f/g, perk.hunger.toString())
    }
    else if(perk.entities) {
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