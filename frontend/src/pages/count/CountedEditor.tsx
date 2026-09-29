import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import type { ICellEditorParams } from 'ag-grid-community'
import { numberSeparators } from '../../lib/format'

type Props = ICellEditorParams & { ariaLabel: (stockNo: string) => string }

// Typing a digit on the cell starts here with that digit; Enter or double click edits the value.
const CountedEditor = forwardRef<unknown, Props>(function CountedEditor(props, ref) {
  const typed = props.eventKey && /^[\d.,]$/.test(props.eventKey) ? props.eventKey : null
  const cleared = props.eventKey === 'Backspace' || props.eventKey === 'Delete'
  const current =
    props.value === null || props.value === undefined
      ? ''
      : String(props.value).replace('.', numberSeparators().decimal)
  const [text, setText] = useState(typed ?? (cleared ? '' : current))
  const input = useRef<HTMLInputElement>(null)

  useImperativeHandle(ref, () => ({ getValue: () => text }), [text])

  useEffect(() => {
    input.current?.focus()
    if (!typed && !cleared) input.current?.select()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <input
      ref={input}
      aria-label={props.ariaLabel(props.data?.stockNo ?? '')}
      inputMode="decimal"
      value={text}
      onChange={(e) => setText(e.target.value)}
      className="h-full w-full bg-transparent px-2.5 text-right font-mono text-grid outline-none"
    />
  )
})

export default CountedEditor
