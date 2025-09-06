import '@testing-library/jest-dom'
import { IntlProvider } from 'react-intl'
import enUsMessages from '../../public/lang/en_us.json'

// Mock IntlProvider for tests
export const TestWrapper = ({ children }: { children: React.ReactNode }) => (
  <IntlProvider locale="en" messages={enUsMessages}>
    {children}
  </IntlProvider>
)
