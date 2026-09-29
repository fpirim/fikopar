import { useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { CellKeyDownEvent, ColDef, ICellRendererParams } from 'ag-grid-community'
import { exportCarryForward, getCarryForward, postCarryForward } from '../api/endpoints'
import { errorText } from '../api/errorText'
import type { CarryForward as Preview, CarryForwardRow } from '../api/types'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { useApi } from '../hooks/useApi'
import { useDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useDownload } from '../hooks/useDownload'
import { useIdempotencyKey } from '../hooks/useIdempotencyKey'
import { useOrgId } from '../hooks/useOrgId'
import { formatBalance, formatDateTime, formatMoney, todayIso } from '../lib/format'
import { Button } from '../components/ui/button'
import { Chip, type ChipTone } from '../components/ui/chip'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../components/ui/dialog'
import { Panel } from '../components/ui/panel'
import { Stat } from '../components/ui/stat'
import { FkGrid } from '../components/grid/FkGrid'
import { exprFilter, numCol } from '../components/grid/columns'
import SelectCompany from '../components/layout/SelectCompany'
import ConflictDialog from '../components/ConflictDialog'
import OverrideDialog from './carryforward/OverrideDialog'
import ReopenDialog from './carryforward/ReopenDialog'
import Steps from './carryforward/Steps'
import { dash } from './statement/docType'

const STATUS = ['READY', 'SKIP', 'EDITED', 'POSTED'] as const
const TONE: Record<string, ChipTone> = {
  READY: 'grey',
  SKIP: 'grey',
  EDITED: 'amber',
  POSTED: 'green',
}

export default function CarryForwardPage() {
  const orgId = useOrgId()
  const { year } = useParams()
  if (!orgId) return <SelectCompany />
  return <CarryForward orgId={orgId} year={Number(year ?? todayIso().slice(0, 4))} />
}

function CarryForward({ orgId, year }: Readonly<{ orgId: number; year: number }>) {
  const { t, i18n } = useTranslation(['ledger', 'grid', 'errors'])
  const toast = useToast()
  const { depotId } = useDepot()
  const download = useDownload()
  const idem = useIdempotencyKey()
  const admin = useAuth().user?.role === 'admin'
  const depots = useDepots()
  const cf = useApi(() => getCarryForward(orgId, year, depotId), [orgId, year, depotId])
  const [editing, setEditing] = useState<CarryForwardRow | null>(null)
  const [confirming, setConfirming] = useState(false)
  const [reopening, setReopening] = useState(false)
  const [conflict, setConflict] = useState(false)
  const [busy, setBusy] = useState(false)
  const data = cf.data
  const posted = data?.status === 'POSTED'
  const onConflict = () => {
    setEditing(null)
    setReopening(false)
    setConflict(true)
  }
  const blocked = data?.checks.some((c) => c.blocking && !c.ok && c.code !== 'YEAR_NOT_OPENED')

  const columnDefs = useMemo<ColDef<CarryForwardRow>[]>(
    () => [
      {
        field: 'accountCode',
        headerName: t('carryForward.col.account'),
        sort: 'asc',
        flex: 0,
        width: 130,
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: '120.', defaultOp: 'startsWith' }),
      },
      {
        field: 'accountName',
        headerName: t('carryForward.col.name'),
        flex: 2,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        colId: 'depotId',
        headerName: t('carryForward.col.depot'),
        flex: 0,
        width: 90,
        cellClass: 'font-mono',
        valueGetter: (p) => String(p.data?.depotId ?? ''),
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.all'),
          options: depots.map((d) => ({ value: String(d.id), label: d.code })),
        }),
      },
      {
        field: 'closingBalance',
        headerName: t('carryForward.col.closing', { date: `31.12.${year - 1}` }),
        flex: 0,
        width: 170,
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: t('grid:fk.nonZero') }),
        valueGetter: (p) => Number(p.data?.closingBalance ?? 0),
        valueFormatter: (p) => formatBalance(p.value) || formatMoney(0),
      },
      ...(['openingDebit', 'openingCredit'] as const).map<ColDef<CarryForwardRow>>((f) => ({
        field: f,
        headerName: t(`carryForward.col.${f === 'openingDebit' ? 'debit' : 'credit'}`),
        flex: 0,
        width: 140,
        ...numCol,
        valueGetter: (p) => Number(p.data?.[f] ?? 0),
        valueFormatter: (p) => dash(p.value),
        cellClassRules: { 'fk-muted': (p) => !p.value },
      })),
      {
        field: 'status',
        headerName: t('carryForward.col.status'),
        flex: 0,
        width: 130,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: STATUS.map((s) => ({ value: s, label: t(`carryForward.status.${s}`) })),
        }),
        cellRenderer: (p: ICellRendererParams<CarryForwardRow>) =>
          p.value ? (
            <Chip tone={TONE[p.value]} className="align-middle">
              {t(`carryForward.status.${p.value}`)}
            </Chip>
          ) : null,
        tooltipValueGetter: (p) => p.data?.overrideReason ?? undefined,
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, year, depots],
  )

  async function post() {
    setBusy(true)
    try {
      await postCarryForward(orgId, year, idem.keyFor({ year }))
      cf.reload()
      toast.success(t('carryForward.confirm.done', { year }))
      setConfirming(false)
    } catch (e) {
      toast.error(errorText(t, e))
    } finally {
      setBusy(false)
    }
  }

  const edit = (row: CarryForwardRow | undefined) => {
    if (row && !posted) setEditing(row)
  }

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-4">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-semibold">
          {t('carryForward.title', { from: year - 1, to: year })}
        </h1>
        {posted && <Chip tone="green">{t('carryForward.posted')}</Chip>}
        {data?.status === 'REOPENED' && <Reopened cf={data} />}
        <div className="flex-1" />
        {posted && admin && (
          <Button variant="outline" onClick={() => setReopening(true)}>
            {t('carryForward.reopen')}
          </Button>
        )}
        <Button
          variant="outline"
          disabled={download.busy !== null}
          onClick={() =>
            download.run('pdf', `carry-forward-${year}.pdf`, () =>
              exportCarryForward(orgId, year, depotId),
            )
          }
        >
          {t('carryForward.previewPdf')}
        </Button>
        {data && !posted && (
          <Button disabled={blocked || !data.entryCount} onClick={() => setConfirming(true)}>
            {t('carryForward.post', { count: data.entryCount })}
          </Button>
        )}
      </div>
      {data && <Steps cf={data} />}
      <div className="grid grid-cols-4 gap-4">
        <Stat label={t('carryForward.kpi.accounts')} value={data?.accountCount ?? '—'} />
        <Stat label={t('carryForward.kpi.entries')} value={data?.entryCount ?? '—'} />
        <Stat
          label={t('carryForward.kpi.debit')}
          value={data ? formatMoney(data.totalDebit) : '—'}
        />
        <Stat
          label={t('carryForward.kpi.credit')}
          value={data ? formatMoney(data.totalCredit) : '—'}
        />
      </div>
      {!posted && <p className="-mb-2 text-13 text-fk-muted">{t('carryForward.hint')}</p>}
      <Panel className="flex min-h-[240px] flex-1 flex-col overflow-hidden">
        <FkGrid
          className="min-h-0 flex-1"
          columnDefs={columnDefs}
          rowData={data?.rows ?? []}
          getRowId={(p) => `${p.data.accountId}:${p.data.depotId}`}
          onRowDoubleClicked={(e) => edit(e.data)}
          onCellKeyDown={(e: CellKeyDownEvent<CarryForwardRow>) => {
            if ((e.event as KeyboardEvent | undefined)?.key === 'Enter') edit(e.data)
          }}
        />
      </Panel>
      {editing && data && (
        <OverrideDialog
          orgId={orgId}
          year={year}
          row={editing}
          preview={{ version: data.version, depotId }}
          onClose={() => setEditing(null)}
          onSaved={(next) => {
            setEditing(null)
            cf.setData(next)
          }}
          onConflict={onConflict}
        />
      )}
      {reopening && data && (
        <ReopenDialog
          orgId={orgId}
          cf={data}
          onClose={() => setReopening(false)}
          onReopened={() => {
            setReopening(false)
            toast.success(t('carryForward.reopenDialog.done', { year }))
            cf.reload()
          }}
          onConflict={onConflict}
        />
      )}
      {conflict && (
        <ConflictDialog
          title={t('carryForward.conflictTitle')}
          message={t('carryForward.conflict')}
          onReload={() => {
            setConflict(false)
            cf.reload()
          }}
        />
      )}
      {confirming && data && (
        <Dialog open onOpenChange={(o) => !o && setConfirming(false)}>
          <DialogContent role="alertdialog" className="max-w-md">
            <DialogHeader>
              <DialogTitle>{t('carryForward.confirm.title')}</DialogTitle>
              <DialogDescription>
                {t('carryForward.confirm.body', { count: data.entryCount, year })}
              </DialogDescription>
            </DialogHeader>
            <DialogFooter>
              <Button variant="outline" onClick={() => setConfirming(false)}>
                {t('common:cancel')}
              </Button>
              <Button onClick={() => void post()} disabled={busy} className="min-w-[80px]">
                {t('carryForward.confirm.post')}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      )}
    </div>
  )
}

function Reopened({ cf }: Readonly<{ cf: Preview }>) {
  const { t } = useTranslation('ledger')
  return (
    <>
      <Chip tone="amber">{t('carryForward.reopened')}</Chip>
      {cf.reopenReason && (
        <span className="min-w-0 truncate text-13 text-fk-muted">
          {t('carryForward.reopenedNote', {
            at: cf.reopenedAt ? formatDateTime(cf.reopenedAt) : '—',
            reason: cf.reopenReason,
          })}
        </span>
      )}
    </>
  )
}
