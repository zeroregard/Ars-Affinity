import React, { useEffect, useState } from 'react'
import './App.css'
import UnifiedPerkTreeViewer from './UnifiedPerkTreeViewer'
import { IntlProvider } from 'react-intl'

function App(): React.JSX.Element {
    const [messages, setMessages] = useState<Record<string, string>>({})

    useEffect(() => {
        const loadMessages = async () => {
            try {
                const response = await fetch('/lang/en_us.json')
                if (response.ok) {
                    const data = await response.json()
                    setMessages(data)
                }
            } catch (error) {
                console.error('Error loading messages:', error)
            }
        }

        loadMessages()
    }, [])

    return (
        <IntlProvider messages={messages} locale="en">
            <UnifiedPerkTreeViewer />
        </IntlProvider>
    )
}

export default App 