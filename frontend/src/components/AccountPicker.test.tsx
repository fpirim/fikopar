import { describe, expect, it, vi } from 'vitest'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { AccountPicker } from './AccountPicker'

describe('AccountPicker', () => {
  it('moves through hits with the keyboard and picks with Enter', async () => {
    const onPick = vi.fn()
    render(<AccountPicker orgId={1} label="Hesap" onPick={onPick} />)
    const box = screen.getByRole('combobox', { name: 'Hesap' })
    await userEvent.type(box, 'Tarım')
    await waitFor(() => expect(screen.getAllByRole('option').length).toBeGreaterThan(1))
    await userEvent.keyboard('{ArrowDown}{ArrowUp}{ArrowUp}')
    const options = screen.getAllByRole('option')
    expect(options[options.length - 1]).toHaveAttribute('aria-selected', 'true')
    await userEvent.keyboard('{Enter}')
    expect(onPick).toHaveBeenCalledWith(expect.objectContaining({ sicilId: expect.any(Number) }))
    expect(screen.queryByRole('listbox')).not.toBeInTheDocument()
  })

  it('shows the picked account and closes on Escape', async () => {
    render(
      <AccountPicker
        orgId={1}
        label="Hesap"
        value={{ sicilId: 1006, accountId: 5006, hesapNo: '120.10517', hesapAdi: 'Kaya Ziraat' }}
        onPick={vi.fn()}
      />,
    )
    const box = screen.getByRole('combobox', { name: 'Hesap' })
    expect(box).toHaveValue('120.10517 · Kaya Ziraat')
    await userEvent.clear(box)
    await userEvent.type(box, 'Kaya')
    await screen.findByRole('listbox')
    await userEvent.keyboard('{Escape}')
    expect(box).toHaveValue('120.10517 · Kaya Ziraat')
  })
})
