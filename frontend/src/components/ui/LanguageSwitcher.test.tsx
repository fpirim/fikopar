import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'

vi.mock('../../i18n/config', () => ({
  SUPPORTED: [
    { code: 'tr', label: 'Türkçe' },
    { code: 'en', label: 'English' },
  ],
}))

import LanguageSwitcher from './LanguageSwitcher'

describe('LanguageSwitcher', () => {
  it('renders a select with the configured locales', () => {
    render(<LanguageSwitcher />)
    expect(screen.getByRole('combobox')).toBeInTheDocument()
    expect(screen.getByText('Türkçe')).toBeInTheDocument()
    expect(screen.getByText('English')).toBeInTheDocument()
  })
})
