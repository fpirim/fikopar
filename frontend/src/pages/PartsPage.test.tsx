import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const pinned = () => document.querySelector('.ag-floating-bottom') as HTMLElement
const location = () => screen.getByTestId('location').textContent
const last = <T,>(xs: T[]) => xs[xs.length - 1]

describe('PartsPage', () => {
  it('loads every depot with a pinned total row and default sort', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    expect(await screen.findByRole('heading', { name: 'Parça ve fiyatlar' })).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: 'HP-3774' })).toHaveAttribute(
      'href',
      '/parts/1001',
    )
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

  it('scopes stock columns to the top-bar depot and shows min, max and shelf', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts?depot=1')
    expect(await screen.findByRole('link', { name: 'HP-3774' })).toHaveAttribute(
      'href',
      '/parts/1001?depot=1',
    )
    expect(screen.getByRole('columnheader', { name: 'Raf' })).toBeInTheDocument()
    expect(calls[0].body).toMatchObject({ depotId: 1 })
    const row = (await screen.findByRole('link', { name: 'HP-3774' })).closest('.ag-row')!
    const idx = row.getAttribute('row-index')
    const cells = document.querySelectorAll(`.ag-row[row-index="${idx}"] [col-id="shelf"]`)
    expect(cells[0]).toHaveTextContent('A-04-2')
  })

  it('filters by stock state from the segment and keeps it in the URL', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    await screen.findByRole('link', { name: 'HP-3774' })
    await userEvent.click(
      within(screen.getByRole('group', { name: 'Stok' })).getByRole('button', { name: 'Eksi' }),
    )
    await waitFor(() => expect(location()).toContain('stock=NEGATIVE'))
    await waitFor(() => expect(last(calls).body).toMatchObject({ stock: 'NEGATIVE' }))
    expect(await screen.findByRole('link', { name: 'BR-2215' })).toBeInTheDocument()
    expect((await screen.findAllByText('EKSİ')).length).toBeGreaterThan(0)
    expect(screen.queryByRole('link', { name: 'HP-3774' })).not.toBeInTheDocument()
  })

  it('opens with the dashboard link state and marks below-minimum rows', async () => {
    renderApp('/parts?stock=BELOW_MIN_OR_NEGATIVE&depot=1')
    expect(await screen.findByRole('link', { name: 'CL-1150' })).toBeInTheDocument()
    expect((await screen.findAllByText('MİN ALTI')).length).toBeGreaterThan(0)
    expect(
      within(screen.getByRole('group', { name: 'Stok' })).getByRole('button', {
        name: 'Min altı veya eksi',
      }),
    ).toHaveAttribute('aria-pressed', 'true')
  })

  it('sends code and number filters by PartColId', async () => {
    const calls = capture('post', '/api/orgs/:orgId/catalog/parts/grid')
    renderApp('/parts')
    await screen.findByRole('link', { name: 'HP-3774' })
    await userEvent.type(screen.getByLabelText('OEM / alt. filtresi'), '3774612')
    await waitFor(() =>
      expect(last(calls).body).toMatchObject({
        filterModel: { codes: { filterType: 'text', type: 'contains', filter: '3774612' } },
      }),
    )
    await waitFor(() => expect(pinned()).toHaveTextContent(/^1 \/ 1\.988/))
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
    await screen.findByRole('link', { name: 'HP-3774' })
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
