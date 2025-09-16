import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import PerkTreeViewer from './PerkTreeViewer'

vi.mock('./PerkRenderer', () => ({
  default: ({ perk }: { perk: any }) => <div data-testid="perk-renderer">{perk.perk}</div>
}))

describe('PerkTreeViewer', () => {
  it('renders loading state initially', () => {
    render(<PerkTreeViewer school="fire" onClose={() => {}} />)
    expect(screen.getByText('Loading Fire perk tree...')).toBeInTheDocument()
  })

  it('renders school title correctly', () => {
    render(<PerkTreeViewer school="necromancy" onClose={() => {}} />)
    expect(screen.getByText('Loading Necromancy perk tree...')).toBeInTheDocument()
  })
})
