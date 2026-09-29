import { afterEach, describe, expect, it } from 'vitest'
import i18n from '../i18n'
import { describeCondition, parseCondition } from './filterExpr'

describe('parseCondition', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  it('reads number comparisons with locale separators', async () => {
    await i18n.changeLanguage('en')
    expect(parseCondition('number', '> 10,000')).toEqual({
      filterType: 'number',
      type: 'greaterThan',
      filter: 10000,
    })
    expect(parseCondition('number', '<=5.5')).toMatchObject({
      type: 'lessThanOrEqual',
      filter: 5.5,
    })
    expect(parseCondition('number', '≠ 0')).toMatchObject({ type: 'notEqual', filter: 0 })
    expect(parseCondition('number', '!=0')).toMatchObject({ type: 'notEqual', filter: 0 })
    expect(parseCondition('number', '100..200')).toEqual({
      filterType: 'number',
      type: 'inRange',
      filter: 100,
      filterTo: 200,
    })
    expect(parseCondition('number', '42')).toMatchObject({ type: 'equals', filter: 42 })
    await i18n.changeLanguage('tr')
    expect(parseCondition('number', '>= 1.234,5')).toMatchObject({
      type: 'greaterThanOrEqual',
      filter: 1234.5,
    })
  })

  it('rejects junk and treats blank as no filter', () => {
    expect(parseCondition('number', 'abc')).toBeUndefined()
    expect(parseCondition('number', '   ')).toBeNull()
    expect(parseCondition('date', '32.13.2026')).toBeUndefined()
  })

  it('reads dates and ranges as dd.MM.yyyy', () => {
    expect(parseCondition('date', '< 01.09.2026')).toEqual({
      filterType: 'date',
      type: 'lessThan',
      dateFrom: '2026-09-01',
      dateTo: null,
    })
    expect(parseCondition('date', '01.09.2026..30.09.2026')).toEqual({
      filterType: 'date',
      type: 'inRange',
      dateFrom: '2026-09-01',
      dateTo: '2026-09-30',
    })
    expect(parseCondition('date', '29.09.2026')).toMatchObject({
      type: 'equals',
      dateFrom: '2026-09-29',
    })
  })

  it('reads text with an optional operator', () => {
    expect(parseCondition('text', 'Konya')).toEqual({
      filterType: 'text',
      type: 'contains',
      filter: 'Konya',
    })
    expect(parseCondition('text', '120.', 'startsWith')).toMatchObject({
      type: 'startsWith',
      filter: '120.',
    })
    expect(parseCondition('text', '=Konya')).toMatchObject({ type: 'equals', filter: 'Konya' })
    expect(parseCondition('text', '!Konya')).toMatchObject({ type: 'notContains' })
  })
})

describe('describeCondition', () => {
  afterEach(async () => {
    await i18n.changeLanguage('tr')
  })

  it('round-trips conditions to the text a user would type', async () => {
    await i18n.changeLanguage('en')
    for (const [kind, text] of [
      ['number', '> 10,000'],
      ['number', '100..200'],
      ['number', '≠ 0'],
      ['number', '42'],
      ['date', '< 01.09.2026'],
      ['date', '01.09.2026..30.09.2026'],
      ['text', 'Konya'],
      ['text', '=Konya'],
    ] as const) {
      expect(describeCondition(parseCondition(kind, text)!)).toBe(text)
    }
    expect(describeCondition(null)).toBe('')
    expect(describeCondition(parseCondition('text', '120.', 'startsWith')!, 'startsWith')).toBe(
      '120.',
    )
  })
})
