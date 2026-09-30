import { afterEach, describe, expect, it, vi } from 'vitest'
import { act, fireEvent, render, screen } from '@testing-library/react'
import type { IFloatingFilterParams } from 'ag-grid-community'
import { APP_DELAYS, delays } from '@/lib/delays'
import ExprFloatingFilter from './ExprFloatingFilter'

function setup() {
  const setModel = vi.fn()
  const onFilterChanged = vi.fn()
  const props = {
    kind: 'text',
    column: { getColDef: () => ({ headerName: 'Şehir' }), getColId: () => 'city' },
    parentFilterInstance: (cb: (f: { setModel: typeof setModel }) => void) => cb({ setModel }),
    api: { onFilterChanged },
  } as unknown as IFloatingFilterParams & { kind: 'text' }
  render(<ExprFloatingFilter {...props} />)
  return { setModel, input: screen.getByLabelText('Şehir filtresi') }
}

describe('ExprFloatingFilter debounce', () => {
  afterEach(() => vi.useRealTimers())

  it('applies the typed text once, 350 ms after the last change', () => {
    delays.gridFilter = APP_DELAYS.gridFilter
    vi.useFakeTimers()
    const { setModel, input } = setup()
    for (const v of ['K', 'Ko', 'Konya']) {
      fireEvent.change(input, { target: { value: v } })
      act(() => vi.advanceTimersByTime(200))
    }
    expect(setModel).not.toHaveBeenCalled()
    act(() => vi.advanceTimersByTime(149))
    expect(setModel).not.toHaveBeenCalled()
    act(() => vi.advanceTimersByTime(1))
    expect(setModel).toHaveBeenCalledTimes(1)
    expect(setModel).toHaveBeenCalledWith({ filterType: 'text', type: 'contains', filter: 'Konya' })
  })

  it('applies at once on Enter', () => {
    delays.gridFilter = APP_DELAYS.gridFilter
    vi.useFakeTimers()
    const { setModel, input } = setup()
    fireEvent.change(input, { target: { value: 'Konya' } })
    fireEvent.keyDown(input, { key: 'Enter' })
    expect(setModel).toHaveBeenCalledTimes(1)
    act(() => vi.advanceTimersByTime(1000))
    expect(setModel).toHaveBeenCalledTimes(1)
  })
})
