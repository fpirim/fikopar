import { describe, expect, it } from 'vitest'
import { http, HttpResponse } from 'msw'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { server } from '../mocks/node'
import { renderApp } from '../test/renderApp'
import { capture, captureDownloads } from '../test/msw'
import { getDocument } from '../api/endpoints'

const D = '/api/orgs/:orgId/documents'
const grid = () => document.querySelector('.fk-doc-lines') as HTMLElement
const cellText = (row: number, col: string) =>
  grid()
    .querySelector(`.ag-center-cols-container .ag-row[row-index="${row}"] [col-id="${col}"]`)
    ?.textContent?.trim()
const totals = () => screen.getByRole('region', { name: 'Toplamlar' })
const effects = () => screen.getByRole('region', { name: 'Kayıtta' })
const location = () => screen.getByTestId('location').textContent

async function loaded(path: string, admin = false) {
  renderApp(path, { admin })
  await waitFor(() => expect(cellText(0, 'rn')).toBe('1'))
}

describe('DocumentPage', () => {
  it('shows a draft like the mockup: next number, lines, stock colours, totals, effects, warnings', async () => {
    await loaded('/invoices/1001')
    expect(await screen.findByRole('heading', { level: 1 })).toHaveTextContent('SF-2026-1-000419')
    expect(screen.getByText('TASLAK')).toBeInTheDocument()
    expect(screen.getByLabelText('Belge türü')).toHaveValue('SALES_INVOICE_WITH_WAYBILL')
    await waitFor(() =>
      expect(screen.getByRole('combobox', { name: 'Cari' })).toHaveValue(
        '120.10482 · Yılmaz Tarım Makinaları',
      ),
    )
    expect(screen.getByLabelText('Plasiyer')).toHaveValue('7')
    expect(cellText(0, 'part')).toBe('HP-3774')
    expect(cellText(3, 'd2')).toBe('4,44')
    expect(cellText(3, 'net')).toBe('2.150,00')
    const stock = (row: number) =>
      grid().querySelector(
        `.ag-center-cols-container .ag-row[row-index="${row}"] [col-id="stock"]`,
      )!
    expect(stock(0)).toHaveClass('fk-stock-ok')
    expect(stock(1)).toHaveClass('fk-stock-short')
    expect(stock(3)).toHaveClass('fk-stock-none')
    expect(grid().querySelector('.ag-floating-bottom')).toHaveTextContent('4 satır · 32 adet')
    expect(totals()).toHaveTextContent('Brüt43.750,00')
    expect(totals()).toHaveTextContent('İskonto−3.150,00')
    expect(totals()).toHaveTextContent('Ara toplam40.600,00')
    expect(totals()).toHaveTextContent('KDV %208.120,00')
    expect(totals()).toHaveTextContent('Toplam48.720,00')
    expect(effects()).toHaveTextContent('+48.720,00 B')
    expect(effects()).toHaveTextContent('Stok · Depo 1−32')
    expect(within(effects()).getByText('%94')).toHaveClass('bg-fk-chip-amber')
    const warnings = screen.getByRole('list', { name: 'Uyarılar' })
    expect(warnings).toHaveTextContent('Satır 4 · BR-2210: 6 istendi, Depo 1 içinde 0')
    expect(warnings).toHaveTextContent('Satır 2 · CL-1150: fiyat liste fiyatının altında')
    expect(screen.getByRole('button', { name: 'Taslağı kaydet' })).toBeDisabled()
  })

  it('saves a changed draft with If-Match, holds stock and shows until when', async () => {
    const puts = capture('put', `${D}/:documentId`)
    await loaded('/invoices/1001')
    const date = screen.getByLabelText('Tarih')
    await userEvent.clear(date)
    await userEvent.type(date, '30.09.2026{Enter}')
    // The due date follows the date and the card's 30 days.
    expect(screen.getByLabelText('Vade')).toHaveValue('30.10.2026')
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    await waitFor(() => expect(puts).toHaveLength(1))
    expect(puts[0].headers.get('If-Match')).toBe('"3"')
    expect(puts[0].body).toMatchObject({
      type: 'SALES_INVOICE_WITH_WAYBILL',
      depotId: 1,
      documentDate: '2026-09-30',
      dueDate: '2026-10-30',
      salesRepUserId: 7,
      linkedDocumentId: null,
      waybillDocumentId: null,
      priceDirection: null,
    })
    expect((puts[0].body as { lines: unknown[] }).lines[0]).toEqual({
      partId: expect.any(Number),
      qty: '2',
      unitPrice: '12400',
      discount1Pct: '10',
      discount2Pct: '0',
      vatPct: '20',
      originLineId: null,
      billedLineId: null,
    })
    expect(await screen.findByText(/^Stok .* tarihine kadar ayrıldı$/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Taslağı kaydet' })).toBeDisabled()
  })

  it('a 412 on save offers a reload', async () => {
    server.use(
      http.put(`${D}/:documentId`, () =>
        HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 }),
      ),
    )
    await loaded('/invoices/1001')
    await userEvent.selectOptions(screen.getByLabelText('Plasiyer'), '9')
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    const dialog = await screen.findByRole('alertdialog')
    expect(dialog).toHaveTextContent('Başkası değiştirdi')
    server.resetHandlers()
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yeniden yükle' }))
    await waitFor(() => expect(screen.getByLabelText('Plasiyer')).toHaveValue('7'))
  })

  it('posts with an Idempotency-Key and If-Match, then shows the number and follow-ups', async () => {
    const posts = capture('post', `${D}/:documentId/post`)
    await loaded('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    expect(await screen.findByText('SF-2026-1-000419 kaydedildi')).toBeInTheDocument()
    expect(posts[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(posts[0].headers.get('If-Match')).toBe('"3"')
    expect(posts[0].body).toEqual({})
    expect(screen.getByText('KAYITLI')).toBeInTheDocument()
    expect(screen.getByRole('link', { name: 'İade oluştur' })).toHaveAttribute(
      'href',
      '/invoices/new?type=SALES_RETURN&original=1001',
    )
    expect(screen.getByRole('link', { name: 'Fiyat farkı' })).toHaveAttribute(
      'href',
      '/invoices/new?type=PRICE_DIFFERENCE&original=1001',
    )
    expect(screen.getByRole('region', { name: 'Kaydedildi' })).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: 'Kaydet' })).not.toBeInTheDocument()
    expect(screen.getByLabelText('Tarih')).toHaveAttribute('readonly')
  })

  it('a post over the credit limit shows the numbers; a user cannot override', async () => {
    await loaded('/invoices/1002')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    const dialog = await screen.findByRole('alertdialog')
    expect(dialog).toHaveTextContent('Kredi limiti250.000,00')
    expect(dialog).toHaveTextContent('Bakiye36.315,00')
    expect(dialog).toHaveTextContent('Bu belge305.280,00')
    expect(dialog).toHaveTextContent('Limit aşımı91.595,00')
    expect(dialog).toHaveTextContent('Bir yönetici gerekçe yazarak kaydedebilir')
    expect(within(dialog).queryByRole('button', { name: 'Yine de kaydet' })).toBeNull()
    await userEvent.click(within(dialog).getAllByRole('button', { name: 'Kapat' })[0])
    expect(screen.getByText('TASLAK')).toBeInTheDocument()
  })

  it('an admin posts over the limit with a reason; the retry reuses nothing stale', async () => {
    const posts = capture('post', `${D}/:documentId/post`)
    await loaded('/invoices/1002', true)
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    const dialog = await screen.findByRole('alertdialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yine de kaydet' }))
    expect(dialog).toHaveTextContent('En az 3 karakter')
    await userEvent.type(within(dialog).getByLabelText('Limiti aşma gerekçesi'), 'Havale geldi')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yine de kaydet' }))
    expect(await screen.findByText('SF-2026-1-000419 kaydedildi')).toBeInTheDocument()
    expect(posts).toHaveLength(2)
    expect(posts[1].body).toEqual({ creditOverride: { reason: 'Havale geldi' } })
    expect(posts[1].headers.get('Idempotency-Key')).not.toBe(
      posts[0].headers.get('Idempotency-Key'),
    )
    expect(screen.getByText('LİMİT AŞIMI')).toBeInTheDocument()
    expect(
      screen.getByText(/Demo kredi limitini aşarak kaydetti: Havale geldi/),
    ).toBeInTheDocument()
  })

  it('an override refused by the server keeps the dialog open with the reason', async () => {
    server.use(
      http.post(`${D}/:documentId/post`, async ({ request }) => {
        const body = (await request.json()) as { creditOverride?: unknown }
        return body.creditOverride
          ? HttpResponse.json({ code: 'ERR_FORBIDDEN' }, { status: 403 })
          : HttpResponse.json(
              {
                code: 'ERR_CREDIT_LIMIT_EXCEEDED',
                details: { limit: '1.00', balance: '0.00', documentTotal: '2.00', excess: '1.00' },
              },
              { status: 409 },
            )
      }),
    )
    await loaded('/invoices/1002', true)
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    const dialog = await screen.findByRole('alertdialog')
    await userEvent.type(within(dialog).getByLabelText('Limiti aşma gerekçesi'), 'Test')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yine de kaydet' }))
    expect(await within(dialog).findByRole('alert')).toBeInTheDocument()
  })

  it('maps line errors to the line and closed years to the reopen hint', async () => {
    server.use(
      http.post(`${D}/:documentId/post`, () =>
        HttpResponse.json(
          { code: 'ERR_RETURN_QTY_EXCEEDED', field: 'lines[1].qty' },
          { status: 422 },
        ),
      ),
    )
    await loaded('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Satır 2: Kalan miktardan fazlası iade ediliyor',
    )
    await waitFor(() =>
      expect(grid().querySelector('.ag-center-cols-container .ag-row[row-index="1"]')).toHaveClass(
        'fk-row-bad',
      ),
    )
    server.use(
      http.post(`${D}/:documentId/post`, () =>
        HttpResponse.json({ code: 'ERR_YEAR_CLOSED' }, { status: 409 }),
      ),
    )
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    expect(
      await screen.findByText('2026 yılı kapalı; önce 2027 devrini yeniden açın'),
    ).toBeInTheDocument()
  })

  it('deletes a draft after asking and goes back to the list', async () => {
    const dels = capture('delete', `${D}/:documentId`)
    await loaded('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: 'Sil' }))
    expect(screen.getByText('Taslak silinsin mi?')).toBeInTheDocument()
    await userEvent.click(screen.getAllByRole('button', { name: 'Sil' }).slice(-1)[0])
    await waitFor(() => expect(location()).toBe('/invoices'))
    expect(dels[0].headers.get('If-Match')).toBe('"3"')
  })

  it('cancelling asks a reason; dependents are listed with links', async () => {
    await loaded('/invoices/391')
    await userEvent.click(screen.getByRole('button', { name: 'Belgeyi iptal et' }))
    const dialog = screen.getByRole('dialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Belgeyi iptal et' }))
    expect(dialog).toHaveTextContent('En az 3 karakter')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Mükerrer')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Belgeyi iptal et' }))
    expect(await within(dialog).findByRole('link', { name: 'SI-2026-1-000012' })).toHaveAttribute(
      'href',
      '/invoices/12',
    )
    expect(within(dialog).getByRole('link', { name: 'FF-2026-1-000007' })).toBeInTheDocument()
    expect(dialog).toHaveTextContent('Bu belgeye bağlı iade veya fiyat farkı var')
  })

  it('cancels a posted invoice: reversal entry, opposite stock, negative stock toast', async () => {
    const cancels = capture('post', `${D}/:documentId/cancel`)
    await loaded('/invoices/201')
    await userEvent.click(screen.getByRole('button', { name: 'Belgeyi iptal et' }))
    await userEvent.type(screen.getByLabelText('Gerekçe'), 'Tedarikçi hatası')
    await userEvent.click(
      within(screen.getByRole('dialog')).getByRole('button', { name: 'Belgeyi iptal et' }),
    )
    expect(await screen.findByText('AF-2026-1-000201 iptal edildi')).toBeInTheDocument()
    expect(cancels[0].body).toEqual({ reason: 'Tedarikçi hatası' })
    expect(cancels[0].headers.get('If-Match')).toBe('"2"')
    expect(cancels[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(screen.getByText('İPTAL')).toBeInTheDocument()
    expect(screen.getByText(/^İptal 29\.09\.2026 · Tedarikçi hatası/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /^Ters fiş/ })).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Ters kayıt' })).toBeInTheDocument()
    expect(await screen.findByText(/CL-1150, depo 1 içinde −\d+ oluyor/)).toBeInTheDocument()
  })

  it('downloads the PDF of a document', async () => {
    const blobs = captureDownloads()
    await loaded('/invoices/418')
    await userEvent.click(screen.getByRole('button', { name: 'PDF' }))
    await waitFor(() => expect(blobs).toHaveLength(1))
  })

  it('bills a waybill: lines copied line for line, no stock, locked parts', async () => {
    const creates = capture('post', D)
    await loaded('/invoices/new?waybill=52')
    await waitFor(() => expect(cellText(0, 'part')).toBe('HP-3780'))
    expect(screen.getByLabelText('Belge türü')).toHaveValue('SALES_INVOICE')
    expect(screen.getByLabelText('Belge türü')).toBeDisabled()
    expect(effects()).toHaveTextContent('Stok—')
    expect(screen.getByRole('button', { name: /Satır ekle/ })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    await waitFor(() => expect(creates).toHaveLength(1))
    expect(creates[0].body).toMatchObject({
      type: 'SALES_INVOICE',
      waybillDocumentId: 52,
      lines: [
        { qty: '1', billedLineId: 5201 },
        { qty: '10', billedLineId: 5202 },
      ],
    })
    expect(creates[0].headers.get('Idempotency-Key')).toBeTruthy()
    await waitFor(() => expect(location()).toMatch(/^\/invoices\/\d+$/))
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('SF-2026-1-000419')
  })

  it('a return starts from the original with what is left of each line', async () => {
    await loaded('/invoices/new?type=SALES_RETURN&original=391')
    await waitFor(() => expect(cellText(1, 'part')).toBe('WP-3715'))
    expect(screen.getByLabelText('Asıl belge')).toHaveValue('391')
    expect(cellText(1, 'returnable')).toBe('3')
    expect(cellText(1, 'qty')).toBe('3')
    expect(effects()).toHaveTextContent('Stok · Depo 1+13')
    expect(effects()).toHaveTextContent('A')
  })

  it('a price difference is amount only and flips the card side by direction', async () => {
    const creates = capture('post', D)
    await loaded('/invoices/new?type=PRICE_DIFFERENCE&original=108')
    await waitFor(() => expect(cellText(0, 'origin')).toContain('HP-3774'))
    expect(screen.queryByRole('columnheader', { name: 'Miktar' })).toBeNull()
    expect(screen.getByRole('columnheader', { name: 'Tutar' })).toBeInTheDocument()
    expect(screen.getByRole('group', { name: 'Yön' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Azalış' }))
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    await waitFor(() => expect(creates).toHaveLength(1))
    // Lines without an amount are left out.
    expect(creates[0].body).toMatchObject({
      type: 'PRICE_DIFFERENCE',
      priceDirection: 'DECREASE',
      linkedDocumentId: 108,
      lines: [],
    })
  })

  it('refuses to save without an account', async () => {
    renderApp('/invoices/new?type=SALES_INVOICE')
    await userEvent.click(await screen.findByRole('button', { name: 'Taslağı kaydet' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('Cari seçin')
  })

  it('imports CSV lines through resolvePartCodes and reports unknown codes', async () => {
    const resolves = capture('post', '/api/orgs/:orgId/catalog/parts/resolve')
    renderApp('/invoices/new?type=SALES_INVOICE')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Cari' }), 'Yılmaz')
    await userEvent.click(await screen.findByRole('option', { name: /Yılmaz/ }))
    const file = new File(['kod;miktar;fiyat\nHP-3774;2;12.000,00\nNOPE-1;1;\n'], 'lines.csv', {
      type: 'text/csv',
    })
    await userEvent.upload(screen.getByLabelText('CSV içe aktar'), file)
    expect(
      await screen.findByText('1 satır eklendi; atlananlar: NOPE-1 (bulunamadı)'),
    ).toBeInTheDocument()
    expect(resolves[0].body).toEqual({ codes: ['HP-3774', 'NOPE-1'], depotId: 1 })
    await waitFor(() => expect(cellText(0, 'part')).toBe('HP-3774'))
    expect(cellText(0, 'price')).toBe('12.000,00')
    expect(cellText(0, 'd1')).toBe('10')
    expect(totals()).toHaveTextContent('Ara toplam20.520,00')
  })

  it('shows the account no. of the document without the card summary', async () => {
    server.use(
      http.get('/api/orgs/:orgId/sicils/:sicilId/summary', () =>
        HttpResponse.json({ code: 'ERR_INTERNAL' }, { status: 500 }),
      ),
    )
    await loaded('/invoices/391')
    await waitFor(() =>
      expect(screen.getByLabelText('Cari')).toHaveValue('120.10482 · Yılmaz Tarım Makinaları'),
    )
  })

  it("a new document takes the card's salesperson", async () => {
    const creates = capture('post', D)
    renderApp('/invoices/new?type=SALES_INVOICE')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Cari' }), 'Öztürk')
    await userEvent.click(await screen.findByRole('option', { name: /Öztürk/ }))
    await waitFor(() => expect(screen.getByLabelText('Plasiyer')).toHaveValue('9'))
    expect(screen.queryByRole('option', { name: 'Kart varsayılanı' })).toBeNull()
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    await waitFor(() => expect(creates).toHaveLength(1))
    expect(creates[0].body).toMatchObject({ salesRepUserId: 9 })
  })

  it('a card without a salesperson leaves the picker empty', async () => {
    renderApp('/invoices/new?type=SALES_INVOICE')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Cari' }), 'Kaya Ziraat')
    await userEvent.click(await screen.findByRole('option', { name: /Kaya Ziraat/ }))
    await waitFor(() =>
      expect(screen.getByLabelText('Plasiyer')).toHaveDisplayValue('Plasiyer yok'),
    )
  })

  it('the waybill picker asks only for waybills not billed yet', async () => {
    const grids = capture('post', `${D}/grid`)
    renderApp('/invoices/new?type=SALES_INVOICE')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Cari' }), 'Kaya Ziraat')
    await userEvent.click(await screen.findByRole('option', { name: /Kaya Ziraat/ }))
    expect(
      await within(screen.getByLabelText('İrsaliye ref.')).findByRole('option', {
        name: /SIR-2026-1-000052/,
      }),
    ).toBeInTheDocument()
    expect(grids[grids.length - 1].body).toMatchObject({
      types: ['SALES_WAYBILL'],
      statuses: ['POSTED'],
      billed: false,
    })
  })

  it('the original picker of a return asks for documents that bill no waybill', async () => {
    const grids = capture('post', `${D}/grid`)
    renderApp('/invoices/new?type=SALES_RETURN')
    await userEvent.type(await screen.findByRole('combobox', { name: 'Cari' }), 'Yılmaz')
    await userEvent.click(await screen.findByRole('option', { name: /Yılmaz/ }))
    expect(
      await within(screen.getByLabelText('Asıl belge')).findByRole('option', {
        name: /SF-2026-1-000391/,
      }),
    ).toBeInTheDocument()
    expect(grids[grids.length - 1].body).toMatchObject({ billsWaybill: false })
  })

  it('cancel lists the dependents the error carries without reading them', async () => {
    server.use(
      http.post(`${D}/:documentId/cancel`, () =>
        HttpResponse.json(
          {
            code: 'ERR_DOCUMENT_HAS_DEPENDENTS',
            details: {
              dependents: [{ id: 77, documentNo: 'SI-2026-9-000077', type: 'SALES_RETURN' }],
            },
          },
          { status: 409 },
        ),
      ),
    )
    await loaded('/invoices/391')
    const gets = capture('get', `${D}/:documentId`)
    await userEvent.click(screen.getByRole('button', { name: 'Belgeyi iptal et' }))
    const dialog = screen.getByRole('dialog')
    await userEvent.type(within(dialog).getByLabelText('Gerekçe'), 'Mükerrer')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Belgeyi iptal et' }))
    expect(await within(dialog).findByRole('link', { name: 'SI-2026-9-000077' })).toHaveAttribute(
      'href',
      '/invoices/77',
    )
    expect(gets).toHaveLength(0)
  })

  it('a draft that other drafts bill or link names them on delete and save', async () => {
    const refused = () =>
      HttpResponse.json(
        {
          code: 'ERR_DOCUMENT_HAS_DEPENDENTS',
          details: { dependents: [{ id: 2001, documentNo: null, type: 'SALES_INVOICE' }] },
        },
        { status: 409 },
      )
    server.use(http.delete(`${D}/:documentId`, refused), http.put(`${D}/:documentId`, refused))
    await loaded('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: 'Sil' }))
    await userEvent.click(screen.getAllByRole('button', { name: 'Sil' }).slice(-1)[0])
    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Bu taslağı faturalayan veya bağlayan taslaklar var')
    expect(within(alert).getByRole('link', { name: 'Taslak #2001' })).toHaveAttribute(
      'href',
      '/invoices/2001',
    )
    await userEvent.selectOptions(screen.getByLabelText('Plasiyer'), '9')
    await userEvent.click(screen.getByRole('button', { name: 'Taslağı kaydet' }))
    await waitFor(() =>
      expect(
        within(screen.getByRole('alert')).getByRole('link', { name: 'Taslak #2001' }),
      ).toBeInTheDocument(),
    )
  })

  it('the negative stock toast names the part of the warning', async () => {
    const d = await getDocument(1, 1001)
    server.use(
      http.post(`${D}/:documentId/post`, () =>
        HttpResponse.json({
          ...d,
          status: 'POSTED',
          documentNo: 'SF-2026-1-000419',
          version: d.version + 1,
          warnings: [
            {
              code: 'NEGATIVE_STOCK',
              partId: 999,
              stockNo: 'ZZ-0001',
              depotId: 1,
              onHandAfter: '-3',
            },
          ],
        }),
      ),
    )
    await loaded('/invoices/1001')
    await userEvent.click(screen.getByRole('button', { name: 'Kaydet' }))
    expect(await screen.findByText(/ZZ-0001, depo 1 içinde −3 oluyor/)).toBeInTheDocument()
  })

  it('an unknown document says so', async () => {
    renderApp('/invoices/999999')
    expect(await screen.findByText('Belge bulunamadı')).toBeInTheDocument()
  })
})
