import { describe, it, expect, vi, afterEach } from 'vitest'
import { printRows } from './print'

type Row = { name: string }

afterEach(() => {
  vi.restoreAllMocks()
})

describe('printRows', () => {
  it('opens a window with an escaped table and triggers print', () => {
    const doc = { head: { innerHTML: '' }, body: { innerHTML: '' }, title: '' }
    const w = { document: doc, focus: vi.fn(), print: vi.fn() }
    const openSpy = vi.spyOn(window, 'open').mockReturnValue(w as unknown as Window)

    printRows<Row>('Liste', [{ header: 'Ad', value: (r) => r.name }], [{ name: 'A & <B>' }])

    expect(openSpy).toHaveBeenCalledWith('', '_blank')
    expect(doc.body.innerHTML).toContain('Liste (1)')
    expect(doc.body.innerHTML).toContain('<th>Ad</th>')
    expect(doc.body.innerHTML).toContain('A &amp; &lt;B&gt;') // HTML escaped
    expect(w.print).toHaveBeenCalled()
  })

  it('does nothing for an empty row set', () => {
    const openSpy = vi.spyOn(window, 'open')
    printRows<Row>('Liste', [{ header: 'Ad', value: (r) => r.name }], [])
    expect(openSpy).not.toHaveBeenCalled()
  })

  it('returns silently when the popup is blocked', () => {
    vi.spyOn(window, 'open').mockReturnValue(null)
    expect(() =>
      printRows<Row>('Liste', [{ header: 'Ad', value: (r) => r.name }], [{ name: 'x' }]),
    ).not.toThrow()
  })
})
