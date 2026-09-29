import { useMemo } from 'react'
import type { RefObject } from 'react'
import { AgGridReact } from 'ag-grid-react'
import type { ColDef } from 'ag-grid-community'

const CHECKBOX_COL: ColDef = {
  headerName: '',
  colId: 'checkbox',
  width: 50,
  checkboxSelection: true,
  headerCheckboxSelection: true,
  sortable: false,
  resizable: false,
}

export default function CrudDataGrid({
  gridRef,
  columnDefs,
  getRowId,
  onGridReady,
  onSelectionChanged,
  onRowDoubleClicked,
  localeText,
}: Readonly<{
  gridRef: RefObject<AgGridReact | null>
  columnDefs: ColDef[]
  getRowId: (params: any) => string
  onGridReady: (params: any) => void
  onSelectionChanged: (e: any) => void
  onRowDoubleClicked: (e: any) => void
  localeText: Record<string, string>
}>) {
  const defaultColDef = useMemo<ColDef>(() => ({ sortable: true, resizable: true }), [])
  const cols = useMemo(() => [CHECKBOX_COL, ...columnDefs], [columnDefs])

  return (
    <div className="ag-theme-alpine min-h-0 flex-1 w-full">
      <AgGridReact
        ref={gridRef}
        columnDefs={cols}
        defaultColDef={defaultColDef}
        rowModelType="serverSide"
        getRowId={getRowId}
        onGridReady={onGridReady}
        onSelectionChanged={onSelectionChanged}
        onRowDoubleClicked={onRowDoubleClicked}
        rowSelection="multiple"
        cacheBlockSize={50}
        localeText={localeText}
        animateRows
      />
    </div>
  )
}
