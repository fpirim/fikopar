import { afterEach, describe, expect, it, vi } from 'vitest'
import i18n from '../i18n'
import { saveBlob, toCsv } from './csv'

describe('toCsv', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  const cols = [
    { header: 'Account', value: (r: { a: string; b: string }) => r.a },
    { header: 'Name', value: (r: { a: string; b: string }) => r.b },
  ]

  it('quotes cells and uses the list separator of the language', async () => {
    await i18n.changeLanguage('en')
    expect(toCsv([{ a: '120.1', b: 'Kaya, "Ziraat"' }], cols)).toBe(
      '\uFEFFAccount,Name\r\n120.1,"Kaya, ""Ziraat"""\r\n',
    )
    await i18n.changeLanguage('tr')
    expect(toCsv([{ a: '120.1', b: 'Kaya; Ziraat' }], cols)).toBe(
      '\uFEFFAccount;Name\r\n120.1;"Kaya; Ziraat"\r\n',
    )
  })
})

describe('saveBlob', () => {
  it('clicks a temporary download link', () => {
    const click = vi.spyOn(HTMLAnchorElement.prototype, 'click').mockImplementation(() => {})
    URL.createObjectURL = vi.fn(() => 'blob:x')
    URL.revokeObjectURL = vi.fn()
    saveBlob(new Blob(['a']), 'balances.csv')
    expect(click).toHaveBeenCalled()
    expect(URL.revokeObjectURL).toHaveBeenCalledWith('blob:x')
  })
})
