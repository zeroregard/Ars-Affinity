import React from 'react'
import { useIntl } from 'react-intl'
import { titleCase } from './utils/string'

interface Perk {
    isBuff?: boolean
    amount?: number
    chance?: number
    time?: number
    perk?: string
    entities?: string[]
    health?: number
    hunger?: number
    manaCost?: number
    cooldown?: number
    damage?: number
    freezeTime?: number
    radius?: number
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

    // Handle active abilities
    if (perk.manaCost !== undefined && perk.cooldown !== undefined) {
        if (perkId === 'ACTIVE_SWAP_ABILITY') {
            // Flat mana cost
            formattedMessage = formattedMessage
                .replace(/%d/g, perk.manaCost.toString())
                .replace(/%d/g, (perk.cooldown / 20).toString())
                .replace('§bF§r', 'KEYBIND')
        } else if (perkId === 'ACTIVE_ICE_BLAST') {
            // Percentage mana cost
            const manaPercentage = perk.manaCost * 100
            formattedMessage = formattedMessage
                .replace(/%d%%/g, `${manaPercentage}%`)
                .replace(/%d/g, (perk.cooldown / 20).toString())
                .replace('§bF§r', 'KEYBIND')
        }
    }
    // Handle passive perks with time and amount
    else if (perk.time && perk.amount) {
        formattedMessage = formattedMessage
            .replace(/%s/g, perk.amount.toString())
            .replace(/%d/g, (perk.time/20).toString())
    }
    // Handle passive perks with just amount
    else if (perk.amount !== undefined) {
        const percentage = perk.amount * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    // Handle passive perks with chance
    else if (perk.chance !== undefined) {
        const percentage = perk.chance * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    // Handle lich feast perk
    else if(perk.health && perk.hunger) {
        formattedMessage = formattedMessage.replace(/%.1f/g, perk.health.toString()).replace(/%.2f/g, perk.hunger.toString())
    }
    // Handle entity-based perks
    else if(perk.entities) {
        formattedMessage = formattedMessage.replace(/%s/g, perk.entities.map(entity => titleCase(entity
            .replace('minecraft:', '')
            .replace('_', ' ')))
        .join(', '))
    }

    const color = perk.isBuff ? '#4ade80' : '#f87171' // green for positive, red for negative
    const prefix = perk.isBuff ? '+ ' : '- '

    const textStyle: React.CSSProperties = {
        color, 
        fontFamily: 'Minecraft, monospace',
        fontSize: '14px',
        textShadow: '1px 1px 0px rgba(0, 0, 0, 0.8)',
        WebkitFontSmoothing: 'none',
        MozOsxFontSmoothing: 'none',
        fontWeight: 'normal',
        textDecoration: 'none'
    }

    return (
        <span style={textStyle}>
            {prefix}{formattedMessage}
        </span>
    )
}

export default PerkRenderer 