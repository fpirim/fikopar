import { useEffect, useState } from 'react'
import type { IHeaderParams } from 'ag-grid-community'

export type SelectAllState = 'all' | 'some' | 'none'

// The infinite row model has no header checkbox, so the grid hook owns the select-all state.
export default function SelectAllHeader(
  props: IHeaderParams & {
    getState: () => SelectAllState
    onToggle: (checked: boolean) => void
    ariaLabel?: string
  },
) {
  const { api, getState, onToggle, ariaLabel } = props
  const [state, setState] = useState<SelectAllState>(getState)

  useEffect(() => {
    const update = () => setState(getState())
    update()
    api.addEventListener('selectionChanged', update)
    return () => api.removeEventListener('selectionChanged', update)
  }, [api, getState])

  const cls = { all: 'ag-checked', some: 'ag-indeterminate', none: '' }[state]

  return (
    <div className={`ag-checkbox-input-wrapper ${cls}`}>
      <input
        type="checkbox"
        className="ag-input-field-input ag-checkbox-input"
        aria-label={ariaLabel}
        checked={state === 'all'}
        onChange={() => {
          onToggle(state !== 'all')
          setState(getState())
        }}
      />
    </div>
  )
}
