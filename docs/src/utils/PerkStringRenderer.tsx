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

    if (perk.manaCost !== undefined && perk.cooldown !== undefined) {
        formattedMessage = formattedMessage.replace('§bF§r', 'KEYBIND')
        formattedMessage = formattedMessage.replace(/%d/g, (perk.cooldown / 20).toString())
    }
    else if (perkId === 'PASSIVE_GHOST_STEP' && perk.amount !== undefined && perk.time !== undefined && perk.cooldown !== undefined) {
        const percentage = perk.amount * 100
        const timeInSeconds = perk.time / 20
        const cooldownInSeconds = perk.cooldown / 20
        formattedMessage = formattedMessage
            .replace(/%d%%/g, percentage.toString() + '%')
            .replace(/%d seconds/g, timeInSeconds.toString() + ' seconds')
            .replace(/%d second cooldown/g, cooldownInSeconds.toString() + ' second cooldown')
    }
    // Handle power perks first (they have amount but time is 0)
    else if (perkId.endsWith('_POWER') && perk.amount !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
    }
    // Handle resistance perks (raw numbers, not percentages)
    else if (perkId.endsWith('_RESISTANCE') && perk.amount !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
    }
    // Handle passive perks with time and amount
    else if (perk.time !== undefined && perk.amount !== undefined) {
        // Special case for PASSIVE_SUMMON_HEALTH - just use the amount as is
        if (perkId === 'PASSIVE_SUMMON_HEALTH') {
            formattedMessage = formattedMessage
                .replace(/%s/g, perk.amount.toString())
                .replace(/%d/g, (perk.time/20).toString())
        } else {
            formattedMessage = formattedMessage
                .replace(/%s/g, perk.amount.toString())
                .replace(/%d/g, (perk.time/20).toString())
        }
    }
    else if (perk.time !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, (perk.time/20).toString())
    }
    else if (perk.amount !== undefined) {
        if (message.includes('%%')) {
            const percentage = perk.amount * 100
            formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
            formattedMessage = formattedMessage.replace(/%%/g, '%')
        } else {
            formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
        }
    }
    else if (perk.chance !== undefined) {
        const percentage = perk.chance * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    else if(perk.health !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage.replace(/%.1f/, perk.health.toFixed(1)).replace(/%.1f/, perk.hunger.toFixed(1))
    }
    else if(perk.duration !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%d/g, (perk.duration / 20).toString())
            .replace(/%d/g, perk.hunger.toString())
    }
    else if(perk.entities) {
        formattedMessage = formattedMessage.replace(/%s/g, perk.entities.map(entity => titleCase(entity
            .replace('minecraft:', '')
            .replace('_', ' ')))
        .join(', '))
    }

    const color = perk.isBuff ? '#4ade80' : '#f87171'
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

    if (perk.manaCost !== undefined && perk.cooldown !== undefined) {
        formattedMessage = formattedMessage.replace('§bF§r', 'KEYBIND')
        formattedMessage = formattedMessage.replace(/%d/g, (perk.cooldown / 20).toString())
    }
    else if (perkId === 'PASSIVE_GHOST_STEP' && perk.amount !== undefined && perk.time !== undefined && perk.cooldown !== undefined) {
        const percentage = perk.amount * 100
        const timeInSeconds = perk.time / 20
        const cooldownInSeconds = perk.cooldown / 20
        formattedMessage = formattedMessage
            .replace(/%d%%/g, percentage.toString() + '%')
            .replace(/%d seconds/g, timeInSeconds.toString() + ' seconds')
            .replace(/%d second cooldown/g, cooldownInSeconds.toString() + ' second cooldown')
    }
    else if (perk.time !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, (perk.time/20).toString())
    }
   
    else if (perkId === 'PASSIVE_SUMMONING_POWER' && perk.amount !== undefined) {
        formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
    }
    else if (perk.amount !== undefined) {
        if (message.includes('%%')) {
            const percentage = perk.amount * 100
            formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
            formattedMessage = formattedMessage.replace(/%%/g, '%')
        } else {
            formattedMessage = formattedMessage.replace(/%d/g, perk.amount.toString())
        }
    }
    else if (perk.chance !== undefined) {
        const percentage = perk.chance * 100
        formattedMessage = formattedMessage.replace(/%d/g, percentage.toString())
        formattedMessage = formattedMessage.replace(/%%/g, '%')
    }
    else if(perk.health !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage.replace(/%.1f/, perk.health.toFixed(1)).replace(/%.1f/, perk.hunger.toFixed(1))
    }
    else if(perk.duration !== undefined && perk.hunger !== undefined) {
        formattedMessage = formattedMessage
            .replace(/%d/g, (perk.duration / 20).toString())
            .replace(/%d/g, perk.hunger.toString())
    }
    else if(perk.entities) {
        formattedMessage = formattedMessage.replace(/%s/g, perk.entities.map(entity => titleCase(entity
            .replace('minecraft:', '')
            .replace('_', ' ')))
        .join(', '))
    }

    const prefix = perk.isBuff ? '+ ' : '- '
    return `${prefix}${formattedMessage}`
}
