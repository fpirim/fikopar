import { useTranslation } from 'react-i18next'
import type { Depot, DocumentEffects, DocumentStatus } from '../../api/types'
import { formatMoney, formatQty } from '../../lib/format'
import type { Totals } from '../../lib/documents'
import { Chip, type ChipTone } from '../../components/ui/chip'
import { KeyValue } from '../../components/ui/panel'

const creditTone = (pct: number): ChipTone => (pct > 100 ? 'red' : pct >= 80 ? 'amber' : 'green')

const card = 'flex flex-col gap-[9px] rounded-lg border border-fk-border bg-fk-surface px-4 py-3.5'

// Totals, VAT per rate, and what posting does (or did).
export default function SidePanel({
  totals,
  effects,
  status,
  depots,
}: Readonly<{
  totals: Totals
  effects: DocumentEffects | null
  status: DocumentStatus | null
  depots: Depot[]
}>) {
  const { t } = useTranslation('documents')
  const code = (id: number) => depots.find((d) => d.id === id)?.code ?? String(id)
  const ledger = effects?.ledger?.[0]
  const pct = effects?.creditUsedPctAfter
  return (
    <aside
      aria-label={t('totals')}
      className="flex w-[280px] shrink-0 flex-col gap-3 max-[1320px]:w-full max-[1320px]:flex-row max-[1320px]:items-start [&>section]:max-[1320px]:flex-1"
    >
      <section aria-label={t('totals')} className={card}>
        <KeyValue label={t('gross')}>{formatMoney(totals.grossTotal)}</KeyValue>
        <KeyValue label={t('discount')}>{formatMoney(-Number(totals.discountTotal))}</KeyValue>
        <KeyValue label={t('subtotal')}>{formatMoney(totals.netTotal)}</KeyValue>
        {totals.vatBreakdown.map((v) => (
          <KeyValue key={v.vatPct} label={t('vatRate', { rate: formatQty(v.vatPct) })}>
            {formatMoney(v.vat)}
          </KeyValue>
        ))}
        {totals.vatBreakdown.length === 0 && (
          <KeyValue label={t('vat')}>{formatMoney('0')}</KeyValue>
        )}
        <KeyValue
          label={t('total')}
          className="border-t border-fk-border pt-[9px] text-base font-semibold"
        >
          {formatMoney(totals.grandTotal)}
        </KeyValue>
      </section>
      <section aria-labelledby="doc-effects" className={card}>
        <h2 id="doc-effects" className="text-15 font-semibold">
          {status === 'POSTED'
            ? t('onPostDone')
            : status === 'CANCELLED'
              ? t('reversed')
              : t('onPost')}
        </h2>
        <KeyValue label={t('account')}>
          {ledger ? (
            <span className="whitespace-nowrap">
              {ledger.side === 'DEBIT' ? '+' : '−'}
              {formatMoney(ledger.amount)} {t(ledger.side === 'DEBIT' ? 'dr' : 'cr')}
            </span>
          ) : (
            '—'
          )}
        </KeyValue>
        {effects?.stock?.length ? (
          effects.stock.map((s) => (
            <KeyValue key={s.depotId} label={t('stockDepot', { code: code(s.depotId) })}>
              {formatQty(s.qtyDelta, { sign: true })}
            </KeyValue>
          ))
        ) : (
          <KeyValue label={t('stockMove')}>—</KeyValue>
        )}
        {pct !== null && pct !== undefined && status === 'DRAFT' && (
          <div className="flex items-center justify-between gap-3 text-sm">
            <span>{t('creditUsed')}</span>
            <Chip tone={creditTone(pct)}>{t('pct', { pct })}</Chip>
          </div>
        )}
      </section>
    </aside>
  )
}
