import { useState } from 'react'
import { describe, it, expect, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import ConfirmDialog from './ConfirmDialog'
import { findDialog } from '../../test/dialog'

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

  it('is a named dialog; Esc closes it and the focus goes back to the opener', async () => {
    function Page() {
      const [open, setOpen] = useState(false)
      return (
        <>
          <button type="button" onClick={() => setOpen(true)}>
            Delete
          </button>
          <ConfirmDialog
            open={open}
            title="Sil?"
            message="Emin misin?"
            onConfirm={vi.fn()}
            onClose={() => setOpen(false)}
          />
        </>
      )
    }
    render(<Page />)
    const opener = screen.getByRole('button', { name: 'Delete' })
    await userEvent.click(opener)
    expect(await findDialog('dialog', { name: 'Sil?' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Kapat' })).toBeInTheDocument()
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    await waitFor(() => expect(opener).toHaveFocus())
  })
})
