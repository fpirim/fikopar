import { useMemo } from 'react'
import type { RefObject } from 'react'
import { AgGridReact } from 'ag-grid-react'
import type { ColDef } from 'ag-grid-community'
import SelectAllHeader, { type SelectAllState } from './SelectAllHeader'

export default function CrudDataGrid({
  gridRef,
  columnDefs,
  getRowId,
  onGridReady,
  onSelectionChanged,
  getSelectAllState,
  onSelectAllToggle,
  onRowDoubleClicked,
  localeText,
}: Readonly<{
  gridRef: RefObject<AgGridReact | null>
  columnDefs: ColDef[]
  getRowId: (params: any) => string
  onGridReady: (params: any) => void
  onSelectionChanged: (e: any) => void
  getSelectAllState: () => SelectAllState
  onSelectAllToggle: (checked: boolean) => void
  onRowDoubleClicked: (e: any) => void
  localeText: Record<string, string>
}>) {
  const defaultColDef = useMemo<ColDef>(() => ({ sortable: true, resizable: true }), [])
  const checkboxCol = useMemo<ColDef>(
    () => ({
      headerName: '',
      colId: 'checkbox',
      width: 50,
      checkboxSelection: true,
      headerComponent: SelectAllHeader,
      headerComponentParams: {
        getState: getSelectAllState,
        onToggle: onSelectAllToggle,
        ariaLabel: localeText.selectAll,
      },
      sortable: false,
      resizable: false,
    }),
    [getSelectAllState, onSelectAllToggle, localeText.selectAll],
  )
  const cols = useMemo(() => [checkboxCol, ...columnDefs], [checkboxCol, columnDefs])

  return (
    <div className="ag-theme-alpine min-h-0 flex-1 w-full">
      <AgGridReact
        ref={gridRef}
        columnDefs={cols}
        defaultColDef={defaultColDef}
        rowModelType="infinite"
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
