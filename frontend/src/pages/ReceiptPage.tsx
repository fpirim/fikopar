import { useCallback, useId, useMemo, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import type { AgGridReact } from 'ag-grid-react'
import type { ColDef, ICellRendererParams } from 'ag-grid-community'
import { ApiError } from '../api/client'
import {
  createReceipt,
  getSicilSummary,
  gridReceipts,
  listAccountsByRole,
  listNumberSeries,
  printReceipt,
} from '../api/endpoints'
import { errorText } from '../api/errorText'
import type { GridRequest, PaymentMethod, Receipt, ReceiptCreate } from '../api/types'
import { useToast } from '../context/ToastContext'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useIdempotencyKey } from '../hooks/useIdempotencyKey'
import { useInfiniteGrid } from '../hooks/useInfiniteGrid'
import { useOrgId } from '../hooks/useOrgId'
import { saveBlob } from '../lib/csv'
import { formatBalance, formatDay, formatMoney, todayIso } from '../lib/format'
import { formatNumber } from '../lib/numbering'
import { AccountPicker, type AccountHit } from '../components/AccountPicker'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import { DateField } from '../components/ui/date-field'
import { Field, selectCls } from '../components/ui/field'
import { KeyValue, Panel } from '../components/ui/panel'
import { MoneyInput } from '../components/ui/money-input'
import { Segmented } from '../components/ui/segmented'
import { TextField } from '../components/ui/text-field'
import { FkGrid } from '../components/grid/FkGrid'
import { exprFilter, numCol } from '../components/grid/columns'
import SelectCompany from '../components/layout/SelectCompany'
import { cn } from '@/lib/utils'

type Kind = ReceiptCreate['kind']
const METHODS: PaymentMethod[] = ['CASH', 'BANK_TRANSFER', 'CHEQUE', 'NOTE']
const ENABLED = new Set<PaymentMethod>(['CASH', 'BANK_TRANSFER'])

interface Form {
  kind: Kind
  account: AccountHit | null
  date: string
  depotId: number
  method: PaymentMethod
  bankAccountId: number | null
  amount: string
  description: string
}

export default function ReceiptPage() {
  const orgId = useOrgId()
  return orgId ? <Receipts orgId={orgId} /> : <SelectCompany />
}

function Receipts({ orgId }: Readonly<{ orgId: number }>) {
  const { t } = useTranslation(['treasury', 'errors', 'grid'])
  const toast = useToast()
  const { depotId: topDepot } = useDepot()
  const depots = useDepots()
  const idem = useIdempotencyKey()
  const blank = useCallback(
    (keep?: Partial<Form>): Form => ({
      kind: 'COLLECTION',
      account: null,
      date: todayIso(),
      depotId: topDepot ?? 1,
      method: 'CASH',
      bankAccountId: null,
      amount: '',
      description: '',
      ...keep,
    }),
    [topDepot],
  )
  const [form, setForm] = useState<Form>(() => blank())
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [methodError, setMethodError] = useState(false)
  const set = <K extends keyof Form>(k: K, v: Form[K]) => setForm((f) => ({ ...f, [k]: v }))

  const prefix = form.kind === 'COLLECTION' ? 'TAH' : 'TED'
  const year = Number(form.date.slice(0, 4))
  const series = useApi(
    () => listNumberSeries(orgId, { prefix, depotId: form.depotId, year }),
    [orgId, prefix, form.depotId, year],
  )
  const depotCode = depots.find((d) => d.id === form.depotId)?.code ?? String(form.depotId)
  const nextNo = series.data?.[0] ? formatNumber(series.data[0], depotCode) : ''

  const banks = useApi(() => listAccountsByRole(orgId, 'BANK'), [orgId]).data ?? []
  const bankAccountId = form.bankAccountId ?? banks[0]?.id ?? null
  const sicilId = form.account?.sicilId ?? null
  const summary = useApi(sicilId ? () => getSicilSummary(orgId, sicilId, form.depotId) : null, [
    orgId,
    sicilId,
    form.depotId,
  ])

  const errors = {
    account: !form.account ? t('receipt.errors.account') : null,
    amount: !(Number(form.amount) > 0) ? t('receipt.errors.amount') : null,
    bank: form.method === 'BANK_TRANSFER' && !bankAccountId ? t('receipt.errors.bank') : null,
  }
  const shown = (k: keyof typeof errors) => (touched ? errors[k] : null)

  const gridApi = useRef<AgGridReact>(null)
  const fetchPage = useCallback(
    (req: GridRequest) => gridReceipts(orgId, { ...req, depotId: topDepot }),
    [orgId, topDepot],
  )
  const recent = useInfiniteGrid<Receipt>(fetchPage, [orgId, topDepot])

  async function post() {
    setTouched(true)
    setError('')
    setMethodError(false)
    if (errors.account || errors.amount || errors.bank) return
    const body: ReceiptCreate = {
      kind: form.kind,
      depotId: form.depotId,
      sicilId: form.account!.sicilId,
      date: form.date,
      method: form.method,
      amount: form.amount,
      currency: 'TRY',
      ...(form.method === 'BANK_TRANSFER' ? { bankAccountId: bankAccountId! } : {}),
      ...(form.description.trim() ? { description: form.description.trim() } : {}),
    }
    setBusy(true)
    try {
      const receipt = await createReceipt(orgId, body, idem.keyFor(body))
      idem.reset()
      toast.success(t('receipt.posted', { no: receipt.receiptNo }))
      setForm(blank({ kind: form.kind, depotId: form.depotId, date: form.date }))
      setTouched(false)
      series.reload()
      recent.apiRef.current?.purgeInfiniteCache()
      saveBlob(await printReceipt(orgId, receipt.id), `${receipt.receiptNo}.pdf`)
    } catch (e) {
      setMethodError(e instanceof ApiError && e.field === 'method')
      setError(errorText(t, e, { date: form.date }))
    } finally {
      setBusy(false)
    }
  }

  const balance = summary.data ? Number(summary.data.balance) : null
  const delta = Number(form.amount || 0) * (form.kind === 'COLLECTION' ? -1 : 1)

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center gap-3">
        <h1 className="text-2xl font-semibold">{t('receipt.title')}</h1>
        <span className="min-w-[150px] font-mono text-sm text-fk-muted">{nextNo}</span>
        <div className="flex-1" />
        <Button
          variant="outline"
          onClick={() => {
            setForm(blank())
            setTouched(false)
            setError('')
          }}
        >
          {t('receipt.cancel')}
        </Button>
        <Button onClick={() => void post()} disabled={busy} className="min-w-[128px]">
          {busy ? t('receipt.posting') : t('receipt.post')}
        </Button>
      </div>
      <div className="flex gap-4">
        <Panel className="flex flex-1 flex-col gap-4 p-[18px]">
          <Segmented
            label={t('receipt.kind')}
            size="lg"
            className="self-start"
            value={form.kind}
            onChange={(v) => set('kind', v)}
            options={(['COLLECTION', 'PAYMENT'] as const).map((k) => ({
              value: k,
              label: t(`receipt.${k}`),
            }))}
          />
          <div className="grid grid-cols-4 gap-3">
            <div className="col-span-2">
              <AccountPicker
                orgId={orgId}
                label={t('receipt.account')}
                placeholder={t('receipt.accountPlaceholder')}
                value={form.account}
                onPick={(h) => set('account', h)}
              />
              {shown('account') && (
                <span className="text-xs text-fk-negative">{shown('account')}</span>
              )}
            </div>
            <DateField
              label={t('receipt.date')}
              value={form.date}
              onChange={(v) => set('date', v)}
              inputClassName="h-10"
            />
            <DepotSelect
              value={form.depotId}
              onChange={(v) => set('depotId', v)}
              options={depots.map((d) => ({ id: d.id!, label: t('cash.depotN', { id: d.code }) }))}
              label={t('receipt.depot')}
            />
          </div>
          <MethodPicker
            value={form.method}
            depotId={form.depotId}
            bankCode={banks.find((b) => b.id === bankAccountId)?.code}
            invalid={methodError}
            onChange={(m) => {
              setMethodError(false)
              set('method', m)
            }}
          />
          {form.method === 'BANK_TRANSFER' && (
            <div className="grid grid-cols-4 gap-3">
              <Field
                label={t('receipt.bank')}
                htmlFor="rc-bank"
                error={shown('bank')}
                className="col-span-2"
              >
                <select
                  id="rc-bank"
                  className={selectCls}
                  value={bankAccountId ?? ''}
                  onChange={(e) => set('bankAccountId', Number(e.target.value))}
                >
                  {banks.map((b) => (
                    <option key={b.id} value={b.id}>
                      {b.code} · {b.name}
                    </option>
                  ))}
                </select>
              </Field>
            </div>
          )}
          <div className="grid grid-cols-4 gap-3">
            <Field label={t('receipt.amount')} htmlFor="rc-amount" error={shown('amount')}>
              <MoneyInput
                id="rc-amount"
                value={form.amount}
                onChange={(v) => set('amount', v)}
                invalid={Boolean(shown('amount'))}
              />
            </Field>
            <TextField
              label={t('receipt.description')}
              className="col-span-3"
              value={form.description}
              onChange={(v) => set('description', v)}
            />
          </div>
          {error && (
            <p role="alert" className="text-13 text-fk-negative">
              {error}
            </p>
          )}
        </Panel>
        <aside
          aria-label={t('receipt.summary')}
          className="flex w-[300px] shrink-0 flex-col gap-2.5 rounded-lg border border-fk-border bg-fk-surface p-4"
        >
          {form.account ? (
            <>
              <h2 className="text-15 font-semibold">{form.account.hesapAdi}</h2>
              <KeyValue label={t('receipt.balance')}>
                {balance === null ? '—' : formatBalance(balance)}
              </KeyValue>
              <KeyValue label={t('receipt.after')}>
                {balance === null ? '—' : formatBalance(balance + delta)}
              </KeyValue>
              <KeyValue label={t('receipt.overdue')}>
                <span className={Number(summary.data?.overdue) > 0 ? 'text-fk-negative' : ''}>
                  {summary.data ? formatMoney(summary.data.overdue) : '—'}
                </span>
              </KeyValue>
              {form.account.accountId && (
                <Link
                  to={withDepot(`/statements/${form.account.accountId}`, topDepot)}
                  className="mt-1 text-sm text-fk-accent hover:text-fk-accent-hover"
                >
                  {t('receipt.statement')}
                </Link>
              )}
            </>
          ) : (
            <p className="text-sm text-fk-muted">{t('receipt.noAccount')}</p>
          )}
        </aside>
      </div>
      <RecentReceipts gridRef={gridApi} grid={recent} topDepot={topDepot} />
    </div>
  )
}

function DepotSelect({
  label,
  value,
  onChange,
  options,
}: Readonly<{
  label: string
  value: number
  onChange: (v: number) => void
  options: { id: number; label: string }[]
}>) {
  const id = useId()
  return (
    <Field label={label} htmlFor={id}>
      <select
        id={id}
        className={selectCls}
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
      >
        {options.map((o) => (
          <option key={o.id} value={o.id}>
            {o.label}
          </option>
        ))}
      </select>
    </Field>
  )
}

function MethodPicker({
  value,
  depotId,
  bankCode,
  invalid,
  onChange,
}: Readonly<{
  value: PaymentMethod
  depotId: number
  bankCode?: string
  invalid?: boolean
  onChange: (m: PaymentMethod) => void
}>) {
  const { t } = useTranslation('treasury')
  const hint: Record<string, string> = {
    CASH: t('receipt.cashHint', { id: depotId }),
    BANK_TRANSFER: bankCode ?? '—',
    CHEQUE: t('receipt.portfolioHint'),
    NOTE: t('receipt.portfolioHint'),
  }
  const enabled = METHODS.filter((m) => ENABLED.has(m))
  const move = (dir: number) => {
    const i = enabled.indexOf(value)
    onChange(enabled[(i + dir + enabled.length) % enabled.length])
  }
  return (
    <div>
      <div id="rc-method" className="mb-1.5 text-xs text-fk-muted">
        {t('receipt.method')}
      </div>
      <div
        role="radiogroup"
        aria-labelledby="rc-method"
        aria-invalid={invalid || undefined}
        className={cn(
          'grid grid-cols-4 gap-2.5 rounded-lg',
          invalid && 'ring-1 ring-fk-negative ring-offset-2',
        )}
        onKeyDown={(e) => {
          if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
            e.preventDefault()
            move(1)
          } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
            e.preventDefault()
            move(-1)
          }
        }}
      >
        {METHODS.map((m) => {
          const on = m === value
          return (
            <button
              key={m}
              type="button"
              role="radio"
              aria-checked={on}
              tabIndex={on ? 0 : -1}
              disabled={!ENABLED.has(m)}
              onClick={() => onChange(m)}
              className={cn(
                'flex flex-col gap-1 rounded-lg border bg-fk-surface text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-fk-accent disabled:cursor-not-allowed disabled:opacity-50',
                on
                  ? 'border-2 border-fk-accent px-[13px] py-[11px]'
                  : 'border-fk-input px-3.5 py-3',
              )}
            >
              <span className="text-sm font-medium">{t(`receipt.methods.${m}`)}</span>
              <span className="truncate text-xs text-fk-muted">{hint[m]}</span>
            </button>
          )
        })}
      </div>
    </div>
  )
}

function RecentReceipts({
  gridRef,
  grid,
  topDepot,
}: Readonly<{
  gridRef: React.RefObject<AgGridReact | null>
  grid: ReturnType<typeof useInfiniteGrid<Receipt>>
  topDepot: number | null
}>) {
  const { t, i18n } = useTranslation(['treasury', 'grid'])
  const columnDefs = useMemo<ColDef<Receipt>[]>(
    () => [
      {
        field: 'receiptNo',
        headerName: t('receipt.col.no'),
        sort: 'desc',
        sortIndex: 1,
        cellClass: 'font-mono',
        minWidth: 170,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        field: 'date',
        headerName: t('receipt.col.date'),
        sort: 'desc',
        sortIndex: 0,
        cellClass: 'font-mono',
        flex: 0,
        width: 120,
        ...exprFilter({ kind: 'date', placeholder: t('grid:fk.range_hint') }),
        valueFormatter: (p) => formatDay(p.value),
      },
      {
        field: 'kind',
        headerName: t('receipt.col.type'),
        flex: 0,
        width: 130,
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: (['COLLECTION', 'PAYMENT'] as const).map((k) => ({
            value: k,
            label: t(`receipt.${k}`),
          })),
        }),
        cellRenderer: (p: ICellRendererParams<Receipt>) =>
          p.value ? (
            <Chip tone={p.value === 'COLLECTION' ? 'green' : 'grey'}>
              {t(`receipt.chip.${p.value}`)}
            </Chip>
          ) : null,
      },
      {
        field: 'method',
        headerName: t('receipt.col.method'),
        ...exprFilter({
          kind: 'enum',
          anyLabel: t('grid:fk.any'),
          options: [...METHODS, 'ENDORSEMENT'].map((m) => ({
            value: m,
            label: t(`receipt.methods.${m}`),
          })),
        }),
        valueFormatter: (p) => (p.value ? t(`receipt.methods.${p.value}`) : ''),
      },
      {
        field: 'sicilName',
        headerName: t('receipt.col.account'),
        flex: 1.6,
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
      },
      {
        field: 'instrumentNo',
        headerName: t('receipt.col.source'),
        cellClass: 'font-mono',
        ...exprFilter({ kind: 'text', placeholder: t('grid:fk.contains') }),
        cellRenderer: (p: ICellRendererParams<Receipt>) =>
          p.value ? (
            <Link to={withDepot(`/journal?q=${encodeURIComponent(p.value)}`, topDepot)}>
              {p.value}
            </Link>
          ) : (
            <span className="text-fk-faint">—</span>
          ),
      },
      {
        field: 'amount',
        headerName: t('receipt.col.amount'),
        flex: 0,
        width: 130,
        ...numCol,
        ...exprFilter({ kind: 'number', placeholder: t('grid:fk.positive') }),
        valueFormatter: (p) => formatMoney(p.value),
      },
    ],
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [t, i18n.language, topDepot],
  )
  return (
    <Panel aria-labelledby="rc-recent" className="overflow-hidden">
      <div className="border-b border-fk-border px-3.5 py-3">
        <h2 id="rc-recent" className="text-15 font-semibold">
          {t('receipt.recent')}
        </h2>
      </div>
      <FkGrid
        ref={gridRef}
        height={36 + 34 + 2 + 6 * 34}
        columnDefs={columnDefs}
        {...grid.gridProps}
        cacheBlockSize={50}
        getRowId={(p) => String(p.data.id)}
      />
    </Panel>
  )
}
