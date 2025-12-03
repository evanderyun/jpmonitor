import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import Navigation from '../components/Navigation'

describe('Navigation', () => {
  it('renders main navigation with role', () => {
    render(<Navigation activeTab="dashboard" setActiveTab={() => {}} />)
    expect(screen.getByRole('navigation', { name: 'Main Navigation' })).toBeTruthy()
  })
})
