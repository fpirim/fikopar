import { useCallback, useState } from 'react'
import type { ReactNode } from 'react'
import type { ColDef } from 'ag-grid-community'
import { useTranslation } from 'react-i18next'
import { printRows, type PrintColumn } from '../../lib/print'
import { useToast } from '../../context/ToastContext'
import { useGridLocale } from '../../hooks/useGridLocale'
import { useServerSideGrid } from '../../hooks/useServerSideGrid'
import CrudDataGrid from './CrudDataGrid'
import ConfirmDialog from './ConfirmDialog'
import GridToolbar from './GridToolbar'

export default function CrudGridPage<T extends { id: number }>({
  title,
  gridPath,
  deletePath,
  columnDefs,
  printTitle,
  printColumns,
  messages,
  renderDialog,
  extraBody,
  onMutated,
}: Readonly<{
  title: string
  gridPath: string
  deletePath: (id: number) => string
  columnDefs: ColDef[]
  printTitle: string
  printColumns: PrintColumn<T>[]
  messages: {
    deleteTitle: string
    deleteConfirm: (count: number) => string
    deletedN: (count: number) => string
    deleteFailedN: (count: number) => string
    deleteFailed: string
  }
  renderDialog: (args: { row?: T; onSaved: () => void; onClose: () => void }) => ReactNode
  extraBody?: Record<string, unknown>
  // Lets the page refresh state outside the grid (e.g. the shared org dropdown).
  onMutated?: () => void
}>) {
  const { t } = useTranslation('common')
  const gridLocale = useGridLocale()
  const grid = useServerSideGrid<T>({ gridPath, deletePath, extraBody })
  const [confirmOpen, setConfirmOpen] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [editing, setEditing] = useState<T | undefined>(undefined)
  const [creating, setCreating] = useState(false)
  const dialogOpen = creating || editing !== undefined
  const toast = useToast()

  const onRowDoubleClicked = useCallback((e: any) => setEditing(e.data), [])

  const closeDialog = () => {
    setCreating(false)
    setEditing(undefined)
  }

  const confirmDelete = async () => {
    setDeleting(true)
    try {
      const { ok, failed } = await grid.deleteSelected()
      if (ok) {
        toast.success(messages.deletedN(ok))
        onMutated?.()
      }
      if (failed) toast.error(messages.deleteFailedN(failed))
    } catch {
      toast.error(messages.deleteFailed)
    } finally {
      setDeleting(false)
      setConfirmOpen(false)
    }
  }

  const handlePrint = async () => {
    if (grid.selectedCount === 0) return
    const rows = await grid.resolveSelectedRows()
    printRows<T>(printTitle, printColumns, rows)
  }

  return (
    <div className="flex h-full flex-col gap-4">
      <h1 className="text-2xl font-bold">{title}</h1>

      <GridToolbar
        selectedCount={grid.selectedCount}
        search={grid.search}
        onSearchChange={grid.setSearch}
        onNew={() => setCreating(true)}
        onDelete={() => setConfirmOpen(true)}
        onPrint={handlePrint}
      />

      <CrudDataGrid
        gridRef={grid.gridRef}
        columnDefs={columnDefs}
        getRowId={grid.getRowId}
        onGridReady={grid.onGridReady}
        onSelectionChanged={grid.onSelectionChanged}
        getSelectAllState={grid.getSelectAllState}
        onSelectAllToggle={grid.toggleSelectAll}
        onRowDoubleClicked={onRowDoubleClicked}
        localeText={gridLocale}
      />

      {dialogOpen &&
        renderDialog({
          row: editing,
          onSaved: () => {
            closeDialog()
            setTimeout(grid.refresh, 0)
            onMutated?.()
          },
          onClose: closeDialog,
        })}

      <ConfirmDialog
        open={confirmOpen}
        title={messages.deleteTitle}
        message={messages.deleteConfirm(grid.selectedCount)}
        confirmText={t('delete')}
        destructive
        loading={deleting}
        onConfirm={confirmDelete}
        onClose={() => setConfirmOpen(false)}
      />
    </div>
  )
}
