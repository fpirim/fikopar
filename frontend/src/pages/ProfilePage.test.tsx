import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ToastProvider } from '../context/ToastContext'
import ProfilePage from './ProfilePage'
import api from '../lib/api'

vi.mock('../hooks/useAuth', () => ({
  useAuth: () => ({
    user: { name: 'A', email: 'a@x.com', avatar_url: null },
    refreshUser: vi.fn(),
  }),
}))
vi.mock('../lib/api', () => ({
  default: { put: vi.fn(), post: vi.fn(), delete: vi.fn() },
}))

beforeEach(() => {
  vi.clearAllMocks()
  ;(api.put as any).mockResolvedValue({ data: {} })
  ;(api.post as any).mockResolvedValue({ data: {} })
  URL.createObjectURL = vi.fn(() => 'blob:preview')
  URL.revokeObjectURL = vi.fn()
})

function renderPage() {
  return render(
    <ToastProvider>
      <ProfilePage />
    </ToastProvider>,
  )
}

describe('ProfilePage', () => {
  it('updates the profile and shows a success toast', async () => {
    const u = userEvent.setup()
    renderPage()
    await u.click(screen.getByRole('button', { name: 'Güncelle' }))
    expect(await screen.findByRole('status')).toHaveTextContent('Profil güncellendi')
    expect(api.put).toHaveBeenCalledWith('/users/me', expect.objectContaining({ email: 'a@x.com' }))
  })

  it('shows an error toast when the update fails', async () => {
    const u = userEvent.setup()
    ;(api.put as any).mockRejectedValue({ response: { data: { code: 'ERR_INTERNAL' } } })
    renderPage()
    await u.click(screen.getByRole('button', { name: 'Güncelle' }))
    expect(await screen.findByRole('status')).toHaveTextContent(
      'Sunucu hatası, lütfen tekrar deneyin',
    )
  })

  it('uploads a newly selected avatar on save', async () => {
    const u = userEvent.setup()
    renderPage()
    const fileInput = document.querySelector('input[type="file"]') as HTMLInputElement
    await u.upload(fileInput, new File(['x'], 'a.png', { type: 'image/png' }))
    await u.click(screen.getByRole('button', { name: 'Güncelle' }))
    await waitFor(() =>
      expect(api.post).toHaveBeenCalledWith(
        '/users/me/avatar',
        expect.anything(),
        expect.anything(),
      ),
    )
  })
})
