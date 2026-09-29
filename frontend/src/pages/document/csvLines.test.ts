import { describe, expect, it } from 'vitest'
import i18n from '../../i18n'
import { parseLinesCsv } from './csvLines'

describe('parseLinesCsv', () => {
  it('reads code; qty; price with a header and blank lines', async () => {
    await i18n.changeLanguage('tr')
    expect(
      parseLinesCsv('\uFEFFkod;miktar;fiyat\r\nHP-3774;2;12.400,50\r\n\r\nCL-1150;4\n'),
    ).toEqual([
      { code: 'HP-3774', qty: '2', price: '12400.5' },
      { code: 'CL-1150', qty: '4', price: '' },
    ])
  })

  it('takes commas or tabs when there is no semicolon and skips bad quantities', async () => {
    await i18n.changeLanguage('en')
    expect(parseLinesCsv('code,qty\n"FL-0092",20\nBR-2210,x\n')).toEqual([
      { code: 'FL-0092', qty: '20', price: '' },
    ])
    expect(parseLinesCsv('WP-3715\t3\t3420.00')).toEqual([
      { code: 'WP-3715', qty: '3', price: '3420' },
    ])
    expect(parseLinesCsv('')).toEqual([])
  })
})
