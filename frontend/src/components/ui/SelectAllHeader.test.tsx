import { describe, it, expect, vi } from 'vitest'
import { act } from 'react'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import SelectAllHeader, { type SelectAllState } from './SelectAllHeader'

function fakeApi() {
  const listeners = new Set<() => void>()
  return {
    addEventListener: vi.fn((_: string, cb: () => void) => listeners.add(cb)),
    removeEventListener: vi.fn((_: string, cb: () => void) => listeners.delete(cb)),
    fire: () => listeners.forEach((cb) => cb()),
  }
}

describe('SelectAllHeader', () => {
  it('reflects the state and toggles select-all', async () => {
    const u = userEvent.setup()
    let state: SelectAllState = 'none'
    const onToggle = vi.fn((checked: boolean) => {
      state = checked ? 'all' : 'none'
    })
    const api = fakeApi()
    const { container, unmount } = render(
      <SelectAllHeader
        {...({ api } as any)}
        getState={() => state}
        onToggle={onToggle}
        ariaLabel="Tümünü Seç"
      />,
    )
    const box = screen.getByRole('checkbox', { name: 'Tümünü Seç' })
    expect(box).not.toBeChecked()

    await u.click(box)
    expect(onToggle).toHaveBeenCalledWith(true)
    expect(box).toBeChecked()
    expect(container.querySelector('.ag-checked')).not.toBeNull()

    state = 'some'
    act(() => api.fire())
    expect(box).not.toBeChecked()
    expect(container.querySelector('.ag-indeterminate')).not.toBeNull()

    await u.click(box)
    expect(onToggle).toHaveBeenLastCalledWith(true)

    unmount()
    expect(api.removeEventListener).toHaveBeenCalled()
  })
})
