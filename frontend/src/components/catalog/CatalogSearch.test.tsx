import { describe, expect, it, vi } from 'vitest'
import { act, fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { useState } from 'react'
import type { CatalogHit, Depot } from '../../api/types'
import { server } from '../../mocks/node'
import { capture } from '../../test/msw'
import CatalogSearch from './CatalogSearch'
import { highlight } from './highlight'

const DEPOTS: Depot[] = [
  { id: 1, code: '1', name: 'Depot 1' },
  { id: 2, code: '2', name: 'Depot 2' },
]

function Harness({
  onPick = () => undefined,
  onClose = () => undefined,
}: Readonly<{ onPick?: (h: CatalogHit) => void; onClose?: () => void }>) {
  const [open, setOpen] = useState(true)
  const [query, setQuery] = useState('')
  return (
    <CatalogSearch
      orgId={1}
      depots={DEPOTS}
      open={open}
      onOpenChange={(o) => {
        setOpen(o)
        if (!o) onClose()
      }}
      query={query}
      onQueryChange={(q) => {
        setQuery(q)
        setOpen(true)
      }}
      onPick={onPick}
    />
  )
}

const input = () => screen.getByRole('combobox', { name: 'Parça ara' })
const options = () => screen.getAllByRole('option')

describe('CatalogSearch', () => {
  it('searches once after typing stops and highlights the words', async () => {
    const calls = capture('get', '/api/orgs/:orgId/catalog/search')
    render(<Harness />)
    await userEvent.type(input(), 'pump 37')
    const row = await screen.findByRole('option', { name: /HP-3702/ })
    expect(calls).toHaveLength(1)
    expect(calls[0].url.searchParams.get('q')).toBe('pump 37')
    const marks = [...row.querySelectorAll('mark')].map((m) => m.textContent?.toLowerCase())
    expect(marks).toEqual(expect.arrayContaining(['pump', '37']))
    expect(screen.getByRole('columnheader', { name: 'Dp 1' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Liste fiyatı' })).toBeInTheDocument()
    expect(within(row).getByText('9.850,00')).toBeInTheDocument()
  })

  it('waits for two characters', async () => {
    const calls = capture('get', '/api/orgs/:orgId/catalog/search')
    render(<Harness />)
    await userEvent.type(input(), 'p')
    expect(await screen.findByText('En az 2 karakter yazın')).toBeInTheDocument()
    await new Promise((r) => setTimeout(r, 300))
    expect(calls).toHaveLength(0)
  })

  it('shows the count, the price date and red stock at zero', async () => {
    render(<Harness />)
    await userEvent.type(input(), 'HP-3780')
    const row = await screen.findByRole('option', { name: /HP-3780/ })
    const cells = within(row).getAllByRole('cell')
    expect(cells[4]).toHaveTextContent('0')
    expect(cells[4]).toHaveClass('text-fk-negative')
    expect(screen.getByText(/1 \/ 1 · fiyat listesi 29\.09\.2026/)).toBeInTheDocument()
  })

  it('moves with arrows, picks with Enter and closes with Esc', async () => {
    const onPick = vi.fn()
    const onClose = vi.fn()
    render(<Harness onPick={onPick} onClose={onClose} />)
    await userEvent.type(input(), 'pump 37')
    await screen.findByRole('option', { name: /HP-3702/ })
    expect(options()[0]).toHaveAttribute('aria-selected', 'true')
    await userEvent.keyboard('{ArrowDown}')
    expect(options()[1]).toHaveAttribute('aria-selected', 'true')
    await userEvent.keyboard('{Enter}')
    expect(onPick).toHaveBeenCalledWith(
      expect.objectContaining({ stockNo: options()[1].dataset.stockNo }),
    )
    await userEvent.keyboard('{Escape}')
    expect(onClose).toHaveBeenCalled()
  })

  it('loads the next page at the end of the list', async () => {
    const calls = capture('get', '/api/orgs/:orgId/catalog/search')
    render(<Harness />)
    await userEvent.type(input(), 'pump')
    await waitFor(() => expect(options()).toHaveLength(20))
    const list = screen.getByRole('listbox')
    Object.defineProperty(list, 'scrollHeight', { value: 1000, configurable: true })
    Object.defineProperty(list, 'clientHeight', { value: 300, configurable: true })
    act(() => {
      list.scrollTop = 700
      fireEvent.scroll(list)
    })
    await waitFor(() => expect(options().length).toBeGreaterThan(20))
    expect(calls[calls.length - 1].url.searchParams.get('cursor')).toBe('20')
  })

  it('shows 1000+ when the total is capped', async () => {
    server.use(
      http.get('/api/orgs/:orgId/catalog/search', () =>
        HttpResponse.json({ items: [], total: 1000, totalCapped: true, date: '2026-09-29' }),
      ),
    )
    render(<Harness />)
    await userEvent.type(input(), 'ab')
    expect(await screen.findByText(/0 \/ 1000\+/)).toBeInTheDocument()
    expect(screen.getByText('Eşleşen parça yok')).toBeInTheDocument()
  })
})

describe('highlight', () => {
  it('marks every word, case- and dotless-i-insensitive', () => {
    expect(highlight('Hydraulic pump, 37', 'PUMP 37')).toEqual([
      { text: 'Hydraulic ', hit: false },
      { text: 'pump', hit: true },
      { text: ', ', hit: false },
      { text: '37', hit: true },
    ])
    expect(highlight('IŞIK', 'ışık')).toEqual([{ text: 'IŞIK', hit: true }])
    expect(highlight('abc', '')).toEqual([{ text: 'abc', hit: false }])
  })
})
