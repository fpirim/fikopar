import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { Search } from 'lucide-react'
import { listJournalEntries } from '../api/endpoints'
import type { JournalEntry } from '../api/types'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney } from '../lib/format'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import { FkGrid } from '../components/grid/FkGrid'
import { exprFilter, numCol } from '../components/grid/columns'
import SelectCompany from '../components/layout/SelectCompany'
import { delays } from '../lib/delays'

type Row = JournalEntry & { total: number }

export default function JournalPage() {
  const orgId = useOrgId()
  return orgId ? <Journal orgId={orgId} /> : <SelectCompany />
}

function Journal({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['ledger', 'grid'])
  const [params, setParams] = useSearchParams()
  const { depotId } = useDepot()
  const q = params.get('q') ?? ''
  const [text, setText] = useState(q)
  const [extra, setExtra] = useState<{ key: string; items: JournalEntry[]; next: string | null }>()

  useEffect(() => {
    const timer = setTimeout(() => {
      if (text.trim() === q) return
      setParams(
        (p) => {
          const next = new URLSearchParams(p)
          if (text.trim()) next.set('q', text.trim())
          else next.delete('q')
          return next
        },
        { replace: true },
      )
    }, delays.journalSearch)
    return () => clearTimeout(timer)
  }, [text, q, setParams])

  const key = `${orgId}|${depotId}|${q}`
  const first = useApi(() => listJournalEntries(orgId, { depotId, q, limit: 50 }), [key]).data
  const more = extra?.key === key ? extra : undefined
  const rows = useMemo<Row[]>(
    () =>
      [...(first?.items ?? []), ...(more?.items ?? [])].map((e) => ({
        ...e,
        total: e.postings
          .filter((p) => p.side === 'DEBIT')
          .reduce((s, p) => s + Number(p.amount), 0),
      })),
    [first, more],
  )
  const next = more ? more.next : (first?.nextCursor ?? null)

  const columnDefs = useMemo<ColDef<Row>[]>(
    () => [
      {
        field: 'entryNo',
        headerName: t('journal.col.entry'),
        flex: 0,
        width: 150,
        cellClass: 'font-mono',
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.data ? <Link to={withDepot(`/journal/${p.data.id}`, depotId)}>{p.value}</Link> : null,
      },
      {
        field: 'entryDate',
        headerName: t('journal.col.date'),
        flex: 0,
        width: 118,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        field: 'description',
        headerName: t('journal.description'),
        flex: 2,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        field: 'sourceNo',
        headerName: t('journal.source'),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        field: 'sourceType',
        headerName: t('journal.col.type'),
        flex: 0,
        width: 150,
        cellRenderer: (p: ICellRendererParams<Row>) =>
          p.value ? (
            <Chip tone={p.value === 'manual' ? 'blue' : 'grey'}>
              {t(`journal.sourceType.${p.value}`)}
            </Chip>
          ) : null,
      },
      {
        field: 'depotId',
        headerName: t('journal.depot'),
        flex: 0,
        width: 80,
        cellClass: 'font-mono',
      },
      {
        field: 'total',
        headerName: t('journal.col.amount'),
        flex: 0,
        width: 140,
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
        valueFormatter: (p) => formatMoney(p.value),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, depotId],
  )

  return (
    <div className="flex flex-col gap-3">
      <div className="flex items-center gap-3">
        <h1 className="flex-1 text-2xl font-semibold">{t('journal.title')}</h1>
        <label className="flex h-9 w-[320px] items-center gap-2 rounded-md border border-fk-input bg-fk-surface px-3 text-sm focus-within:border-fk-accent focus-within:ring-1 focus-within:ring-fk-accent">
          <Search className="h-3.5 w-3.5 text-fk-muted" aria-hidden="true" />
          <input
            type="search"
            aria-label={t('journal.search')}
            placeholder={t('journal.search')}
            value={text}
            onChange={(e) => setText(e.target.value)}
            className="min-w-0 flex-1 bg-transparent font-mono text-13 outline-none placeholder:font-sans placeholder:text-fk-muted"
          />
        </label>
      </div>
      <Panel className="overflow-hidden">
        <FkGrid
          height={36 + 34 + 2 + Math.max(1, Math.min(rows.length, 16)) * 34}
          columnDefs={columnDefs}
          rowData={rows}
          getRowId={(p) => String(p.data.id)}
          overlayNoRowsTemplate={t('journal.empty')}
        />
      </Panel>
      {next && (
        <Button
          variant="outline"
          className="self-center"
          onClick={async () => {
            const page = await listJournalEntries(orgId, { depotId, q, limit: 50, cursor: next })
            setExtra({
              key,
              items: [...(more?.items ?? []), ...page.items],
              next: page.nextCursor ?? null,
            })
          }}
        >
          {t('journal.loadMore')}
        </Button>
      )}
    </div>
  )
}
