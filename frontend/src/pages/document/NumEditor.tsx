import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import type { ICellEditorParams } from 'ag-grid-community'
import { toInput } from '../../lib/documents'

type Props = ICellEditorParams & { ariaLabel: (rowNo: number) => string; text?: boolean }

// Typing a character on the cell starts here with it; Enter or F2 edits the current value.
const NumEditor = forwardRef<unknown, Props>(function NumEditor(props, ref) {
  const typed = props.eventKey && props.eventKey.length === 1 ? props.eventKey : null
  const cleared = props.eventKey === 'Backspace'
  const current = props.text ? String(props.value ?? '') : toInput(props.value)
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
      aria-label={props.ariaLabel(props.rowIndex + 1)}
      inputMode={props.text ? 'text' : 'decimal'}
      value={text}
      onChange={(e) => setText(e.target.value)}
      className={
        props.text
          ? 'h-full w-full bg-transparent px-2.5 text-grid outline-none'
          : 'h-full w-full bg-transparent px-2.5 text-right font-mono text-grid outline-none'
      }
    />
  )
})

export default NumEditor
