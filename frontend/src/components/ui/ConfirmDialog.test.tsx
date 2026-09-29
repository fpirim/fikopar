import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ConfirmDialog from './ConfirmDialog'

describe('ConfirmDialog', () => {
  it('renders nothing when closed', () => {
    render(
      <ConfirmDialog
        open={false}
        title="Başlık"
        message="Mesaj"
        onConfirm={vi.fn()}
        onClose={vi.fn()}
      />,
    )
    expect(screen.queryByText('Başlık')).not.toBeInTheDocument()
  })

  it('renders title/message and fires onConfirm / onClose', async () => {
    const onConfirm = vi.fn()
    const onClose = vi.fn()
    render(
      <ConfirmDialog
        open
        title="Sil?"
        message="Emin misin?"
        confirmText="Sil"
        onConfirm={onConfirm}
        onClose={onClose}
      />,
    )
    expect(screen.getByText('Sil?')).toBeInTheDocument()
    expect(screen.getByText('Emin misin?')).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: 'Sil' }))
    expect(onConfirm).toHaveBeenCalledTimes(1)

    await userEvent.click(screen.getByRole('button', { name: 'İptal' }))
    expect(onClose).toHaveBeenCalledTimes(1)
  })

  it('shows loading state and disables actions', () => {
    render(
      <ConfirmDialog
        open
        loading
        title="T"
        message="M"
        confirmText="Sil"
        onConfirm={vi.fn()}
        onClose={vi.fn()}
      />,
    )
    expect(screen.getByRole('button', { name: 'İşleniyor...' })).toBeDisabled()
  })
})
