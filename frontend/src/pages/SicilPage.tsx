import { useMemo, useState } from 'react'
import type { ColDef } from 'ag-grid-community'
import { useTranslation } from 'react-i18next'
import { createSicil, updateSicil } from '../api/endpoints'
import { errorText } from '../api/errorText'
import type { Sicil } from '../api/types'
import { formatDate } from '../lib/format'
import { useToast } from '../context/ToastContext'
import { useOrganization } from '../context/OrganizationContext'
import CrudGridPage from '../components/ui/CrudGridPage'
import DialogShell from '../components/ui/DialogShell'
import PhoneInput from '../components/ui/PhoneInput'
import NumericInput from '../components/ui/NumericInput'
import { Trash2, Plus } from 'lucide-react'
import type { SicilTipi, SicilTelefon, TelefonTipi } from '../types/sicil'
import { SICIL_TIPLERI, TELEFON_TIPLERI } from '../types/sicil'

// Top-level component, not defined inside the parent (Sonar S6478).
function SicilTipiCell({ value }: Readonly<{ value?: SicilTipi }>) {
  const { t } = useTranslation('sicil')
  if (!value) return null
  return (
    <span className="rounded-full bg-primary/10 px-2.5 py-1 text-xs font-medium text-primary">
      {t(`sicil:hesapTipi.${value}`)}
    </span>
  )
}

function SicilDialog({
  organizationId,
  card,
  onSaved,
  onClose,
}: Readonly<{
  organizationId: number
  card?: Sicil
  onSaved: () => void
  onClose: () => void
}>) {
  const { t } = useTranslation(['sicil', 'common', 'errors'])
  const toast = useToast()

  const [hesapNo, setHesapNo] = useState(card?.hesap_no || '')
  const [hesapAdi, setHesapAdi] = useState(card?.hesap_adi || '')
  const [adres, setAdres] = useState(card?.adres || '')
  const [ilce, setIlce] = useState(card?.ilce || '')
  const [sehir, setSehir] = useState(card?.sehir || '')
  const [vDairesi, setVDairesi] = useState(card?.v_dairesi || '')
  const [vNo, setVNo] = useState(card?.v_no || '')
  // Client-only uid for React keys; not sent to the API.
  const [telefonlar, setTelefonlar] = useState<Array<SicilTelefon & { uid: string }>>(() =>
    (card?.telefonlar?.length ? card.telefonlar : []).map((tel) => ({
      ...tel,
      uid: crypto.randomUUID(),
    })),
  )
  const [tcNo, setTcNo] = useState(card?.tc_no || '')
  const [gun, setGun] = useState(card?.gun || '')
  const [iskonto, setIskonto] = useState<string>(card?.iskonto == null ? '0' : String(card.iskonto))
  const [hesapTipi, setHesapTipi] = useState<SicilTipi>(card?.sicil_tipi || 'FIRMA')
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const addTelefon = () =>
    setTelefonlar((prev) => [...prev, { tipi: 'CEP', numara: '', uid: crypto.randomUUID() }])
  const removeTelefon = (idx: number) => setTelefonlar((prev) => prev.filter((_, i) => i !== idx))
  const updateTelefon = (idx: number, patch: Partial<SicilTelefon>) =>
    setTelefonlar((prev) => prev.map((t, i) => (i === idx ? { ...t, ...patch } : t)))

  const handleSubmit = async () => {
    setError('')
    setSaving(true)
    try {
      const iskontoNum = Number.parseFloat(iskonto || '0')
      const cleanTelefonlar = telefonlar
        .map((t) => ({ tipi: t.tipi, numara: t.numara.trim() }))
        .filter((t) => t.numara !== '')
      const payload = {
        hesap_no: hesapNo,
        hesap_adi: hesapAdi,
        adres,
        ilce,
        sehir,
        v_dairesi: vDairesi,
        v_no: vNo,
        tc_no: tcNo,
        gun,
        iskonto: Number.isFinite(iskontoNum) ? iskontoNum : 0,
        sicil_tipi: hesapTipi,
        telefonlar: cleanTelefonlar,
      }
      if (card) await updateSicil(organizationId, card, payload)
      else await createSicil(organizationId, payload)
      toast.success(card ? t('sicil:updated') : t('sicil:created'))
      onSaved()
    } catch (err) {
      const msg = errorText(t, err)
      setError(msg)
      toast.error(msg)
    } finally {
      setSaving(false)
    }
  }

  let submitLabel = t('common:create')
  if (saving) submitLabel = t('common:saving')
  else if (card) submitLabel = t('common:update')

  return (
    <DialogShell
      title={card ? t('sicil:dialog.editTitle') : t('sicil:dialog.createTitle')}
      error={error}
      maxWidth="2xl"
      saving={saving}
      submitLabel={submitLabel}
      onSubmit={handleSubmit}
      onClose={onClose}
    >
      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.id')}</label>
        <input
          type="text"
          value={card ? String(card.id) : '*'}
          readOnly
          className="w-full rounded-lg border bg-muted px-3 py-2 text-sm text-muted-foreground outline-none"
        />
      </div>

      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.hesapNo')}</label>
        <input
          type="text"
          value={hesapNo}
          onChange={(e) => setHesapNo(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          required
        />
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.hesapAdi')}</label>
        <input
          type="text"
          value={hesapAdi}
          onChange={(e) => setHesapAdi(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          required
        />
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.adres')}</label>
        <textarea
          value={adres}
          onChange={(e) => setAdres(e.target.value)}
          rows={3}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
        />
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.ilce')}</label>
          <input
            type="text"
            value={ilce}
            onChange={(e) => setIlce(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.sehir')}</label>
          <input
            type="text"
            value={sehir}
            onChange={(e) => setSehir(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          />
        </div>
      </div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.vDairesi')}</label>
          <input
            type="text"
            value={vDairesi}
            onChange={(e) => setVDairesi(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.vNo')}</label>
          <input
            type="text"
            value={vNo}
            onChange={(e) => setVNo(e.target.value)}
            className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
          />
        </div>
      </div>
      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.tcNo')}</label>
        <input
          type="text"
          value={tcNo}
          onChange={(e) => setTcNo(e.target.value)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
        />
      </div>

      <div className="space-y-2">
        <div className="flex items-center justify-between">
          <label className="text-sm font-medium">{t('sicil:telefonBolumu.title')}</label>
          <button
            type="button"
            onClick={addTelefon}
            className="flex items-center gap-1 rounded-lg border px-3 py-1.5 text-xs font-medium text-primary transition-all hover:-translate-y-0.5 hover:shadow-md hover:shadow-primary/20 active:scale-[0.98]"
          >
            <Plus className="h-3.5 w-3.5" />
            {t('sicil:telefonBolumu.add')}
          </button>
        </div>
        {telefonlar.length === 0 ? (
          <p className="text-xs text-muted-foreground">{t('sicil:telefonBolumu.empty')}</p>
        ) : (
          <ul className="space-y-2">
            {telefonlar.map((tel, idx) => (
              <li
                key={tel.uid}
                className="grid grid-cols-[8rem_1fr_auto] gap-2 items-start sm:items-center"
              >
                <select
                  value={tel.tipi}
                  onChange={(e) => updateTelefon(idx, { tipi: e.target.value as TelefonTipi })}
                  aria-label={t('sicil:telefonBolumu.title')}
                  className="rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
                >
                  {TELEFON_TIPLERI.map((tipi) => (
                    <option key={tipi} value={tipi}>
                      {t(`sicil:telefonTipi.${tipi}`)}
                    </option>
                  ))}
                </select>
                <PhoneInput
                  value={tel.numara}
                  onChange={(v) => updateTelefon(idx, { numara: v })}
                />
                <button
                  type="button"
                  onClick={() => removeTelefon(idx)}
                  aria-label={t('sicil:telefonBolumu.remove')}
                  className="flex h-9 w-9 items-center justify-center rounded-lg border text-destructive transition-all hover:bg-destructive/10"
                >
                  <Trash2 className="h-4 w-4" />
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.gun')}</label>
          <NumericInput
            value={gun}
            onChange={setGun}
            currencySymbol=""
            decimalPlaces={0}
            minimumValue="0"
            maximumValue="9999"
            padZeroOnEmpty={false}
          />
        </div>
        <div className="space-y-2">
          <label className="text-sm font-medium">{t('sicil:field.iskonto')} (%)</label>
          <NumericInput
            value={iskonto}
            onChange={setIskonto}
            currencySymbol="%"
            decimalPlaces={4}
            minimumValue="0"
            maximumValue="100"
            padZeroOnEmpty={false}
          />
        </div>
      </div>

      <div className="space-y-2">
        <label className="text-sm font-medium">{t('sicil:field.hesapTipi')}</label>
        <select
          value={hesapTipi}
          onChange={(e) => setHesapTipi(e.target.value as SicilTipi)}
          className="w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring"
        >
          {SICIL_TIPLERI.map((tip) => (
            <option key={tip} value={tip}>
              {t(`sicil:hesapTipi.${tip}`)}
            </option>
          ))}
        </select>
      </div>
    </DialogShell>
  )
}

export default function SicilPage() {
  const { t } = useTranslation(['sicil', 'common'])
  const { selectedId } = useOrganization()

  const columnDefs = useMemo<ColDef[]>(
    () => [
      { field: 'id', headerName: t('sicil:grid.id'), width: 80, sort: 'asc' },
      { field: 'hesap_no', headerName: t('sicil:grid.hesapNo'), width: 140 },
      { field: 'hesap_adi', headerName: t('sicil:grid.hesapAdi'), width: 260 },
      {
        field: 'sicil_tipi',
        headerName: t('sicil:grid.hesapTipi'),
        width: 120,
        cellRenderer: SicilTipiCell,
      },
      { field: 'ilce', headerName: t('sicil:grid.ilce'), width: 140 },
      { field: 'sehir', headerName: t('sicil:grid.sehir'), width: 140 },
      { field: 'tc_no', headerName: t('sicil:grid.tcNo'), width: 160 },
      { field: 'v_no', headerName: t('sicil:grid.vNo'), width: 140 },
      {
        field: 'created_at',
        headerName: t('sicil:grid.createdAt'),
        width: 140,
        valueFormatter: (p: { value?: string }) => formatDate(p.value),
      },
    ],
    [t],
  )

  // No grid without an org: sicils live under /orgs/{orgId}.
  if (!selectedId) {
    return (
      <div className="flex h-full flex-col gap-4">
        <h1 className="text-2xl font-bold">{t('sicil:title')}</h1>
        <p className="text-sm text-muted-foreground">{t('sicil:selectOrgPrompt')}</p>
      </div>
    )
  }

  return (
    // Keyed by org so the SSRM datasource is rebuilt on switch.
    <CrudGridPage<Sicil>
      key={selectedId}
      title={t('sicil:title')}
      gridPath={`/orgs/${selectedId}/sicils/grid`}
      deletePath={(id) => `/orgs/${selectedId}/sicils/${id}`}
      columnDefs={columnDefs}
      printTitle={t('sicil:printTitle')}
      printColumns={[
        { header: t('sicil:grid.hesapNo'), value: (c) => c.hesap_no },
        { header: t('sicil:grid.hesapAdi'), value: (c) => c.hesap_adi },
        { header: t('sicil:grid.hesapTipi'), value: (c) => t(`sicil:hesapTipi.${c.sicil_tipi}`) },
        { header: t('sicil:grid.ilce'), value: (c) => c.ilce ?? '' },
        { header: t('sicil:grid.sehir'), value: (c) => c.sehir ?? '' },
        { header: t('sicil:grid.tcNo'), value: (c) => c.tc_no ?? '' },
        { header: t('sicil:grid.vNo'), value: (c) => c.v_no ?? '' },
        { header: t('sicil:grid.createdAt'), value: (c) => formatDate(c.created_at) },
      ]}
      messages={{
        deleteTitle: t('sicil:deleteTitle'),
        deleteConfirm: (c) => t('sicil:deleteConfirm', { count: c }),
        deletedN: (c) => t('sicil:deletedN', { count: c }),
        deleteFailedN: (c) => t('sicil:deleteFailedN', { count: c }),
        deleteFailed: t('sicil:deleteFailed'),
      }}
      renderDialog={({ row, onSaved, onClose }) => (
        <SicilDialog organizationId={selectedId} card={row} onSaved={onSaved} onClose={onClose} />
      )}
    />
  )
}
