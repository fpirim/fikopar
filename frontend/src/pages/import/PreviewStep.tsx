import { useCallback, useMemo, useState } from 'react'
import { useTranslation } from 'react-i18next'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { discardPriceImport, exportPriceImportRows, gridPriceImportRows } from '../../api/endpoints'
import { errorText } from '../../api/errorText'
import type {
  GridRequest,
  ImportChange,
  PriceImport,
  PriceImportIssue,
  PriceImportRow,
} from '../../api/types'
import { useDownload } from '../../hooks/useDownload'
import { useInfiniteGrid } from '../../hooks/useInfiniteGrid'
import { formatDay, formatMoney } from '../../lib/format'
import { cn } from '@/lib/utils'
import { Button } from '../../components/ui/button'
import { Chip, type ChipTone } from '../../components/ui/chip'
import { Panel } from '../../components/ui/panel'
import { FkGrid } from '../../components/grid/FkGrid'
import { exprFilter, monoWidth, numCol } from '../../components/grid/columns'
import GridFooter from '../../components/grid/GridFooter'

type Counts = PriceImport['counts']
const TABS: [ImportChange, (c: Counts) => number][] = [
  ['ALL', (c) => c.rows + c.missing],
  ['NEW', (c) => c.new],
  ['PRICE', (c) => c.price],
  ['ATTRIBUTES', (c) => c.attributes],
  ['UNCHANGED', (c) => c.unchanged],
  ['MISSING', (c) => c.missing],
  ['ERROR', (c) => c.errors],
  ['WARNING', (c) => c.warnings],
]
const CHANGE_TONE: Record<PriceImportRow['changes'][number], ChipTone> = {
  NEW: 'green',
  PRICE: 'blue',
  ATTRIBUTES: 'grey',
  MISSING: 'amber',
  DELETE: 'red',
}
const MAX_BODY = 12

// File vs catalog, one tab per change kind; the grid pages through gridPriceImportRows.
export default function PreviewStep({
  orgId,
  job,
  onEditMapping,
  onContinue,
  onDiscarded,
}: Readonly<{
  orgId: number
  job: PriceImport
  onEditMapping: () => void
  onContinue: () => void
  onDiscarded: (job: PriceImport) => void
}>) {
  const { t, i18n } = useTranslation(['catalog', 'grid', 'errors'])
  const [change, setChange] = useState<ImportChange>('ALL')
  const [error, setError] = useState('')
  const download = useDownload()

  const fetchPage = useCallback(
    (req: GridRequest) => gridPriceImportRows(orgId, job.id, { ...req, change }),
    [orgId, job.id, change],
  )
  const grid = useInfiniteGrid<PriceImportRow>(fetchPage, [orgId, job.id, job.version, change])

  const issueText = useCallback(
    (list: PriceImportIssue[]) =>
      list
        .map((i) => {
          const field = i.field ? ` · ${t(`import.target.${i.field}`)}` : ''
          const value = i.value ? ` (${i.value})` : ''
          return `${t(`import.issue.${i.code}`)}${field}${value}`
        })
        .join('; '),
    [t],
  )

  const columnDefs = useMemo<ColDef<PriceImportRow>[]>(() => {
    const money = (colId: 'price' | 'currentPrice', get: (r: PriceImportRow) => unknown) => ({
      colId,
      headerName: t(`import.col.${colId}`),
      flex: 0,
      width: monoWidth(12),
      ...numCol,
      valueGetter: (p: { data?: PriceImportRow }) => (p.data ? get(p.data) : null),
      valueFormatter: (p: { value: unknown }) => (p.value ? formatMoney(p.value as string) : '—'),
    })
    return [
      {
        colId: 'rowNo',
        field: 'rowNo',
        headerName: t('import.col.rowNo'),
        flex: 0,
        width: monoWidth(6),
        ...numCol,
        valueFormatter: (p) => (p.value === null || p.value === undefined ? '—' : String(p.value)),
      },
      {
        colId: 'stockNo',
        field: 'stockNo',
        headerName: t('import.col.stockNo'),
        flex: 0,
        width: monoWidth(15),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'description',
        headerName: t('import.col.description'),
        flex: 2,
        minWidth: 180,
        valueGetter: (p) => p.data?.file?.description ?? p.data?.current?.description ?? '',
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'brand',
        headerName: t('import.col.brand'),
        flex: 0,
        width: 112,
        valueGetter: (p) => p.data?.file?.brand ?? p.data?.current?.brand ?? '',
      },
      money('price', (r) => r.file?.price),
      money('currentPrice', (r) => r.current?.price),
      {
        colId: 'priceChangePct',
        field: 'priceChangePct',
        headerName: t('import.col.priceChangePct'),
        flex: 0,
        width: monoWidth(7),
        ...numCol,
        cellRenderer: (p: ICellRendererParams<PriceImportRow>) => {
          if (!p.value) return <span className="text-fk-faint">—</span>
          const n = Number(p.value)
          return (
            <span className={n > 0 ? 'text-fk-chip-amber-fg' : 'text-fk-chip-green-fg'}>
              {formatMoney(n, { sign: true, digits: 1 })}%
            </span>
          )
        },
      },
      {
        colId: 'validFrom',
        headerName: t('import.col.validFrom'),
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        valueGetter: (p) => p.data?.file?.validFrom ?? null,
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        colId: 'changes',
        headerName: t('import.col.changes'),
        flex: 0,
        width: 158,
        sortable: false,
        cellRenderer: (p: ICellRendererParams<PriceImportRow>) => (
          <span className="flex gap-1">
            {p.data?.changes.map((c) => (
              <Chip key={c} size="sm" tone={CHANGE_TONE[c]}>
                {t(`import.change.${c}`)}
              </Chip>
            ))}
          </span>
        ),
      },
      {
        colId: 'errors',
        headerName: t('import.col.issues'),
        flex: 1.4,
        minWidth: 180,
        sortable: false,
        tooltipValueGetter: (p) =>
          p.data ? issueText([...p.data.errors, ...p.data.warnings]) : '',
        cellRenderer: (p: ICellRendererParams<PriceImportRow>) =>
          p.data ? (
            <span className="truncate">
              <span className="text-fk-negative">{issueText(p.data.errors)}</span>
              {p.data.errors.length > 0 && p.data.warnings.length > 0 && '; '}
              <span className="text-fk-chip-amber-fg">{issueText(p.data.warnings)}</span>
            </span>
          ) : null,
      },
    ]
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [t, i18n.language, issueText])

  const labels = Object.fromEntries(columnDefs.map((c) => [c.colId!, c.headerName!]))
  const total = grid.info?.totalCount ?? 0
  const height = 36 + 34 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34

  async function discard() {
    setError('')
    try {
      onDiscarded(await discardPriceImport(orgId, job))
    } catch (e) {
      setError(errorText(t, e))
    }
  }

  return (
    <div className="flex min-h-0 flex-col gap-3">
      {job.duplicateOfImportId && (
        <p className="rounded-md bg-fk-chip-amber px-3 py-2 text-13 text-fk-chip-amber-fg">
          {t('import.duplicate', { id: job.duplicateOfImportId })}
        </p>
      )}
      <div className="flex items-center gap-2">
        <div role="tablist" aria-label={t('import.tabsLabel')} className="flex gap-1">
          {TABS.map(([key, count]) => (
            <button
              key={key}
              type="button"
              role="tab"
              aria-selected={change === key}
              onClick={() => setChange(key)}
              className={cn(
                'flex h-[30px] items-center gap-1.5 whitespace-nowrap rounded-md border px-2.5 text-13 font-medium',
                change === key
                  ? 'border-fk-text bg-fk-text text-white'
                  : 'border-fk-input bg-fk-surface text-fk-muted hover:text-fk-text',
                key === 'ERROR' && count(job.counts) > 0 && change !== key && 'text-fk-negative',
              )}
            >
              <span>{t(`import.tabs.${key}`)}</span>
              <span className="font-mono text-xs">{count(job.counts)}</span>
            </button>
          ))}
        </div>
        <div className="flex-1" />
        <span className="whitespace-nowrap text-13 text-fk-muted">
          {t('import.rows', { count: job.counts.rows })}
        </span>
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', `import-${job.id}.xlsx`, () =>
              exportPriceImportRows(orgId, job.id, { ...grid.request(), change }),
            )
          }
        >
          {t('grid:fk.excel')}
        </Button>
      </div>
      <Panel className="flex min-h-0 shrink flex-col overflow-hidden">
        <FkGrid
          height={height}
          className="min-h-0 shrink"
          columnDefs={columnDefs}
          {...grid.gridProps}
          getRowId={(p) => String(p.data.id)}
          tooltipShowDelay={300}
        />
        <GridFooter
          sorted={grid.sorted.map((s) => labels[s.colId] ?? s.colId)}
          filtered={grid.filtered.map((c) => labels[c] ?? c)}
          range={grid.range}
          total={total}
          onClear={grid.clearFilters}
        />
      </Panel>
      {error && (
        <p role="alert" className="text-sm text-fk-negative">
          {error}
        </p>
      )}
      <div className="flex items-center gap-2">
        <Button variant="outline" onClick={onEditMapping}>
          {t('import.editMapping')}
        </Button>
        <Button variant="outline" onClick={discard}>
          {t('import.discard')}
        </Button>
        <div className="flex-1" />
        <Button onClick={onContinue}>{t('import.continue')}</Button>
      </div>
    </div>
  )
}
