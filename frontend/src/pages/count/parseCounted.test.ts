import { afterAll, describe, expect, it } from 'vitest'
import i18n from '../../i18n'
import { parseCounted } from './parseCounted'

describe('parseCounted', () => {
  afterAll(() => i18n.changeLanguage('tr'))

  it('reads English numbers: dot decimal, comma groups of three', async () => {
    await i18n.changeLanguage('en')
    expect(parseCounted('12')).toBe('12')
    expect(parseCounted('2.5')).toBe('2.5')
    expect(parseCounted('1,250')).toBe('1250')
    expect(parseCounted('1,250.75')).toBe('1250.75')
    expect(parseCounted('')).toBeNull()
    expect(parseCounted('2,5')).toBeUndefined()
    expect(parseCounted('12,50')).toBeUndefined()
    expect(parseCounted('1.2.3')).toBeUndefined()
    expect(parseCounted('abc')).toBeUndefined()
  })

  it('reads Turkish numbers: comma decimal, dot groups of three', async () => {
    await i18n.changeLanguage('tr')
    expect(parseCounted('2,5')).toBe('2.5')
    expect(parseCounted('1.250')).toBe('1250')
    expect(parseCounted('1.250,5')).toBe('1250.5')
    expect(parseCounted('2.5')).toBeUndefined()
    expect(parseCounted('1,2,3')).toBeUndefined()
  })

  it('refuses negative quantities in both languages', async () => {
    for (const lng of ['en', 'tr']) {
      await i18n.changeLanguage(lng)
      expect(parseCounted('-3')).toBeUndefined()
      expect(parseCounted('−3')).toBeUndefined()
      expect(parseCounted('-0')).toBeUndefined()
    }
  })
})
