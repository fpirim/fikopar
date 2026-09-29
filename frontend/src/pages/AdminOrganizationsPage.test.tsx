import { describe, it, expect, vi, beforeEach } from 'vitest'
import { act } from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import AdminOrganizationsPage from './AdminOrganizationsPage'
import { ToastProvider } from '../context/ToastContext'
import api from '../lib/api'

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
const refresh = vi.fn()
vi.mock('../context/OrganizationContext', () => ({
  useOrganization: () => ({
    organizations: [],
    selectedId: null,
    selected: null,
    setSelectedId: vi.fn(),
    refresh,
    isLoading: false,
  }),
}))

beforeEach(() => {
  vi.clearAllMocks()
  ;(api.get as any).mockResolvedValue({ data: [] })
  ;(api.post as any).mockResolvedValue({ data: { rows: [], totalCount: 0 } })
  ;(api.delete as any).mockResolvedValue({ data: {} })
  URL.createObjectURL = vi.fn(() => 'blob:preview')
  URL.revokeObjectURL = vi.fn()
  h.props = {}
})

function renderPage() {
  return render(
    <MemoryRouter>
      <ToastProvider>
        <AdminOrganizationsPage />
      </ToastProvider>
    </MemoryRouter>,
  )
}

function selApi(opts: { rows?: any[]; selectAll?: boolean; toggled?: string[] }) {
  const nodes = (opts.rows || []).map((r) => ({ data: r, isSelected: () => true }))
  return {
    forEachNode: (cb: any) => nodes.forEach(cb),
    getServerSideSelectionState: () => ({
      selectAll: !!opts.selectAll,
      toggledNodes: opts.toggled || [],
    }),
    deselectAll: vi.fn(),
    refreshServerSide: vi.fn(),
    getFilterModel: () => ({}),
  }
}

describe('AdminOrganizationsPage', () => {
  it('opens the create-organization dialog (renders the modal backdrop)', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    expect(await screen.findByRole('heading', { name: 'Yeni Organizasyon' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Kapat' })).toBeInTheDocument()
  })

  it('grid search box shows clear (X) and clears', async () => {
    const u = userEvent.setup()
    renderPage()
    const box = screen.getByPlaceholderText('Ara...')
    await u.type(box, 'acme')
    await u.click(await screen.findByRole('button', { name: 'Temizle' }))
    expect(box).toHaveValue('')
  })

  it('bulk delete: selection enables Sil, confirm dialog deletes', async () => {
    const u = userEvent.setup()
    renderPage()
    act(() => {
      h.props.onSelectionChanged({
        api: selApi({ rows: [{ id: 7, name: 'Org', slug: 'org' }], toggled: ['7'] }),
      })
    })
    const del = await screen.findByRole('button', { name: 'Sil (1)' })
    expect(del).toBeEnabled()
    await u.click(del)
    expect(await screen.findByRole('heading', { name: 'Organizasyonları sil' })).toBeInTheDocument()
    await u.click(screen.getByRole('button', { name: /^Sil$/ }))
    await waitFor(() => expect(api.delete).toHaveBeenCalledWith('/admin/organizations/7'))
  })

  it('select-all covers totalCount and prints all matching', async () => {
    const u = userEvent.setup()
    const rows = [
      { id: 1, name: 'A', slug: 'a' },
      { id: 2, name: 'B', slug: 'b' },
    ]
    ;(api.post as any).mockResolvedValue({ data: { rows, totalCount: 2 } })
    const openSpy = vi.spyOn(window, 'open').mockReturnValue({
      document: { head: { innerHTML: '' }, body: { innerHTML: '' }, title: '' },
      focus: vi.fn(),
      print: vi.fn(),
    } as any)
    renderPage()

    await act(async () => {
      h.props.onGridReady({
        api: {
          setServerSideDatasource: (ds: any) =>
            ds.getRows({
              request: { startRow: 0, endRow: 50, sortModel: [], filterModel: {} },
              success: vi.fn(),
              fail: vi.fn(),
            }),
        },
      })
    })
    await act(async () => {
      await Promise.resolve()
      await Promise.resolve()
    })
    act(() => h.props.onSelectionChanged({ api: selApi({ rows: [rows[0]], selectAll: true }) }))

    await u.click(await screen.findByRole('button', { name: 'Yazdır (2)' }))
    await waitFor(() => expect(openSpy).toHaveBeenCalled())
  })

  it('creates an organization and shows a success toast', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 42, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Organizasyon' })
    const form = document.querySelector('form') as HTMLFormElement
    const texts = form.querySelectorAll('input[type="text"]')
    await u.type(texts[0] as HTMLInputElement, 'New Org')
    await u.type(texts[1] as HTMLInputElement, 'new-org')
    await u.click(screen.getByRole('button', { name: 'Oluştur' }))
    expect(await screen.findByText('Organizasyon oluşturuldu')).toBeInTheDocument()
    expect(api.post).toHaveBeenCalledWith(
      '/admin/organizations',
      expect.objectContaining({ slug: 'new-org' }),
    )
  })

  it('uploads a logo when creating an organization', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 42, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Organizasyon' })
    const form = document.querySelector('form') as HTMLFormElement
    const texts = form.querySelectorAll('input[type="text"]')
    await u.type(texts[0] as HTMLInputElement, 'New Org')
    await u.type(texts[1] as HTMLInputElement, 'new-org')
    await u.upload(
      form.querySelector('input[type="file"]') as HTMLInputElement,
      new File(['x'], 'logo.png', { type: 'image/png' }),
    )
    await u.click(screen.getByRole('button', { name: 'Oluştur' }))
    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/admin/organizations/42/logo',
        expect.anything(),
        expect.anything(),
      ),
    )
  })
})
