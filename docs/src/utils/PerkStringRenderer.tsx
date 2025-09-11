import React from 'react'
import { useIntl } from 'react-intl'
import { titleCase } from './string'

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
    duration?: number
}

interface PerkStringRendererProps {
    perk: Perk
}

export function PerkStringRenderer({ perk }: PerkStringRendererProps) {
    const intl = useIntl()
    const perkId = perk.perk;
    
    if (!perkId) {
        return <span style={{ color: '#ff6b6b' }}>Invalid perk data: {JSON.stringify(perk)}</span>
    }
    
    const messageKey = `ars_affinity.perk.${perkId}`
    let message: string
    
    try {
        message = intl.formatMessage({ id: messageKey, defaultMessage: messageKey })
    } catch (error) {
        message = messageKey
    }

    if (message === messageKey) {
        return <span style={{ color: '#ff6b6b' }}>Unknown perk: {perkId}</span>
    }

    let formattedMessage = message

    // Handle active abilities
    if (perk.manaCost !== undefined && perk.cooldown !== undefined) {
        // Replace §bF§r first before other %d replacements
        formattedMessage = formattedMessage.replace('§bF§r', 'KEYBIND')
        formattedMessage = formattedMessage.replace(/%d/g, (perk.cooldown / 20).toString())
    }
    // Handle ghost step perk (special case with amount, time, and cooldown)
    else if (perkId === 'PASSIVE_GHOST_STEP' && perk.amount !== undefined && perk.time !== undefined && perk.cooldown !== undefined) {
        const percentage = perk.amount * 100
        const timeInSeconds = perk.time
        const cooldownInSeconds = perk.cooldown
        formattedMessage = formattedMessage
            .replace(/%d%%/g, percentage.toString() + '%')
            .replace(/%d seconds/g, timeInSeconds.toString() + ' seconds')
            .replace(/%d second cooldown/g, cooldownInSeconds.toString() + ' second cooldown')
    }
    // Handle passive perks with time and amount
    else if (perk.time && perk.amount !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%s/g, perk.amount.toString())
            .replace(/%d/g, (perk.time/20).toString())
    }
    // Handle passive perks with just time
    else if (perk.time !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, (perk.time/20).toString())
    }
   
    else if (perkId === 'PASSIVE_SUMMONING_POWER' && perk.amount !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
    }
    // Handle passive perks with just amount
    else if (perk.amount !== undefined) {
        // Check if the message contains %% to determine if it's a percentage
        if (message.includes('%%')) {
            const percentage = perk.amount * 100
            formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
            formattedMessage = formattedMessage.replace(/%%/g, '%')
        } else {
            // Direct value, not a percentage
            formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
        }
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
    // Handle manipulation sickness perk
    else if(perk.duration !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%d/g, (perk.duration / 20).toString())
            .replace(/%d/g, perk.hunger.toString())
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

// Export the pure function for unit testing
export function formatPerkString(perk: Perk, messages: Record<string, string>): string {
    const perkId = perk.perk;
    
    if (!perkId) {
        return `Invalid perk data: ${JSON.stringify(perk)}`
    }
    
    const messageKey = `ars_affinity.perk.${perkId}`
    let message = messages[messageKey] || messageKey

    if (message === messageKey) {
        return `Unknown perk: ${perkId}`
    }

    let formattedMessage = message

    // Handle active abilities
    if (perk.manaCost !== undefined && perk.cooldown !== undefined) {
        // Replace §bF§r first before other %d replacements
        formattedMessage = formattedMessage.replace('§bF§r', 'KEYBIND')
        formattedMessage = formattedMessage.replace(/%d/g, (perk.cooldown / 20).toString())
    }
    // Handle ghost step perk (special case with amount, time, and cooldown)
    else if (perkId === 'PASSIVE_GHOST_STEP' && perk.amount !== undefined && perk.time !== undefined && perk.cooldown !== undefined) {
        const percentage = perk.amount * 100
        const timeInSeconds = perk.time
        const cooldownInSeconds = perk.cooldown
        formattedMessage = formattedMessage
            .replace(/%d%%/g, percentage.toString() + '%')
            .replace(/%d seconds/g, timeInSeconds.toString() + ' seconds')
            .replace(/%d second cooldown/g, cooldownInSeconds.toString() + ' second cooldown')
    }
    // Handle passive perks with time and amount
    else if (perk.time && perk.amount !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%s/g, perk.amount.toString())
            .replace(/%d/g, (perk.time/20).toString())
    }
    // Handle passive perks with just time
    else if (perk.time !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, (perk.time/20).toString())
    }
   
    else if (perkId === 'PASSIVE_SUMMONING_POWER' && perk.amount !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
    }
    // Handle passive perks with just amount
    else if (perk.amount !== undefined) {
        // Check if the message contains %% to determine if it's a percentage
        if (message.includes('%%')) {
            const percentage = perk.amount * 100
            formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
            formattedMessage = formattedMessage.replace(/%%/g, '%')
        } else {
            // Direct value, not a percentage
            formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
        }
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
    // Handle manipulation sickness perk
    else if(perk.duration !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%d/g, (perk.duration / 20).toString())
            .replace(/%d/g, perk.hunger.toString())
    }
    // Handle entity-based perks
    else if(perk.entities) {
        formattedMessage = formattedMessage.replace(/%s/g, perk.entities.map(entity => titleCase(entity
            .replace('minecraft:', '')
            .replace('_', ' ')))
        .join(', '))
    }

    const prefix = perk.isBuff ? '+ ' : '- '
    return `${prefix}${formattedMessage}`
}
