import { useState } from 'react'
import { describe, it, expect } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { Dialog, DialogContent, DialogTitle } from './dialog'
import { findDialog } from '../../test/dialog'

// Opened from state, as the editors do: no Radix trigger to return to.
function Page() {
  const [open, setOpen] = useState(false)
  return (
    <>
      <button type="button" onClick={() => setOpen(true)}>
        Cancel document
      </button>
      {open && (
        <Dialog open onOpenChange={(o) => !o && setOpen(false)}>
          <DialogContent aria-describedby={undefined}>
            <DialogTitle>Cancel</DialogTitle>
            <input aria-label="Reason" autoFocus />
          </DialogContent>
        </Dialog>
      )}
    </>
  )
}

describe('DialogContent', () => {
  it('labels its close button in the UI language', async () => {
    render(<Page />)
    await userEvent.click(screen.getByRole('button', { name: 'Cancel document' }))
    expect(await screen.findByRole('button', { name: 'Kapat' })).toBeInTheDocument()
  })

  it('gives the focus back to what opened it on Esc', async () => {
    render(<Page />)
    const opener = screen.getByRole('button', { name: 'Cancel document' })
    await userEvent.click(opener)
    await waitFor(() => expect(screen.getByLabelText('Reason')).toHaveFocus())
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    await waitFor(() => expect(opener).toHaveFocus())
  })

  it('returns to the last focused control when the opener lost the focus first', async () => {
    function Busy() {
      const [open, setOpen] = useState(false)
      const [busy, setBusy] = useState(false)
      return (
        <>
          <button
            type="button"
            disabled={busy}
            onClick={() => {
              setBusy(true)
              ;(document.activeElement as HTMLElement).blur()
              setTimeout(() => {
                setBusy(false)
                setOpen(true)
              })
            }}
          >
            Post
          </button>
          {open && (
            <Dialog open onOpenChange={(o) => !o && setOpen(false)}>
              <DialogContent aria-describedby={undefined}>
                <DialogTitle>Credit limit</DialogTitle>
              </DialogContent>
            </Dialog>
          )}
        </>
      )
    }
    render(<Busy />)
    const post = screen.getByRole('button', { name: 'Post' })
    await userEvent.click(post)
    await findDialog('dialog')
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(post).toHaveFocus())
  })
})
