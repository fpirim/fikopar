import { describe, it, expect, vi, beforeEach } from 'vitest'
import { act } from 'react'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import AdminUsersPage from './AdminUsersPage'
import { ToastProvider } from '../context/ToastContext'
import api from '../lib/api'

// Controllable AG Grid stub: capture the props so tests can drive grid callbacks.
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

const orgs = [
  { id: 1, name: 'Acme', slug: 'acme' },
  { id: 2, name: 'Beta', slug: 'beta' },
]

beforeEach(() => {
  vi.clearAllMocks()
  ;(api.get as any).mockResolvedValue({ data: orgs })
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
        <AdminUsersPage />
      </ToastProvider>
    </MemoryRouter>,
  )
}

// Fake grid api for onSelectionChanged.
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

describe('AdminUsersPage', () => {
  it('opens the create-user dialog (renders the modal backdrop)', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    expect(await screen.findByRole('heading', { name: 'Yeni Kullanıcı' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Kapat' })).toBeInTheDocument()
  })

  it('searchable org dropdown: focus lists, filter, add chip, remove', async () => {
    const u = userEvent.setup()
    renderPage()
    await waitFor(() => expect(api.get).toHaveBeenCalledWith('/admin/organizations'))
    await u.click(screen.getByRole('button', { name: /yeni/i }))

    const orgInput = await screen.findByPlaceholderText('Organizasyon ara ve ekle...')
    await u.click(orgInput) // focus -> dropdown lists all (sorted by name)
    expect(await screen.findByRole('button', { name: 'Acme' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Beta' })).toBeInTheDocument()

    await u.type(orgInput, 'bet') // filter
    expect(screen.queryByRole('button', { name: 'Acme' })).not.toBeInTheDocument()
    await u.click(screen.getByRole('button', { name: 'Beta' })) // add

    // Chip with remove button appears.
    expect(await screen.findByRole('button', { name: 'Kaldır' })).toBeInTheDocument()
    await u.click(screen.getByRole('button', { name: 'Kaldır' }))
    expect(screen.queryByRole('button', { name: 'Kaldır' })).not.toBeInTheDocument()
  })

  it('grid search box shows clear (X) and clears', async () => {
    const u = userEvent.setup()
    renderPage()
    const box = screen.getByPlaceholderText('Ara...')
    await u.type(box, 'acme')
    const clear = await screen.findByRole('button', { name: 'Temizle' })
    await u.click(clear)
    expect(box).toHaveValue('')
  })

  it('bulk delete: selection enables Sil, confirm dialog deletes', async () => {
    const u = userEvent.setup()
    renderPage()
    act(() => {
      h.props.onSelectionChanged({
        api: selApi({ rows: [{ id: 5, name: 'X', email: 'x@x', role: 'user' }], toggled: ['5'] }),
      })
    })
    const del = await screen.findByRole('button', { name: 'Sil (1)' })
    expect(del).toBeEnabled()
    await u.click(del)
    expect(await screen.findByRole('heading', { name: 'Kullanıcıları sil' })).toBeInTheDocument()
    await u.click(screen.getByRole('button', { name: /^Sil$/ }))
    await waitFor(() => expect(api.delete).toHaveBeenCalledWith('/admin/users/5'))
  })

  it('select-all covers totalCount and prints all matching', async () => {
    const u = userEvent.setup()
    const rows = [
      { id: 1, name: 'A', email: 'a@x', role: 'user', organizations: [] },
      { id: 2, name: 'B', email: 'b@x', role: 'admin', organizations: [] },
      { id: 3, name: 'C', email: 'c@x', role: 'user', organizations: [] },
    ]
    ;(api.post as any).mockResolvedValue({ data: { rows, totalCount: 3 } })
    const openSpy = vi.spyOn(window, 'open').mockReturnValue({
      document: { head: { innerHTML: '' }, body: { innerHTML: '' }, title: '' },
      focus: vi.fn(),
      print: vi.fn(),
    } as any)
    renderPage()

    // Drive the datasource once so totalCountRef is populated.
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

    await u.click(await screen.findByRole('button', { name: 'Yazdır (3)' }))
    await waitFor(() => expect(openSpy).toHaveBeenCalled())
  })

  it('creates a user and shows a success toast', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 99, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Kullanıcı' })
    const form = document.querySelector('form') as HTMLFormElement
    await u.type(form.querySelector('input[type="text"]') as HTMLInputElement, 'New User')
    await u.type(form.querySelector('input[type="email"]') as HTMLInputElement, 'n@x.com')
    await u.type(form.querySelector('input[type="password"]') as HTMLInputElement, 'pw123')
    await u.click(screen.getByRole('button', { name: 'Oluştur' }))
    expect(await screen.findByText('Kullanıcı oluşturuldu')).toBeInTheDocument()
    expect(api.post).toHaveBeenCalledWith(
      '/admin/users',
      expect.objectContaining({ name: 'New User' }),
    )
  })

  it('uploads an avatar when creating a user', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { id: 99, rows: [], totalCount: 0 } })
    renderPage()
    await u.click(screen.getByRole('button', { name: /yeni/i }))
    await screen.findByRole('heading', { name: 'Yeni Kullanıcı' })
    const form = document.querySelector('form') as HTMLFormElement
    await u.type(form.querySelector('input[type="text"]') as HTMLInputElement, 'New User')
    await u.type(form.querySelector('input[type="email"]') as HTMLInputElement, 'n@x.com')
    await u.type(form.querySelector('input[type="password"]') as HTMLInputElement, 'pw123')
    await u.upload(
      form.querySelector('input[type="file"]') as HTMLInputElement,
      new File(['x'], 'a.png', { type: 'image/png' }),
    )
    await u.click(screen.getByRole('button', { name: 'Oluştur' }))
    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/admin/users/99/avatar',
        expect.anything(),
        expect.anything(),
      ),
    )
  })

  it('edits a user and uploads a new avatar', async () => {
    const u = userEvent.setup()
    ;(api.post as any).mockResolvedValue({ data: { avatar_url: '/new.png' } })
    renderPage()
    act(() =>
      h.props.onRowDoubleClicked({
        data: { id: 5, name: 'Old', email: 'o@x.com', role: 'user', organizations: [] },
      }),
    )
    await screen.findByRole('heading', { name: 'Kullanıcıyı Düzenle' })
    const form = document.querySelector('form') as HTMLFormElement
    await u.upload(
      form.querySelector('input[type="file"]') as HTMLInputElement,
      new File(['x'], 'a.png', { type: 'image/png' }),
    )
    await u.click(screen.getByRole('button', { name: 'Güncelle' }))
    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/admin/users/5/avatar',
        expect.anything(),
        expect.anything(),
      ),
    )
    await waitFor(() =>
      expect(api.put).toHaveBeenCalledWith(
        '/admin/users/5',
        expect.objectContaining({ name: 'Old' }),
      ),
    )
  })
})
