import { useState } from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import DialogShell from './DialogShell'

function Page() {
  const [open, setOpen] = useState(false)
  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>
        New card
      </button>
      {open && (
        <DialogShell
          title="Card"
          saving={false}
          submitLabel="Save"
          onSubmit={() => setOpen(false)}
          onClose={() => setOpen(false)}
        >
          <input aria-label="Name" />
        </DialogShell>
      )}
    </>
  )
}

describe('DialogShell', () => {
  it('is a named dialog with a close button in the UI language', async () => {
    render(<Page />)
    await userEvent.click(screen.getByRole('button', { name: 'New card' }))
    expect(await screen.findByRole('dialog', { name: 'Card' })).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Kapat' })).toBeInTheDocument()
  })

  it('closes on Esc and gives the focus back to the opener', async () => {
    render(<Page />)
    const opener = screen.getByRole('button', { name: 'New card' })
    await userEvent.click(opener)
    await screen.findByRole('dialog')
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    await waitFor(() => expect(opener).toHaveFocus())
  })

  it('submits the form', async () => {
    render(<Page />)
    await userEvent.click(screen.getByRole('button', { name: 'New card' }))
    await userEvent.click(await screen.findByRole('button', { name: 'Save' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
  })
})
