import { describe, expect, it } from 'vitest'
import i18n from '../../i18n'
import { parseQty } from './qty'

describe('parseQty', () => {
  it('reads quantities in the UI language', async () => {
    await i18n.changeLanguage('tr')
    expect(parseQty('2,5')).toBe('2.5')
    expect(parseQty('1.250')).toBe('1250')
    expect(parseQty(' ')).toBe('')
    expect(parseQty('-3')).toBeNull()
    expect(parseQty('abc')).toBeNull()
    await i18n.changeLanguage('en')
    expect(parseQty('2.50')).toBe('2.5')
  })
})
