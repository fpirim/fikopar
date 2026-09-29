import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'
import { server } from '../mocks/node'

const pinned = () => document.querySelector('.ag-floating-bottom') as HTMLElement
const row = (no: string) => screen.getByText(no).closest('[role="row"]') as HTMLElement
const counted = (no: string) => row(no).querySelector('[col-id="countedQty"]') as HTMLElement
const LINE = '/api/orgs/:orgId/stock/counts/:countId/lines/:lineId'

async function shelfA0() {
  renderApp('/stock-counts/2')
  await userEvent.type(await screen.findByLabelText('Raf filtresi'), 'A-0')
  await waitFor(() => expect(pinned()).toHaveTextContent(/7 \/ [\d.]+ · raf A-0\*/))
  await screen.findByText('GS-4410')
}

async function count(no: string, text: string) {
  await userEvent.dblClick(counted(no))
  const input = await screen.findByLabelText(`Sayılan ${no}`)
  await userEvent.clear(input)
  await userEvent.type(input, text)
}

describe('StockCountPage', () => {
  it('shows the header, progress and the shelf totals of mockup 10', async () => {
    await shelfA0()
    expect(screen.getByRole('heading', { name: 'SAY-2026-1-000002' })).toBeInTheDocument()
    expect(screen.getByText('SÜRÜYOR')).toBeInTheDocument()
    expect(screen.getByText('Depo 1 · başlangıç 28.09.2026')).toBeInTheDocument()
    expect(screen.getByText(/^412 \/ [\d.]+ satır$/)).toBeInTheDocument()
    expect(pinned()).toHaveTextContent('+270,00')
    expect(row('HP-3774')).toHaveTextContent('HAREKET −2')
    expect(row('CL-1150')).toHaveTextContent('−2.850,00')
  })

  it('narrows lines with the show buttons and a shelf range', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/counts/:countId/lines/grid')
    renderApp('/stock-counts/2')
    const diff = await screen.findByRole('button', { name: 'Yalnız farklar' })
    await userEvent.click(diff)
    expect(diff).toHaveAttribute('aria-pressed', 'true')
    await waitFor(() => expect(calls[calls.length - 1].body).toMatchObject({ only: 'DIFFERENCES' }))
    await userEvent.type(screen.getByLabelText('Raf başı'), 'C{Enter}')
    await waitFor(() =>
      expect(calls[calls.length - 1].body).toMatchObject({ only: 'DIFFERENCES', shelfFrom: 'C' }),
    )
    await userEvent.click(diff)
    await waitFor(() => expect(calls[calls.length - 1].body).toMatchObject({ only: 'ALL' }))
  })

  it('autosaves a count with the line version and moves on', async () => {
    const saves = capture('patch', LINE)
    await shelfA0()
    await count('GS-4410', '17{Enter}')
    await waitFor(() => expect(saves).toHaveLength(1))
    expect(saves[0].body).toEqual({ countedQty: '17' })
    expect(saves[0].headers.get('If-Match')).toBe('"2"')
    await waitFor(() => expect(row('GS-4410')).toHaveTextContent('−1'))
    expect(await within(counted('GS-4410')).findByLabelText('Kaydedildi')).toBeInTheDocument()
    await waitFor(() => expect(pinned()).toHaveTextContent('41'))
  })

  it('restores the value on Esc without saving', async () => {
    const saves = capture('patch', LINE)
    await shelfA0()
    await count('HP-3702', '9{Escape}')
    expect(counted('HP-3702')).toHaveTextContent('6')
    expect(saves).toHaveLength(0)
  })

  it('sends null when a count is cleared', async () => {
    const saves = capture('patch', LINE)
    await shelfA0()
    await userEvent.dblClick(counted('HP-3702'))
    await userEvent.clear(await screen.findByLabelText('Sayılan HP-3702'))
    await userEvent.keyboard('{Enter}')
    await waitFor(() => expect(saves[0]?.body).toEqual({ countedQty: null }))
  })

  it('shows who counted first on 412 and keeps the own count on request', async () => {
    const saves = capture('patch', LINE)
    await shelfA0()
    await count('BE-0620', '32{Enter}')
    const dialog = await screen.findByRole('alertdialog')
    expect(dialog).toHaveTextContent('Başkası saydı')
    expect(dialog).toHaveTextContent('a.kaya, BE-0620 için')
    expect(dialog).toHaveTextContent('31')
    await userEvent.click(within(dialog).getByRole('button', { name: '32 kalsın' }))
    await waitFor(() => expect(saves).toHaveLength(2))
    expect(saves[1].headers.get('If-Match')).toBe('"3"')
    await waitFor(() => expect(counted('BE-0620')).toHaveTextContent('32'))
  })

  it('accepts the other count', async () => {
    await shelfA0()
    await count('BE-0620', '32{Enter}')
    const dialog = await screen.findByRole('alertdialog')
    await userEvent.click(within(dialog).getByRole('button', { name: '31 kullan' }))
    await waitFor(() => expect(counted('BE-0620')).toHaveTextContent('31'))
    expect(row('BE-0620')).toHaveTextContent('a.kaya')
  })

  it('posts differences with the header version and shows negative stock', async () => {
    const posts = capture('post', '/api/orgs/:orgId/stock/counts/:countId/finalize')
    renderApp('/stock-counts/2')
    await userEvent.click(await screen.findByRole('button', { name: 'Farkları işle' }))
    const dialog = await screen.findByRole('dialog')
    expect(within(dialog).getByRole('group', { name: 'Sayılan satır' })).toHaveTextContent('412')
    expect(within(dialog).getByRole('group', { name: 'Net' })).toBeInTheDocument()
    await userEvent.click(within(dialog).getByRole('button', { name: '0 say' }))
    await userEvent.click(within(dialog).getByRole('button', { name: 'İşle' }))
    await waitFor(() => expect(posts).toHaveLength(1))
    expect(posts[0].body).toEqual({ uncounted: 'ZERO' })
    expect(posts[0].headers.get('If-Match')).toBe('"1"')
    expect(posts[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(
      await within(dialog).findByText(/SAY-2026-1-000002 işlendi · \d+ düzeltme/),
    ).toBeVisible()
    expect(within(dialog).getByText('İşlemden sonra eksi stok')).toBeInTheDocument()
    await userEvent.click(within(dialog).getByRole('button', { name: 'Tamam' }))
    expect(await screen.findByText('KAPANDI')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Farkları işle' })).not.toBeInTheDocument()
  })

  it('asks to reload when the header changed before posting', async () => {
    server.use(
      http.post('/api/orgs/:orgId/stock/counts/:countId/finalize', () =>
        HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 }),
      ),
    )
    renderApp('/stock-counts/2')
    await userEvent.click(await screen.findByRole('button', { name: 'Farkları işle' }))
    await userEvent.click(
      within(await screen.findByRole('dialog')).getByRole('button', { name: 'İşle' }),
    )
    const alert = await screen.findByRole('alertdialog')
    expect(alert).toHaveTextContent('Sayım değişti')
    await userEvent.click(within(alert).getByRole('button', { name: 'Yeniden yükle' }))
    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
  })

  it('cancels the count with a reason', async () => {
    const calls = capture('post', '/api/orgs/:orgId/stock/counts/:countId/cancel')
    renderApp('/stock-counts/2')
    await userEvent.click(await screen.findByRole('button', { name: 'Sayımı iptal et' }))
    const dialog = await screen.findByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Wrong depot')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Sayımı iptal et' }))
    expect(await screen.findByText('İPTAL')).toBeInTheDocument()
    expect(calls[0].body).toEqual({ reason: 'Wrong depot' })
    expect(calls[0].headers.get('If-Match')).toBe('"1"')
  })

  it('downloads the count sheet', async () => {
    const blobs = captureDownloads()
    const calls = capture('get', '/api/orgs/:orgId/stock/counts/:countId')
    renderApp('/stock-counts/2')
    await userEvent.click(await screen.findByRole('button', { name: 'Sayım listesi PDF' }))
    await userEvent.click(
      await screen.findByRole('menuitem', { name: 'Tekrar sayım listesi (sayılmayan)' }),
    )
    await waitFor(() => expect(blobs).toHaveLength(1))
    const pdf = calls.find((c) => c.headers.get('Accept') === 'application/pdf')!
    expect(pdf.url.searchParams.get('sheet')).toBe('BLIND')
    expect(pdf.url.searchParams.get('only')).toBe('NOT_COUNTED')
  })

  it('shows a finalized count read-only', async () => {
    renderApp('/stock-counts/1')
    expect(await screen.findByText('KAPANDI')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Farkları işle' })).not.toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Sayımı iptal et' })).not.toBeInTheDocument()
  })
})
