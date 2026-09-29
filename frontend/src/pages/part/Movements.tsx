import { useCallback, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { exportStockMovements, gridStockMovements } from '../../api/endpoints'
import type { Depot, GridRequest, StockMovement } from '../../api/types'
import { useDownload } from '../../hooks/useDownload'
import { useInfiniteGrid } from '../../hooks/useInfiniteGrid'
import { formatDay, formatQty } from '../../lib/format'
import { Button } from '../../components/ui/button'
import { Chip, type ChipTone } from '../../components/ui/chip'
import { Panel } from '../../components/ui/panel'
import { FkGrid } from '../../components/grid/FkGrid'
import { exprFilter, fitMono, monoWidth, numCol } from '../../components/grid/columns'

type Label = NonNullable<StockMovement['label']>
const LABELS: Label[] = [
  'SALE',
  'PURCHASE',
  'SALES_RETURN',
  'PURCHASE_RETURN',
  'TRANSFER',
  'COUNT',
  'OPENING',
]
const TONE: Record<Label, ChipTone> = {
  SALE: 'amber',
  PURCHASE: 'green',
  SALES_RETURN: 'blue',
  PURCHASE_RETURN: 'blue',
  TRANSFER: 'grey',
  COUNT: 'grey',
  OPENING: 'grey',
}
const MAX_BODY = 8
const QTY_W = monoWidth(6)

// Movements of one part with the running on-hand (ledger order), opening and closing on hand.
export default function Movements({
  orgId,
  partId,
  stockNo,
  depotId,
  depots,
  version,
}: Readonly<{
  orgId: number
  partId: number
  stockNo: string
  depotId: number | null
  depots: Depot[]
  version: number
}>) {
  const { t, i18n } = useTranslation(['parts', 'grid'])
  const download = useDownload()
  const [bracket, setBracket] = useState<{ opening: string | null; closing: string | null }>({
    opening: null,
    closing: null,
  })
  const code = useCallback(
    (id: number | null | undefined) => depots.find((d) => d.id === id)?.code ?? String(id ?? ''),
    [depots],
  )

  const fetchPage = useCallback(
    async (req: GridRequest) => {
      const r = await gridStockMovements(orgId, { ...req, partId, depotId })
      setBracket({ opening: r.openingOnHand ?? null, closing: r.closingOnHand ?? null })
      return r
    },
    [orgId, partId, depotId],
  )
  const grid = useInfiniteGrid<StockMovement>(fetchPage, [orgId, partId, depotId, version])

  const qty = (colId: 'in' | 'out'): ColDef<StockMovement> => ({
    colId,
    headerName: t(`mcol.${colId}`),
    flex: 0,
    width: QTY_W,
    sortable: false,
    ...numCol,
    valueGetter: (p) => {
      const d = Number(p.data?.delta ?? 0)
      return colId === 'in' ? (d > 0 ? d : null) : d < 0 ? -d : null
    },
    valueFormatter: (p) => (p.value === null ? '—' : formatQty(p.value)),
    cellClassRules: { 'fk-muted': (p) => p.value === null },
  })

  const columnDefs = useMemo<ColDef<StockMovement>[]>(
    () => [
      {
        colId: 'date',
        field: 'date',
        headerName: t('mcol.date'),
        sort: 'desc',
        flex: 0,
        width: monoWidth(10),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        colId: 'label',
        field: 'label',
        headerName: t('mcol.label'),
        flex: 0,
        width: 132,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: LABELS.map((v) => ({ value: v, label: t(`label.${v}`) })),
        }),
        cellRenderer: (p: ICellRendererParams<StockMovement>) =>
          p.value ? (
            <Chip size="sm" tone={TONE[p.value as Label]}>
              {t(`label.${p.value}`)}
            </Chip>
          ) : null,
      },
      {
        colId: 'sourceNo',
        field: 'sourceNo',
        headerName: t('mcol.sourceNo'),
        flex: 0,
        width: fitMono([], 18),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<StockMovement>) =>
          p.data?.countId ? (
            <Link to={`/stock-counts/${p.data.countId}`}>{p.value}</Link>
          ) : (
            (p.value ?? '—')
          ),
      },
      {
        colId: 'sicilName',
        field: 'sicilName',
        headerName: t('mcol.sicilName'),
        flex: 1,
        minWidth: 160,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        valueFormatter: (p) => p.value ?? '—',
        cellClassRules: { 'fk-muted': (p) => !p.value } as ColDef<StockMovement>['cellClassRules'],
      },
      {
        colId: 'depotId',
        field: 'depotId',
        headerName: t('mcol.depotId'),
        flex: 0,
        width: 96,
        cellClass: 'font-mono',
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.all'),
          options: depots.map((d) => ({ value: String(d.id), label: d.code })),
        }),
        valueFormatter: (p) => {
          const m = p.data
          if (!m) return ''
          if (m.kind === 'TRANSFER_OUT') return `${code(m.depotId)} → ${code(m.otherDepotId)}`
          if (m.kind === 'TRANSFER_IN') return `${code(m.otherDepotId)} → ${code(m.depotId)}`
          return code(m.depotId)
        },
      },
      qty('in'),
      qty('out'),
      {
        colId: 'runningOnHand',
        field: 'runningOnHand',
        headerName: t('mcol.runningOnHand'),
        flex: 0,
        width: monoWidth(8),
        sortable: false,
        ...numCol,
        valueFormatter: (p) =>
          p.value === null || p.value === undefined ? '—' : formatQty(p.value),
        cellClassRules: {
          'fk-neg': (p) => Number(p.value) < 0,
        } as ColDef<StockMovement>['cellClassRules'],
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, depots, code],
  )

  const total = grid.info?.totalCount ?? 0
  const height = 36 + 34 + 2 + Math.max(1, Math.min(total, MAX_BODY)) * 34

  return (
    <Panel aria-labelledby="movements-h" className="flex min-h-0 shrink flex-col overflow-hidden">
      <div className="flex items-center gap-4 border-b border-fk-border px-3.5 py-3">
        <h2 id="movements-h" className="flex-1 text-15 font-semibold">
          {t('movements')}
        </h2>
        {(['opening', 'closing'] as const).map((k) => (
          <div
            key={k}
            role="group"
            aria-label={t(k)}
            className="flex items-baseline gap-1.5 whitespace-nowrap text-13 text-fk-muted"
          >
            <span>{t(k)}</span>
            <span
              className={
                Number(bracket[k]) < 0
                  ? 'min-w-[3ch] text-right font-mono text-fk-negative'
                  : 'min-w-[3ch] text-right font-mono text-fk-text'
              }
            >
              {bracket[k] === null ? '—' : formatQty(bracket[k])}
            </span>
          </div>
        ))}
        <Button
          variant="outline"
          size="sm"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('xlsx', `movements-${stockNo}.xlsx`, () =>
              exportStockMovements(orgId, { ...grid.request(), partId, depotId }),
            )
          }
        >
          {t('grid:fk.excel')}
        </Button>
      </div>
      <FkGrid
        height={height}
        className="min-h-0 shrink"
        columnDefs={columnDefs}
        {...grid.gridProps}
        getRowId={(p) => String(p.data.id)}
      />
    </Panel>
  )
}
