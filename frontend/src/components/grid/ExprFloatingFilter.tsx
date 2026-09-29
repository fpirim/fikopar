import { forwardRef, useImperativeHandle, useRef, useState } from 'react'
import type { IFloatingFilterParams, IFilter } from 'ag-grid-community'
import { useTranslation } from 'react-i18next'
import {
  describeCondition,
  parseCondition,
  type Condition,
  type FilterKind,
} from '@/lib/filterExpr'
import { cn } from '@/lib/utils'

export interface ExprFilterParams {
  kind: FilterKind | 'enum'
  placeholder?: string
  defaultOp?: 'contains' | 'startsWith' | 'equals'
  options?: { value: string; label: string }[]
  anyLabel?: string
}

type Props = IFloatingFilterParams & ExprFilterParams

const inputCls =
  'h-[26px] w-full min-w-0 rounded border font-normal border-fk-input bg-fk-surface px-1.5 text-xs text-fk-text outline-none placeholder:text-fk-faint focus:border-fk-accent focus:ring-1 focus:ring-fk-accent'

// One text box per column: "> 10000", "01.09.2026..30.09.2026", "=Konya"; enums get a select.
const ExprFloatingFilter = forwardRef<unknown, Props>(function ExprFloatingFilter(props, ref) {
  const { kind, placeholder, defaultOp = 'contains', options, anyLabel, column } = props
  const [text, setText] = useState('')
  const [invalid, setInvalid] = useState(false)
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined)
  const { t } = useTranslation('grid')
  const label = t('fk.filterCol', { col: column.getColDef().headerName ?? column.getColId() })

  useImperativeHandle(ref, () => ({
    onParentModelChanged(model: Condition | null) {
      setInvalid(false)
      setText(kind === 'enum' ? String(model?.filter ?? '') : describeCondition(model, defaultOp))
    },
  }))

  function apply(model: Condition | null) {
    props.parentFilterInstance((instance: IFilter) => {
      void Promise.resolve(instance.setModel(model)).then(() => props.api.onFilterChanged())
    })
  }

  function onText(value: string) {
    setText(value)
    clearTimeout(timer.current)
    timer.current = setTimeout(() => {
      const model = parseCondition(kind as FilterKind, value, defaultOp)
      setInvalid(model === undefined)
      if (model !== undefined) apply(model)
    }, 350)
  }

  if (kind === 'enum') {
    return (
      <select
        aria-label={label}
        value={text}
        onChange={(e) => {
          setText(e.target.value)
          apply(
            e.target.value ? { filterType: 'text', type: 'equals', filter: e.target.value } : null,
          )
        }}
        className={cn(inputCls, !text && 'text-fk-faint')}
      >
        <option value="">{anyLabel ?? ''}</option>
        {options?.map((o) => (
          <option key={o.value} value={o.value}>
            {o.label}
          </option>
        ))}
      </select>
    )
  }

  return (
    <input
      aria-label={label}
      aria-invalid={invalid || undefined}
      value={text}
      placeholder={placeholder}
      onChange={(e) => onText(e.target.value)}
      onKeyDown={(e) => {
        if (e.key === 'Enter') {
          clearTimeout(timer.current)
          const model = parseCondition(kind as FilterKind, text, defaultOp)
          setInvalid(model === undefined)
          if (model !== undefined) apply(model)
        }
      }}
      className={cn(
        inputCls,
        invalid && 'border-fk-negative focus:border-fk-negative focus:ring-fk-negative',
      )}
    />
  )
})

export default ExprFloatingFilter
