import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import LoginPage from './LoginPage'
import { useAuth } from '../hooks/useAuth'

const navigate = vi.fn()
vi.mock('react-router-dom', async (orig) => ({
  ...(await orig<typeof import('react-router-dom')>()),
  useNavigate: () => navigate,
}))
vi.mock('../hooks/useAuth', () => ({ useAuth: vi.fn() }))

const mockedUseAuth = vi.mocked(useAuth)

async function fillAndSubmit() {
  const user = userEvent.setup()
  const email = document.querySelector('input[type="email"]') as HTMLInputElement
  const password = document.querySelector('input[type="password"]') as HTMLInputElement
  await user.type(email, 'a@b.com')
  await user.type(password, 'secret')
  await user.click(screen.getByRole('button', { name: /giriş yap/i }))
}

beforeEach(() => {
  navigate.mockReset()
  mockedUseAuth.mockReset()
})

describe('LoginPage', () => {
  it('navigates to / on successful login', async () => {
    const login = vi.fn().mockResolvedValue(undefined)
    mockedUseAuth.mockReturnValue({ login } as never)

    render(<LoginPage />)
    await fillAndSubmit()

    expect(login).toHaveBeenCalledWith('a@b.com', 'secret')
    await waitFor(() => expect(navigate).toHaveBeenCalledWith('/'))
  })

  it('shows an inline error when login fails (does not reload)', async () => {
    const login = vi.fn().mockRejectedValue({
      response: { data: { code: 'ERR_INVALID_CREDENTIALS' } },
    })
    mockedUseAuth.mockReturnValue({ login } as never)

    render(<LoginPage />)
    await fillAndSubmit()

    expect(await screen.findByText('Geçersiz e-posta veya şifre')).toBeInTheDocument()
    expect(navigate).not.toHaveBeenCalled()
  })
})
