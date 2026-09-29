import { describe, it, expect, afterEach } from 'vitest'
import i18n from '../i18n'
import {
  formatBalance,
  formatDate,
  formatDateTime,
  formatDay,
  formatMoney,
  formatQty,
  formatTime,
  moneyToNumber,
  numberSeparators,
  parseAmount,
  parseDay,
  toMoney,
} from './format'

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

describe('money and day formats', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  it('formats money per locale', async () => {
    await i18n.changeLanguage('en')
    expect(formatMoney('1234567.5')).toBe('1,234,567.50')
    expect(formatMoney('-43800.00')).toBe('−43,800.00')
    await i18n.changeLanguage('tr')
    expect(formatMoney('1234567.5')).toBe('1.234.567,50')
    expect(formatMoney(null)).toBe('')
  })

  it('formats signed amounts with a sign', async () => {
    await i18n.changeLanguage('en')
    expect(formatMoney('52400.00', { sign: true })).toBe('+52,400.00')
    expect(formatMoney('0.00', { sign: true })).toBe('0.00')
  })

  it('formats balances with a debit/credit side', async () => {
    await i18n.changeLanguage('en')
    expect(formatBalance('186340.00')).toBe('186,340.00 Dr')
    expect(formatBalance('-96000.00')).toBe('96,000.00 Cr')
    expect(formatBalance('0.00')).toBe('0.00')
    await i18n.changeLanguage('tr')
    expect(formatBalance('186340.00')).toBe('186.340,00 B')
    expect(formatBalance('-96000.00')).toBe('96.000,00 A')
  })

  it('formats and parses calendar days as dd.MM.yyyy', async () => {
    await i18n.changeLanguage('en')
    expect(formatDay('2026-09-29')).toBe('29.09.2026')
    expect(formatDay(null)).toBe('')
    expect(parseDay('29.09.2026')).toBe('2026-09-29')
    expect(parseDay('1.9.2026')).toBe('2026-09-01')
    expect(parseDay('2026-09-29')).toBe('2026-09-29')
    expect(parseDay('31.02.2026')).toBeNull()
    expect(parseDay('nope')).toBeNull()
  })

  it('formats a timestamp as date and time', () => {
    expect(formatDateTime('2026-09-29T09:02:17')).toBe('29.09.2026 09:02:17')
    expect(formatTime('2026-09-29T09:02:17')).toBe('09:02:17')
  })

  it('converts between money strings and numbers', () => {
    expect(toMoney(1234.5)).toBe('1234.50')
    expect(toMoney(-0.004)).toBe('0.00')
    expect(moneyToNumber('1250.00')).toBe(1250)
    expect(moneyToNumber(undefined)).toBe(0)
  })

  it('uses the separators of the active language', async () => {
    await i18n.changeLanguage('en')
    expect(numberSeparators()).toEqual({ group: ',', decimal: '.' })
    await i18n.changeLanguage('tr')
    expect(numberSeparators()).toEqual({ group: '.', decimal: ',' })
  })
})

describe('parseAmount', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  it('reads typed amounts with the separators of the language', async () => {
    await i18n.changeLanguage('en')
    expect(parseAmount('15,000.5')).toBe('15000.50')
    expect(parseAmount('15000')).toBe('15000.00')
    expect(parseAmount('')).toBe('')
    expect(parseAmount('1.2.3')).toBeNull()
    await i18n.changeLanguage('tr')
    expect(parseAmount('15.000,5')).toBe('15000.50')
    expect(parseAmount('abc')).toBeNull()
  })
})

describe('formatQty', () => {
  it('formats signed quantities in the UI language', async () => {
    await i18n.changeLanguage('en')
    expect(formatQty('12')).toBe('12')
    expect(formatQty('-3')).toBe('−3')
    expect(formatQty('2.5', { sign: true })).toBe('+2.5')
    expect(formatQty('12500')).toBe('12,500')
    expect(formatQty(0, { sign: true })).toBe('0')
    expect(formatQty(null)).toBe('')
    expect(formatQty('x')).toBe('')
    await i18n.changeLanguage('tr')
    expect(formatQty('2.5')).toBe('2,5')
  })
})
