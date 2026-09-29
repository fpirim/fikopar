import { useId, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../../api/client'
import { createCashVoucher, listAccountsByPrefix, listNumberSeries } from '../../api/endpoints'
import type { CashVoucherCreate } from '../../api/types'
import { useToast } from '../../context/ToastContext'
import { useApi } from '../../hooks/useApi'
import { useIdempotencyKey } from '../../hooks/useIdempotencyKey'
import { todayIso } from '../../lib/format'
import { formatNumber } from '../../lib/numbering'
import { Button } from '../../components/ui/button'
import { DateField } from '../../components/ui/date-field'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../../components/ui/dialog'
import { Field, selectCls } from '../../components/ui/field'
import { MoneyInput } from '../../components/ui/money-input'
import { Segmented } from '../../components/ui/segmented'
import { TextField } from '../../components/ui/text-field'

type Kind = CashVoucherCreate['kind']

// Expense (GDR) or cash to / from a bank (VRM).
export default function VoucherDialog({
  orgId,
  depotId,
  depotCode,
  mode,
  onClose,
  onPosted,
}: Readonly<{
  orgId: number
  depotId: number
  depotCode: string
  mode: 'expense' | 'bank'
  onClose: () => void
  onPosted: () => void
}>) {
  const { t } = useTranslation(['treasury', 'common', 'errors'])
  const toast = useToast()
  const idem = useIdempotencyKey()
  const ids = { account: useId(), amount: useId() }
  const [kind, setKind] = useState<Kind>(mode === 'expense' ? 'EXPENSE' : 'BANK_DEPOSIT')
  const [accountId, setAccountId] = useState<number | null>(null)
  const [date, setDate] = useState(todayIso())
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [touched, setTouched] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const prefix = mode === 'expense' ? 'GDR' : 'VRM'
  const accounts =
    useApi(
      () =>
        mode === 'expense'
          ? listAccountsByPrefix(orgId, 'EXPENSE', '770.')
          : listAccountsByPrefix(orgId, 'ASSET', '102.'),
      [orgId, mode],
    ).data ?? []
  const series = useApi(
    () => listNumberSeries(orgId, { prefix, depotId, year: Number(date.slice(0, 4)) }),
    [orgId, prefix, depotId, date.slice(0, 4)],
  ).data
  const account = accountId ?? accounts[0]?.id ?? null
  const bad = {
    amount: !(Number(amount) > 0),
    description: !description.trim(),
  }

  async function post(e: React.FormEvent) {
    e.preventDefault()
    setTouched(true)
    if (bad.amount || bad.description || !account) return
    const body: CashVoucherCreate = {
      kind,
      depotId,
      date,
      accountId: account,
      amount,
      description: description.trim(),
    }
    setBusy(true)
    setError('')
    try {
      const v = await createCashVoucher(orgId, body, idem.keyFor(body))
      toast.success(t('cash.voucher.posted', { no: v.voucherNo }))
      onPosted()
    } catch (err) {
      setError(t(`errors:${err instanceof ApiError ? err.code : 'ERR_UNKNOWN'}`))
    } finally {
      setBusy(false)
    }
  }

  return (
    <Dialog open onOpenChange={(o) => !o && onClose()}>
      <DialogContent aria-describedby={undefined}>
        <DialogHeader>
          <DialogTitle>
            {t(mode === 'expense' ? 'cash.voucher.expenseTitle' : 'cash.voucher.depositTitle')}
          </DialogTitle>
          {series?.[0] && (
            <p className="font-mono text-13 text-fk-muted">
              {t('cash.voucher.number', { no: formatNumber(series[0], depotCode) })}
            </p>
          )}
        </DialogHeader>
        <form onSubmit={post} className="grid grid-cols-2 gap-3">
          {mode === 'bank' && (
            <Segmented
              label={t('cash.voucher.kind')}
              className="col-span-2 self-start justify-self-start"
              value={kind}
              onChange={setKind}
              options={(['BANK_DEPOSIT', 'BANK_WITHDRAWAL'] as const).map((k) => ({
                value: k,
                label: t(`cash.voucher.${k}`),
              }))}
            />
          )}
          <Field
            label={t(
              mode === 'expense' ? 'cash.voucher.expenseAccount' : 'cash.voucher.bankAccount',
            )}
            htmlFor={ids.account}
            className="col-span-2"
          >
            <select
              id={ids.account}
              className={selectCls}
              value={account ?? ''}
              onChange={(e) => setAccountId(Number(e.target.value))}
            >
              {accounts.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.code} · {a.name}
                </option>
              ))}
            </select>
          </Field>
          <DateField
            label={t('cash.voucher.date')}
            value={date}
            onChange={setDate}
            inputClassName="h-10"
          />
          <Field label={t('cash.voucher.amount')} htmlFor={ids.amount}>
            <MoneyInput
              id={ids.amount}
              value={amount}
              onChange={setAmount}
              invalid={touched && bad.amount}
            />
          </Field>
          <TextField
            label={t('cash.voucher.description')}
            className="col-span-2"
            value={description}
            onChange={setDescription}
            aria-invalid={(touched && bad.description) || undefined}
          />
          {error && (
            <p role="alert" className="col-span-2 text-13 text-fk-negative">
              {error}
            </p>
          )}
          <DialogFooter className="col-span-2 mt-1">
            <Button type="button" variant="outline" onClick={onClose}>
              {t('common:cancel')}
            </Button>
            <Button type="submit" disabled={busy} className="min-w-[88px]">
              {t('cash.voucher.post')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}
