import { forwardRef, useEffect, useImperativeHandle, useRef, useState } from 'react'
import type { ICellEditorParams } from 'ag-grid-community'
import type { CatalogHit, Depot } from '../../api/types'
import CatalogSearch from '../../components/catalog/CatalogSearch'

interface PartEditorParams {
  orgId: number
  depots: Depot[]
  date: string
  onPicked: (rowIndex: number) => void
}

type Props = ICellEditorParams & PartEditorParams

// Part cell: typing searches the catalog (F2 popup); Enter takes the highlighted part.
const PartEditor = forwardRef<unknown, Props>(function PartEditor(props, ref) {
  const typed = props.eventKey && props.eventKey.length === 1 ? props.eventKey : null
  const [query, setQuery] = useState(typed ?? '')
  const [open, setOpen] = useState(Boolean(typed))
  const picked = useRef<CatalogHit | null>(null)
  const box = useRef<HTMLDivElement>(null)

  useImperativeHandle(ref, () => ({ getValue: () => picked.current ?? props.value }), [props.value])

  useEffect(() => {
    const input = box.current?.querySelector('input')
    input?.focus()
    if (!typed) input?.select()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <div
      ref={box}
      className="h-full w-full"
      onKeyDown={(e) => {
        // The search input swallows Esc; close the popup and give the cell its old value back.
        if (e.key !== 'Escape') return
        e.stopPropagation()
        const { api, rowIndex, column } = props
        api.stopEditing(true)
        api.setFocusedCell(rowIndex, column)
      }}
    >
      <CatalogSearch
        orgId={props.orgId}
        depots={props.depots}
        date={props.date}
        open={open}
        onOpenChange={setOpen}
        query={query}
        onQueryChange={(q) => {
          setQuery(q)
          setOpen(q.trim().length > 0)
        }}
        onPick={(hit) => {
          picked.current = hit
          setOpen(false)
          props.stopEditing()
          props.onPicked(props.rowIndex)
        }}
        below
        inputClassName="h-[32px] rounded-none border-0 bg-transparent px-1.5 focus-within:border-0"
      />
    </div>
  )
})

export default PartEditor
