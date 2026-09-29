import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'

const G = '/api/orgs/:orgId/documents/grid'
const rows = () => [...document.querySelectorAll('.fk-grid .ag-center-cols-container .ag-row')]
const pinned = () => document.querySelector('.fk-grid .ag-floating-bottom') as HTMLElement
const location = () => screen.getByTestId('location').textContent

describe('DocumentsPage', () => {
  it('lists every document newest first with totals that leave cancelled ones out', async () => {
    renderApp('/invoices')
    expect(await screen.findByRole('heading', { name: 'Faturalar ve iadeler' })).toBeInTheDocument()
    await waitFor(() => expect(pinned()).toHaveTextContent('14 / 14'))
    await waitFor(() => expect(rows()[0]).toHaveTextContent('Taslak'))
    expect(within(rows()[0] as HTMLElement).getByText('TASLAK')).toBeInTheDocument()
    expect(pinned()).toHaveTextContent('14 / 14 · iptaller hariç')
    // Σ of the posted documents and drafts, cancelled SF-2026-1-000415 left out.
    expect(pinned().querySelector('[col-id="grandTotal"]')).toHaveTextContent(/\d/)
    expect(await screen.findByText('SF-2026-1-000418')).toBeInTheDocument()
  })

  it('sends type, status, account, date and number filters in one request', async () => {
    const reqs = capture('post', G)
    renderApp('/invoices?depot=1')
    await waitFor(() => expect(rows().length).toBeGreaterThan(0))
    await userEvent.click(screen.getByRole('button', { name: 'Tüm türler' }))
    await userEvent.click(await screen.findByRole('menuitemcheckbox', { name: 'Satış faturası' }))
    await userEvent.keyboard('{Escape}')
    await userEvent.click(screen.getByRole('button', { name: 'KAYITLI' }))
    await userEvent.type(screen.getByRole('combobox', { name: 'Cari' }), 'Yılmaz')
    await userEvent.click(await screen.findByRole('option', { name: /Yılmaz/ }))
    await userEvent.type(screen.getByLabelText('Başlangıç'), '01.09.2026{Enter}')
    await userEvent.type(screen.getByLabelText('Numara'), '000418')
    await waitFor(() =>
      expect(reqs.slice(-1)[0].body).toMatchObject({
        depotId: 1,
        types: ['SALES_INVOICE'],
        statuses: ['POSTED'],
        sicilId: 1000,
        dateFrom: '2026-09-01',
        documentNo: '000418',
      }),
    )
    await waitFor(() => expect(rows()).toHaveLength(0))
    expect(location()).toContain('status=POSTED')
  })

  it('opens a document from its row and a new one by type', async () => {
    renderApp('/invoices')
    await waitFor(() => expect(rows().length).toBeGreaterThan(0))
    await userEvent.click(screen.getByText('SF-2026-1-000418'))
    await waitFor(() => expect(location()).toBe('/invoices/418'))
  })

  it('starts a new document of the picked type', async () => {
    renderApp('/invoices')
    await userEvent.click(await screen.findByRole('button', { name: 'Yeni belge' }))
    await userEvent.click(await screen.findByRole('menuitem', { name: 'Alış iadesi' }))
    await waitFor(() => expect(location()).toBe('/invoices/new?type=PURCHASE_RETURN'))
    expect(await screen.findByLabelText('Belge türü')).toHaveValue('PURCHASE_RETURN')
  })

  it('exports Excel from the server and CSV in the client', async () => {
    const blobs = captureDownloads()
    const reqs = capture('post', G)
    renderApp('/invoices')
    await waitFor(() => expect(rows().length).toBeGreaterThan(0))
    await userEvent.click(screen.getByRole('button', { name: 'Excel' }))
    await waitFor(() => expect(blobs).toHaveLength(1))
    expect(reqs.some((r) => r.headers.get('Accept')?.includes('spreadsheetml'))).toBe(true)
    await userEvent.click(screen.getByRole('button', { name: 'CSV' }))
    await waitFor(() => expect(blobs).toHaveLength(2))
    const csv = await blobs[1].text()
    expect(csv).toContain('SF-2026-1-000418')
    expect(csv.split('\r\n')[0]).toContain('Numara;Tür;Durum')
  })
})
