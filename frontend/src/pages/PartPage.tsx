import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { Pencil } from 'lucide-react'
import { getPart, getPartPriceHistory, getPartStock } from '../api/endpoints'
import { errorText } from '../api/errorText'
import type { Depot, Part, Price, StockLevel, StockTransfer } from '../api/types'
import { useToast } from '../context/ToastContext'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useOrgId } from '../hooks/useOrgId'
import { formatDay, formatMoney, formatQty, todayIso } from '../lib/format'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { Panel } from '../components/ui/panel'
import SelectCompany from '../components/layout/SelectCompany'
import Movements from './part/Movements'
import PriceDialog from './part/PriceDialog'
import SettingsDialog from './part/SettingsDialog'
import TransferDialog from './part/TransferDialog'

const th =
  'h-9 whitespace-nowrap border-b border-r border-fk-border bg-fk-sunken px-2.5 text-left text-xs font-medium text-fk-secondary last:border-r-0'
const td = 'h-[34px] whitespace-nowrap border-b border-fk-divider px-2.5 text-grid'
const num = `${td} fk-num`

export default function PartPage() {
  const orgId = useOrgId()
  const { partId } = useParams()
  return orgId ? <PartView orgId={orgId} partId={Number(partId)} /> : <SelectCompany />
}

type Dialog = { kind: 'price' } | { kind: 'transfer' } | { kind: 'settings'; level: StockLevel }

function PartView({ orgId, partId }: Readonly<{ orgId: number; partId: number }>) {
  const { t } = useTranslation(['parts', 'errors'])
  const toast = useToast()
  const { depotId } = useDepot()
  const depots = [...useDepots()].sort((a, b) => a.code.localeCompare(b.code))
  const part = useApi(() => getPart(orgId, partId).then((r) => r.data), [orgId, partId])
  const stock = useApi(() => getPartStock(orgId, partId), [orgId, partId])
  const prices = useApi(() => getPartPriceHistory(orgId, partId), [orgId, partId])
  const [dialog, setDialog] = useState<Dialog | null>(null)
  const [moved, setMoved] = useState(0)
  const [warned, setWarned] = useState<StockTransfer | null>(null)

  const code = (id: number | null | undefined) =>
    depots.find((d) => d.id === id)?.code ?? String(id ?? '')

  if (part.error) {
    return <p className="text-sm text-fk-negative">{errorText(t, part.error)}</p>
  }
  const p = part.data
  if (!p) return null

  const refreshStock = () => {
    stock.reload()
    part.reload()
  }

  const posted = (tr: StockTransfer) => {
    setDialog(null)
    toast.success(t('transfer.posted', { no: tr.transferNo }))
    setWarned(tr.warnings.length ? tr : null)
    refreshStock()
    setMoved((n) => n + 1)
  }

  return (
    <div className="flex min-h-0 flex-1 flex-col gap-3.5">
      <Header part={p} depotId={depotId}>
        <Button variant="outline" onClick={() => setDialog({ kind: 'price' })}>
          {t('newPrice')}
        </Button>
        <Button variant="outline" onClick={() => setDialog({ kind: 'transfer' })}>
          {t('transfer.open')}
        </Button>
      </Header>
      {warned && (
        <div
          role="alert"
          className="flex items-start gap-3 rounded-lg border border-fk-chip-red-fg/30 bg-fk-chip-red px-3.5 py-2.5 text-13 text-fk-chip-red-fg"
        >
          <Chip size="sm" tone="red" className="mt-px">
            {t('chip.NEGATIVE')}
          </Chip>
          <div className="flex min-w-0 flex-1 flex-col gap-0.5">
            <span className="font-medium">
              {t('transfer.negativeTitle', { no: warned.transferNo })}
            </span>
            {warned.warnings.map((w) => (
              <span key={`${w.partId}-${w.depotId}`}>
                {t('transfer.negative', {
                  code: code(w.depotId),
                  stockNo: w.stockNo ?? p.stockNo,
                  qty: formatQty(w.onHandAfter),
                })}
              </span>
            ))}
          </div>
          <Button variant="outline" size="sm" onClick={() => setWarned(null)}>
            {t('transfer.dismiss')}
          </Button>
        </div>
      )}
      <div className="flex items-start gap-4">
        <Panel className="min-w-0 flex-1 overflow-hidden">
          <DepotTable
            levels={stock.data?.depots ?? []}
            total={stock.data?.total ?? null}
            depots={depots}
            onEdit={(level) => setDialog({ kind: 'settings', level })}
          />
        </Panel>
        <Panel className="w-[380px] shrink-0 overflow-hidden">
          <PriceTable prices={prices.data ?? []} next={p.nextPrice ?? null} />
        </Panel>
      </div>
      <Movements
        orgId={orgId}
        partId={p.id}
        stockNo={p.stockNo}
        depotId={depotId}
        depots={depots}
        version={moved}
      />
      {dialog?.kind === 'price' && (
        <PriceDialog
          orgId={orgId}
          part={p}
          onClose={() => setDialog(null)}
          onSaved={() => {
            setDialog(null)
            prices.reload()
            part.reload()
          }}
        />
      )}
      {dialog?.kind === 'settings' && (
        <SettingsDialog
          orgId={orgId}
          level={dialog.level}
          depotCode={code(dialog.level.depotId)}
          onClose={() => setDialog(null)}
          onSaved={() => {
            setDialog(null)
            stock.reload()
          }}
          onReload={() => {
            setDialog(null)
            stock.reload()
          }}
        />
      )}
      {dialog?.kind === 'transfer' && depots.length > 1 && (
        <TransferDialog
          orgId={orgId}
          part={p}
          depots={depots}
          levels={stock.data?.depots ?? []}
          fromDepotId={depotId ?? depots[0].id!}
          onClose={() => setDialog(null)}
          onPosted={posted}
        />
      )}
    </div>
  )
}

function Header({
  part,
  depotId,
  children,
}: Readonly<{ part: Part; depotId: number | null; children: React.ReactNode }>) {
  const { t } = useTranslation('parts')
  const codes = part.codes ?? []
  const equivalents = codes.filter((c) => c.kind === 'EQUIVALENT' && c.equivalentPartId)
  return (
    <div className="flex items-start gap-3">
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2.5">
          <h1 className="truncate text-2xl font-semibold">{part.name}</h1>
          <Chip tone="grey">{part.stockNo}</Chip>
          {!part.isActive && <Chip tone="grey">{t('chip.INACTIVE')}</Chip>}
        </div>
        <div className="mt-2 flex flex-wrap items-center gap-2 text-13 text-fk-muted">
          {codes
            .filter((c) => c.kind === 'OEM' || c.kind === 'ALT' || c.kind === 'MANUFACTURER')
            .map((c) => (
              <Chip key={`${c.kind}${c.code}`} tone="blue">
                {`${c.kind === 'MANUFACTURER' ? 'MFR' : c.kind} ${c.code}`}
              </Chip>
            ))}
          {equivalents.length > 0 && (
            <span className="flex items-center gap-1.5 whitespace-nowrap">
              {t('equivalents')}
              {equivalents.map((c) => (
                <Link
                  key={c.code}
                  to={withDepot(`/parts/${c.equivalentPartId}`, depotId)}
                  className="font-mono"
                >
                  {c.code}
                </Link>
              ))}
            </span>
          )}
          <span className="whitespace-nowrap">
            {[
              t('priceList', { catalog: part.catalogName ?? part.catalogCode }),
              part.brand ? t('brand', { brand: part.brand }) : null,
              t('pack', { qty: part.packageQty ?? 1 }),
            ]
              .filter(Boolean)
              .join(' · ')}
          </span>
        </div>
      </div>
      {children}
    </div>
  )
}

function DepotTable({
  levels,
  total,
  depots,
  onEdit,
}: Readonly<{
  levels: StockLevel[]
  total: StockLevel | null
  depots: Depot[]
  onEdit: (level: StockLevel) => void
}>) {
  const { t } = useTranslation('parts')
  const code = (id: number | null | undefined) =>
    depots.find((d) => d.id === id)?.code ?? String(id ?? '')
  const sorted = [...levels].sort((a, b) => code(a.depotId).localeCompare(code(b.depotId)))
  const qty = (v: string | null | undefined, neg = false) => (
    <td className={neg && Number(v) < 0 ? `${num} text-fk-negative` : num}>
      {v === null || v === undefined ? '—' : formatQty(v)}
    </td>
  )
  return (
    <table aria-label={t('stockTable')} className="w-full border-separate border-spacing-0">
      <thead>
        <tr>
          <th className={th}>{t('mcol.depotId')}</th>
          {(['onHand', 'reserved', 'available', 'minQty', 'maxQty'] as const).map((k) => (
            <th key={k} className={`${th} text-right`}>
              {t(`col.${k}`)}
            </th>
          ))}
          <th className={th}>{t('col.shelf')}</th>
          <th className={`${th} w-10`} aria-hidden="true" />
        </tr>
      </thead>
      <tbody>
        {sorted.map((l) => (
          <tr key={l.depotId} className="hover:bg-fk-hover">
            <td className={td}>{t('depotN', { code: code(l.depotId) })}</td>
            {qty(l.onHand, true)}
            {qty(l.reserved)}
            {qty(l.available, true)}
            {qty(l.minQty)}
            {qty(l.maxQty)}
            <td className={`${td} font-mono`}>{l.shelf || '—'}</td>
            <td className={`${td} py-0 text-right`}>
              <button
                type="button"
                aria-label={t('editDepot', { code: code(l.depotId) })}
                onClick={() => onEdit(l)}
                className="inline-flex h-7 w-7 items-center justify-center rounded text-fk-muted hover:bg-fk-sunken hover:text-fk-accent focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent"
              >
                <Pencil className="h-3.5 w-3.5" aria-hidden="true" />
              </button>
            </td>
          </tr>
        ))}
        {total && (
          <tr className="bg-fk-sunken font-semibold">
            <td className={td}>{t('total')}</td>
            {qty(total.onHand, true)}
            {qty(total.reserved)}
            {qty(total.available, true)}
            <td className={td} />
            <td className={td} />
            <td className={td} />
            <td className={td} />
          </tr>
        )}
      </tbody>
    </table>
  )
}

function PriceTable({
  prices,
  next,
}: Readonly<{ prices: Price[]; next: Part['nextPrice'] | null }>) {
  const { t } = useTranslation('parts')
  // Rows after the current one start in the future.
  const current = prices.find((p) => p.isCurrent)?.validFrom ?? todayIso()
  return (
    <>
      <table aria-label={t('prices')} className="w-full border-separate border-spacing-0">
        <thead>
          <tr>
            <th className={th}>{t('validFrom')}</th>
            <th className={`${th} text-right`}>{t('listPrice')}</th>
            <th className={`${th} text-right`}>{t('change')}</th>
          </tr>
        </thead>
        <tbody>
          {prices.map((p) => {
            const future = (p.validFrom ?? '') > current
            return (
              <tr key={p.id ?? p.validFrom}>
                <td className={`${td} font-mono`}>
                  <span className="inline-flex items-center gap-2">
                    <span>{formatDay(p.validFrom)}</span>
                    {p.isCurrent && (
                      <Chip size="sm" tone="green">
                        {t('chip.CURRENT')}
                      </Chip>
                    )}
                    {future && (
                      <Chip size="sm" tone="blue">
                        {t('chip.FUTURE')}
                      </Chip>
                    )}
                  </span>
                </td>
                <td className={num}>{formatMoney(p.price)}</td>
                <td className={num}>
                  {p.changePct === null || p.changePct === undefined
                    ? '—'
                    : `${formatQty(p.changePct, { sign: true })}%`}
                </td>
              </tr>
            )
          })}
          {prices.length === 0 && (
            <tr>
              <td colSpan={3} className={`${td} text-fk-muted`}>
                {t('noPrice')}
              </td>
            </tr>
          )}
        </tbody>
      </table>
      {next && (
        <p className="px-2.5 py-2 text-13 text-fk-muted">
          {t('nextPrice', { price: formatMoney(next.price), date: formatDay(next.validFrom) })}
        </p>
      )}
    </>
  )
}
