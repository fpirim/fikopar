import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const pinned = () => document.querySelector('.ag-floating-bottom') as HTMLElement
const location = () => screen.getByTestId('location').textContent
const last = <T,>(xs: T[]) => xs[xs.length - 1]
const firstPart = async () => (await screen.findAllByRole('link', { name: /^[A-Z]{2}-\d{4}$/ }))[0]

async function findHp() {
  await firstPart()
  await userEvent.type(screen.getByLabelText('Parça filtresi'), 'HP-3774')
  await screen.findByText('1–1 / 1')
  return screen.findByRole('link', { name: 'HP-3774' })
}

describe('PartsPage', () => {
  it('loads every depot with a pinned total row and default sort', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    expect(await screen.findByRole('heading', { name: 'Parça ve fiyatlar' })).toBeInTheDocument()
    expect(await firstPart()).toHaveAttribute('href', expect.stringMatching(/^\/parts\/\d+$/))
    await waitFor(() => expect(pinned()).toHaveTextContent(/1\.988 \/ 1\.988/))
    expect(calls[0].body).toMatchObject({
      depotId: null,
      stock: 'ALL',
      includeInactive: false,
      startRow: 0,
      sortModel: [{ colId: 'stockNo', sort: 'asc' }],
    })
    expect(screen.queryByRole('columnheader', { name: 'Raf' })).not.toBeInTheDocument()
  })

  it('shows on hand per depot side by side under all depots', async () => {
    renderApp('/parts')
    const hp = await findHp()
    expect(screen.getByRole('columnheader', { name: 'Dp 1' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Dp 2' })).toBeInTheDocument()
    const idx = hp.closest('.ag-row')!.getAttribute('row-index')
    const cell = (c: string) =>
      document.querySelector(
        `.ag-center-cols-container .ag-row[row-index="${idx}"] [col-id="${c}"]`,
      )
    expect(cell('depot.1')).toHaveTextContent('10')
    expect(cell('depot.2')).toHaveTextContent('4')
  })

  it('hides the per-depot columns when a depot is chosen', async () => {
    renderApp('/parts?depot=1')
    await findHp()
    expect(screen.queryByRole('columnheader', { name: 'Dp 1' })).not.toBeInTheDocument()
  })

  it('scopes stock columns to the top-bar depot and shows min, max and shelf', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts?depot=1')
    expect(await findHp()).toHaveAttribute('href', '/parts/1001?depot=1')
    expect(screen.getByRole('columnheader', { name: 'Raf' })).toBeInTheDocument()
    expect(calls[0].body).toMatchObject({ depotId: 1 })
    const row = (await findHp()).closest('.ag-row')!
    const idx = row.getAttribute('row-index')
    const cells = document.querySelectorAll(`.ag-row[row-index="${idx}"] [col-id="shelf"]`)
    expect(cells[0]).toHaveTextContent('A-04-2')
  })

  it('filters by stock state from the segment and keeps it in the URL', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    await firstPart()
    await userEvent.click(
      within(screen.getByRole('group', { name: 'Stok' })).getByRole('button', { name: 'Eksi' }),
    )
    await waitFor(() => expect(location()).toContain('stock=NEGATIVE'))
    await waitFor(() => expect(last(calls).body).toMatchObject({ stock: 'NEGATIVE' }))
    expect(await screen.findByRole('link', { name: 'BR-2215' })).toBeInTheDocument()
    expect((await screen.findAllByText('EKSİ')).length).toBeGreaterThan(0)
    expect(screen.queryByText('MİN ALTI')).not.toBeInTheDocument()
    expect(await screen.findByText(/^1–\d+ \/ \d+$/)).toBeVisible()
  })

  it('opens with the dashboard link state and marks below-minimum rows', async () => {
    renderApp('/parts?stock=BELOW_MIN_OR_NEGATIVE&depot=1')
    await firstPart()
    expect((await screen.findAllByText('MİN ALTI')).length).toBeGreaterThan(0)
    expect(
      within(screen.getByRole('group', { name: 'Stok' })).getByRole('button', {
        name: 'Min altı veya eksi',
      }),
    ).toHaveAttribute('aria-pressed', 'true')
    await waitFor(() => expect(pinned()).toHaveTextContent('/'))
    expect(await screen.findByText(/^1–\d+ \/ \d+$/)).toBeVisible()
  })

  it('sends code and number filters by PartColId', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    await firstPart()
    await userEvent.type(screen.getByLabelText('OEM / alternatif filtresi'), '3774612')
    await waitFor(() =>
      expect(last(calls).body).toMatchObject({
        filterModel: { codes: { filterType: 'text', type: 'contains', filter: '3774612' } },
      }),
    )
    await waitFor(() => expect(pinned()).toHaveTextContent('1 / 1.988 · 1 filtre'))
    await userEvent.type(screen.getByLabelText('Liste fiyatı filtresi'), '> 10.000')
    await waitFor(() =>
      expect(last(calls).body).toMatchObject({
        filterModel: { listPrice: { filterType: 'number', type: 'greaterThan', filter: 10000 } },
      }),
    )
  })

  it('narrows to a catalog and includes inactive parts on request', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    await firstPart()
    await userEvent.selectOptions(screen.getByLabelText('Katalog'), '12 · Hydraulics 2026')
    await waitFor(() => expect(last(calls).body).toMatchObject({ catalogId: 1 }))
    expect(location()).toContain('catalog=1')
    await userEvent.click(screen.getByLabelText('Pasifler dahil'))
    await waitFor(() => expect(last(calls).body).toMatchObject({ includeInactive: true }))
  })

  it('downloads Excel from the server and CSV from the client', async () => {
    const blobs = captureDownloads()
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts?stock=NEGATIVE')
    await screen.findByRole('link', { name: 'BR-2215' })
    await userEvent.click(screen.getByRole('button', { name: 'Excel' }))
    await waitFor(() => expect(blobs).toHaveLength(1))
    expect(last(calls).headers.get('Accept')).toContain('spreadsheetml')
    expect(last(calls).body).toMatchObject({ stock: 'NEGATIVE' })
    await userEvent.click(screen.getByRole('button', { name: 'CSV' }))
    await waitFor(() => expect(blobs).toHaveLength(2))
    const text = await blobs[1].text()
    expect(text).toContain('Parça;Açıklama')
    expect(text).toContain('BR-2215')
  })

  it('links to the price list import', async () => {
    renderApp('/parts')
    await userEvent.click(await screen.findByRole('link', { name: 'Fiyat listesi aktar' }))
    expect(location()).toBe('/parts/import')
  })
})
