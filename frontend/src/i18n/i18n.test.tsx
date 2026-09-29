import { describe, it, expect } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import { I18nextProvider } from 'react-i18next'
import i18n from '.'
import { DEFAULT_LOCALE, SUPPORTED } from './config'
import LanguageSwitcher from '../components/ui/LanguageSwitcher'

import enLocales from './locales/en/common.json'

const load = import.meta.glob('./locales/*/*.json', { eager: true, import: 'default' })

function keys(obj: unknown, prefix = ''): string[] {
  if (obj === null || typeof obj !== 'object') return [prefix]
  return Object.entries(obj as Record<string, unknown>).flatMap(([k, v]) =>
    keys(v, prefix ? `${prefix}.${k}` : k),
  )
}

describe('i18n', () => {
  it('defaults to English and offers Turkish', () => {
    expect(DEFAULT_LOCALE).toBe('en')
    expect(SUPPORTED.map((l) => l.code)).toEqual(['en', 'tr'])
    expect(enLocales.cancel).toBe('Cancel')
  })

  it('has the same keys in every locale', () => {
    const byNs: Record<string, Record<string, string[]>> = {}
    for (const [path, data] of Object.entries(load)) {
      const [, lng, file] = /\.\/locales\/(\w+)\/(\w+)\.json$/.exec(path)!
      byNs[file] ??= {}
      byNs[file][lng] = keys(data).sort()
    }
    for (const [ns, perLng] of Object.entries(byNs)) {
      expect(perLng.tr, ns).toEqual(perLng.en)
    }
  })

  it('switches EN → TR through the language switcher', async () => {
    await i18n.changeLanguage('en')
    render(
      <I18nextProvider i18n={i18n}>
        <LanguageSwitcher />
      </I18nextProvider>,
    )
    const select = screen.getByRole('combobox', { name: 'Language' })
    expect(select).toHaveValue('en')

    fireEvent.change(select, { target: { value: 'tr' } })

    expect(i18n.language).toBe('tr')
    expect(i18n.t('common:cancel')).toBe('İptal')
    expect(localStorage.getItem('lang')).toBe('tr')
    expect(document.documentElement.lang).toBe('tr')
    expect(await screen.findByRole('combobox', { name: 'Dil' })).toHaveValue('tr')
  })
})
