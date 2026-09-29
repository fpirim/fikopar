import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, act } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { ToastProvider, useToast } from './ToastContext'

function Demo() {
  const t = useToast()
  return (
    <div>
      <button onClick={() => t.success('Kaydedildi')}>ok</button>
      <button onClick={() => t.error('Hata')}>err</button>
    </div>
  )
}

describe('ToastContext', () => {
  it('shows a toast and closes it via the X button', async () => {
    const u = userEvent.setup()
    render(
      <ToastProvider>
        <Demo />
      </ToastProvider>,
    )
    await u.click(screen.getByText('ok'))
    expect(screen.getByText('Kaydedildi')).toBeInTheDocument()
    await u.click(screen.getByRole('button', { name: 'Kapat' }))
    expect(screen.queryByText('Kaydedildi')).not.toBeInTheDocument()
  })

  it('auto-dismisses after the timeout', () => {
    vi.useFakeTimers()
    try {
      render(
        <ToastProvider>
          <Demo />
        </ToastProvider>,
      )
      act(() => {
        fireEvent.click(screen.getByText('err'))
      })
      expect(screen.getByText('Hata')).toBeInTheDocument()
      act(() => {
        vi.advanceTimersByTime(4000)
      })
      expect(screen.queryByText('Hata')).not.toBeInTheDocument()
    } finally {
      vi.useRealTimers()
    }
  })

  it('throws when useToast is used outside the provider', () => {
    const spy = vi.spyOn(console, 'error').mockImplementation(() => {})
    function Bad() {
      useToast()
      return null
    }
    expect(() => render(<Bad />)).toThrow(/ToastProvider/)
    spy.mockRestore()
  })
})
