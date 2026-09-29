import { describe, it, expect, vi, beforeEach } from 'vitest'
import { act } from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import SicilPage from './SicilPage'
import { ToastProvider } from '../context/ToastContext'
import api from '../lib/api'

// Controllable AG Grid stub — see AdminUsersPage.test.tsx for the same pattern.
const h = vi.hoisted(() => ({ props: {} as any }))
vi.mock('ag-grid-react', () => ({
  AgGridReact: (props: any) => {
    h.props = props
    return <div data-testid="grid" />
  },
}))
vi.mock('../lib/api', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
}))

const orgState = { selectedId: 7 as number | null }
vi.mock('../context/OrganizationContext', () => ({
  useOrganization: () => ({
    organizations: [{ id: 7, name: 'TestOrg' }],
    selectedId: orgState.selectedId,
    selected: orgState.selectedId ? { id: 7, name: 'TestOrg' } : null,
    setSelectedId: vi.fn(),
    refresh: vi.fn(),
    isLoading: false,
  }),
}))

beforeEach(() => {
  vi.clearAllMocks()
  orgState.selectedId = 7
  ;(api.post as any).mockResolvedValue({ data: { rows: [], totalCount: 0 } })
  ;(api.put as any).mockResolvedValue({ data: {} })
  ;(api.delete as any).mockResolvedValue({ data: {} })
  h.props = {}
})

function renderPage() {
  return render(
    <MemoryRouter>
      <ToastProvider>
        <SicilPage />
      </ToastProvider>
    </MemoryRouter>,
  )
}

describe('SicilPage', () => {
  it('without a selected org shows the prompt and does not mount the grid', () => {
    orgState.selectedId = null
    renderPage()
    expect(screen.getByText(/üst menüden bir organizasyon seçin/i)).toBeInTheDocument()
    expect(screen.queryByTestId('grid')).not.toBeInTheDocument()
  })

  it('adds the selected organization_id to the datasource POST body', async () => {
    renderPage()

    await act(async () => {
      h.props.onGridReady({
        api: {
          setDatasource: (ds: any) =>
            ds.getRows({
              startRow: 0,
              endRow: 50,
              sortModel: [],
              filterModel: {},
              successCallback: vi.fn(),
              failCallback: vi.fn(),
            }),
        },
      })
    })

    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/sicil/grid',
        expect.objectContaining({ organization_id: 7, startRow: 0, endRow: 50 }),
      ),
    )
  })

  it('the New button opens the account-card dialog', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    expect(await screen.findByRole('heading', { name: 'Yeni Hesap Kartı' })).toBeInTheDocument()
  })

  it('create flow POSTs /sicil with organization_id', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 11, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Hesap Kartı' })

    const form = document.querySelector('form') as HTMLFormElement
    const inputs = form.querySelectorAll('input[type="text"]')
    await u.type(inputs[1] as HTMLInputElement, '320.C.001')
    await u.type(inputs[2] as HTMLInputElement, 'Ceylan')

    await u.click(screen.getByRole('button', { name: 'Oluştur' }))

    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/sicil',
        expect.objectContaining({
          organization_id: 7,
          hesap_no: '320.C.001',
          hesap_adi: 'Ceylan',
          sicil_tipi: 'FIRMA',
          telefonlar: [],
        }),
      ),
    )
    expect(await screen.findByText('Hesap kartı oluşturuldu')).toBeInTheDocument()
  })

  it('add phone: type and number end up in the `telefonlar` array of the POST payload', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 12, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Hesap Kartı' })

    expect(screen.getByText(/henüz telefon eklenmedi/i)).toBeInTheDocument()

    await u.click(screen.getByRole('button', { name: /telefon ekle/i }))
    const tipiSelect = await screen.findByRole('combobox', { name: 'Telefonlar' })
    await u.selectOptions(tipiSelect, 'EV')

    const phoneInput = document.querySelector(
      '.react-tel-input input[type="tel"]',
    ) as HTMLInputElement
    expect(phoneInput).toBeTruthy()
    // userEvent.type fights the masked input's formatting, so set the value directly.
    phoneInput.focus()
    await u.type(phoneInput, '5551112233')

    const inputs = document.querySelectorAll('form input[type="text"]')
    await u.type(inputs[1] as HTMLInputElement, '001')
    await u.type(inputs[2] as HTMLInputElement, 'Alfa')

    await u.click(screen.getByRole('button', { name: 'Oluştur' }))

    await waitFor(() => expect(api.post).toHaveBeenCalled())
    const [, payload] = (api.post as any).mock.calls[0]
    expect(payload.telefonlar).toHaveLength(1)
    expect(payload.telefonlar[0].tipi).toBe('EV')
    expect(payload.telefonlar[0].numara).toMatch(/\+90/)
  })

  it('the delete button removes a phone row', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Hesap Kartı' })

    await u.click(screen.getByRole('button', { name: /telefon ekle/i }))
    expect(await screen.findByRole('combobox', { name: 'Telefonlar' })).toBeInTheDocument()
    const sil = screen.getByRole('button', { name: 'Sil' })
    await u.click(sil)
    await waitFor(() => expect(screen.getByText(/henüz telefon eklenmedi/i)).toBeInTheDocument())
  })

  it('edit flow does NOT send organization_id to PUT /sicil/:id (immutable)', async () => {
    const u = userEvent.setup()
    ;(api.put as any).mockResolvedValue({ data: {} })
    renderPage()
    act(() =>
      h.props.onRowDoubleClicked({
        data: {
          id: 5,
          organization_id: 7,
          hesap_no: '001',
          hesap_adi: 'Eski',
          adres: '',
          ilce: '',
          sehir: '',
          v_dairesi: '',
          v_no: '',
          telefon: '',
          tc_no: '',
          gun: '',
          iskonto: 0,
          sicil_tipi: 'FIRMA',
        },
      }),
    )
    await screen.findByRole('heading', { name: 'Hesap Kartını Düzenle' })

    const idInput = document.querySelector('input[readonly]') as HTMLInputElement
    expect(idInput.value).toBe('5')

    await u.click(screen.getByRole('button', { name: 'Güncelle' }))

    await waitFor(() => expect(api.put).toHaveBeenCalled())
    const [, payload] = (api.put as any).mock.calls[0]
    expect(payload).not.toHaveProperty('organization_id')
    expect(payload.hesap_adi).toBe('Eski')
  })

  it('new dialog shows a read-only ID field with the "*" placeholder', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Hesap Kartı' })
    const idInput = document.querySelector('input[readonly]') as HTMLInputElement
    expect(idInput.value).toBe('*')
  })
})
