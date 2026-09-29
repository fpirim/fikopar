import { describe, it, expect, afterEach } from 'vitest'
import i18n from '../i18n'
import { formatDate } from './format'

describe('formatDate', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  it('returns empty string for empty/nullish input', () => {
    expect(formatDate()).toBe('')
    expect(formatDate(null)).toBe('')
    expect(formatDate('')).toBe('')
  })

  it('follows the active language', async () => {
    const iso = '2026-05-24T12:00:00Z'
    await i18n.changeLanguage('tr')
    expect(formatDate(iso)).toBe('24.05.2026')
    await i18n.changeLanguage('en')
    expect(formatDate(iso)).toBe('5/24/2026')
  })
})
