import { describe, expect, it } from 'vitest'
import { screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { server } from '../mocks/node'
import { capture } from '../test/msw'
import { renderApp } from '../test/renderApp'

const location = () => screen.getByTestId('location').textContent
const IMPORTS = '/api/orgs/:orgId/catalog/imports'

const CSV = [
  'Stok No;Açıklama;Fiyat',
  'HP-3774;Hydraulic pump, 3-cyl.;13.200,00',
  'HP-9001;Hydraulic pump, 6-cyl.;18.500,00',
  'HP-3702;Hydraulic pump, 2-cyl.;abc',
].join('\n')

function uploads() {
  const seen: { form: FormData; headers: Headers }[] = []
  server.use(
    http.post(IMPORTS, async ({ request }) => {
      seen.push({ form: await request.clone().formData(), headers: request.headers })
      return undefined
    }),
  )
  return seen
}

async function upload(text = CSV, name = 'hyd.csv') {
  renderApp('/parts/import')
  const file = new File([text], name, { type: 'text/csv' })
  await userEvent.upload(await screen.findByLabelText('Dosya'), file)
  await userEvent.click(screen.getByRole('button', { name: 'Yükle ve önizle' }))
}

async function toPreview() {
  await upload()
  await waitFor(() => expect(location()).toBe('/parts/import/701'))
  await screen.findByRole('tab', { name: /Yeni\s*1/ })
}

async function confirmApply() {
  await userEvent.click(screen.getByRole('button', { name: 'Uygulamaya geç' }))
  await userEvent.click(screen.getByRole('checkbox', { name: 'Hatalı satırları atla' }))
  await userEvent.click(screen.getByRole('button', { name: 'Uygula' }))
  const dialog = await screen.findByRole('alertdialog')
  await userEvent.click(within(dialog).getByRole('button', { name: 'Uygula' }))
}

describe('PriceImportPage', () => {
  it('uploads a CSV, maps headers by name and previews the diff', async () => {
    const seen = uploads()
    await toPreview()
    expect(seen[0].form.get('catalogId')).toBe('1')
    expect(seen[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(screen.getByRole('tab', { name: /Fiyat\s*1/ })).toBeInTheDocument()
    expect(screen.getByRole('tab', { name: /Hatalı\s*1/ })).toBeInTheDocument()
    expect(await screen.findByText('HP-9001')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('tab', { name: /Hatalı/ }))
    expect(await screen.findByText(/Geçersiz sayı/)).toBeInTheDocument()
    expect(screen.queryByText('HP-9001')).not.toBeInTheDocument()
  })

  it('sends the LISAKT preset and the CSV options', async () => {
    const seen = uploads()
    renderApp('/parts/import')
    await screen.findByRole('option', { name: /LISAKT/ })
    await userEvent.selectOptions(screen.getByLabelText('Biçim'), 'LISAKT')
    await userEvent.upload(screen.getByLabelText('Dosya'), new File(['x'], 'lisakt.txt'))
    await userEvent.click(screen.getByRole('button', { name: 'Yükle ve önizle' }))
    await waitFor(() => expect(seen).toHaveLength(1))
    expect(JSON.parse(await (seen[0].form.get('mapping') as Blob).text())).toEqual({
      preset: 'LISAKT',
    })
  })

  it('takes a .txt file under the automatic format as LISAKT', async () => {
    const seen = uploads()
    renderApp('/parts/import')
    await screen.findByRole('option', { name: /LISAKT/ })
    await userEvent.upload(screen.getByLabelText('Dosya'), new File(['x'], 'LISAKT.TXT'))
    expect(screen.getByLabelText('Biçim')).toHaveValue('LISAKT')
    expect(screen.queryByLabelText('Ayraç')).not.toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Yükle ve önizle' }))
    await waitFor(() => expect(seen).toHaveLength(1))
    expect(JSON.parse(await (seen[0].form.get('mapping') as Blob).text())).toEqual({
      preset: 'LISAKT',
    })
  })

  it('asks for the columns it could not match and saves the mapping with If-Match', async () => {
    const patches = capture('patch', `${IMPORTS}/:importId`)
    await upload('Kod;Tutar\nHP-3774;13.200,00\n')
    expect(await screen.findByRole('heading', { name: 'Sütun eşleme' })).toBeInTheDocument()
    expect(screen.getByRole('columnheader', { name: 'Tutar' })).toBeInTheDocument()
    await userEvent.selectOptions(screen.getByLabelText('Stok no sütunu'), 'Kod')
    await userEvent.selectOptions(screen.getByLabelText('Fiyat sütunu'), 'Tutar')
    await userEvent.click(screen.getByRole('button', { name: 'Eşlemeyi kaydet' }))
    await screen.findByRole('tab', { name: /Fiyat\s*1/ })
    expect(patches[0].headers.get('If-Match')).toBe('"1"')
    expect(patches[0].body).toMatchObject({
      mapping: { columns: { code: { header: 'Kod' }, price: { header: 'Tutar' } } },
    })
  })

  it('reloads the job when someone else changed the mapping', async () => {
    await upload('Kod;Tutar\nHP-3774;13.200,00\n')
    await screen.findByRole('heading', { name: 'Sütun eşleme' })
    server.use(
      http.patch(`${IMPORTS}/:importId`, () =>
        HttpResponse.json({ code: 'ERR_VERSION_CONFLICT' }, { status: 412 }),
      ),
    )
    await userEvent.selectOptions(screen.getByLabelText('Stok no sütunu'), 'Kod')
    await userEvent.selectOptions(screen.getByLabelText('Fiyat sütunu'), 'Tutar')
    await userEvent.click(screen.getByRole('button', { name: 'Eşlemeyi kaydet' }))
    const dialog = await screen.findByRole('alertdialog')
    await userEvent.click(within(dialog).getByRole('button', { name: 'Yeniden yükle' }))
    await waitFor(() => expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument())
  })

  it('applies with skipped error rows, If-Match and an idempotency key', async () => {
    const applies = capture('post', `${IMPORTS}/:importId/apply`)
    await toPreview()
    await userEvent.click(screen.getByRole('button', { name: 'Uygulamaya geç' }))
    const apply = screen.getByRole('button', { name: 'Uygula' })
    expect(apply).toBeDisabled()
    const steps = screen.getByRole('list', { name: 'Aktarım adımları' })
    await userEvent.click(within(steps).getByRole('button', { name: /Önizleme/ }))
    await confirmApply()
    expect(await screen.findByRole('heading', { name: 'Sonuç' })).toBeInTheDocument()
    const result = screen.getByRole('list', { name: 'Sonuç' })
    expect(within(result).getByText('Oluşturulan parça').nextSibling).toHaveTextContent('1')
    expect(within(result).getByText('Eklenen fiyat').nextSibling).toHaveTextContent('2')
    expect(within(result).getByText('Atlanan satır').nextSibling).toHaveTextContent('1')
    expect(applies[0].headers.get('If-Match')).toBe('"1"')
    expect(applies[0].headers.get('Idempotency-Key')).toBeTruthy()
    expect(applies[0].body).toEqual({ skipErrorRows: true, applyAttributes: true, missing: 'KEEP' })
    expect(screen.getByRole('link', { name: 'Parçalara git' })).toHaveAttribute('href', '/parts')
  })

  it('refreshes a stale preview', async () => {
    const patches = capture('patch', `${IMPORTS}/:importId`)
    await toPreview()
    server.use(
      http.post(
        `${IMPORTS}/:importId/apply`,
        () => HttpResponse.json({ code: 'ERR_IMPORT_STALE' }, { status: 409 }),
        { once: true },
      ),
    )
    await confirmApply()
    expect(
      await screen.findByText('Önizlemeden sonra katalog değişti; yenileyin'),
    ).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Önizlemeyi yenile' }))
    await screen.findByRole('tab', { name: /Yeni\s*1/ })
    expect(patches[0].body).toEqual({})
    expect(patches[0].headers.get('If-Match')).toBe('"1"')
  })

  it('polls while the apply runs past the request', async () => {
    await toPreview()
    const job = (status: string, version: number, result: unknown = null) => ({
      id: 701,
      catalogId: 1,
      catalogCode: 12,
      fileName: 'hyd.csv',
      fileSha256: 'a'.repeat(64),
      fileSize: 1,
      format: 'CSV',
      validFrom: '2026-09-29',
      status,
      counts: {
        rows: 3,
        new: 1,
        price: 1,
        attributes: 0,
        unchanged: 0,
        missing: 0,
        delete: 0,
        errors: 1,
        warnings: 0,
      },
      createdAt: '2026-09-29T12:00:00Z',
      expiresAt: '2026-10-06T12:00:00Z',
      version,
      result,
    })
    let polls = 0
    server.use(
      http.post(`${IMPORTS}/:importId/apply`, () =>
        HttpResponse.json(job('APPLYING', 2), { status: 202 }),
      ),
      http.get(`${IMPORTS}/:importId`, () => {
        polls++
        return HttpResponse.json(
          polls < 2
            ? job('APPLYING', 2)
            : job('APPLIED', 3, {
                partsCreated: 1,
                pricesInserted: 2,
                partsUpdated: 0,
                partsDeactivated: 0,
                rowsSkipped: 1,
              }),
        )
      }),
    )
    await confirmApply()
    expect(await screen.findByText('Uygulanıyor…')).toBeInTheDocument()
    expect(await screen.findByRole('heading', { name: 'Sonuç' })).toBeInTheDocument()
    expect(polls).toBeGreaterThanOrEqual(2)
  })

  it('discards the job and lists recent imports', async () => {
    await toPreview()
    await userEvent.click(screen.getByRole('button', { name: 'Aktarımı at' }))
    expect(await screen.findByText('Bu aktarım atıldı.')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('link', { name: 'Yeni aktarım' }))
    const recent = await screen.findByRole('region', { name: 'Son aktarımlar' })
    expect(within(recent).getByRole('link', { name: 'hyd.csv' })).toHaveAttribute(
      'href',
      '/parts/import/701',
    )
    expect(within(recent).getByText('ATILDI')).toBeInTheDocument()
  })

  it('shows an upload error', async () => {
    server.use(
      http.post(IMPORTS, () => HttpResponse.json({ code: 'ERR_FILE_TOO_LARGE' }, { status: 400 })),
    )
    await upload()
    expect(await screen.findByRole('alert')).toHaveTextContent(/büyük/)
  })
})
