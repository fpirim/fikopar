import { describe, expect, it, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '../i18n'
import ConflictDialog from './ConflictDialog'

describe('ConflictDialog', () => {
  it('only offers a reload, also on Escape', async () => {
    const onReload = vi.fn()
    render(<ConflictDialog title="Kart değişti" message="Yeniden yükleyin" onReload={onReload} />)
    const dialog = screen.getByRole('alertdialog', { name: 'Kart değişti' })
    expect(dialog).toHaveTextContent('Yeniden yükleyin')
    await userEvent.click(screen.getByRole('button', { name: 'Yeniden yükle' }))
    expect(onReload).toHaveBeenCalledTimes(1)
    await userEvent.keyboard('{Escape}')
    expect(onReload).toHaveBeenCalledTimes(2)
  })
})
