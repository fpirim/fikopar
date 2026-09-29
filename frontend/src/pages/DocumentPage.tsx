import { useCallback, useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { ApiError } from '../api/client'
import {
  cancelDocument,
  createDocument,
  deleteDocument,
  getDocument,
  getSicilSummary,
  gridDocuments,
  listMembers,
  listNumberSeries,
  postDocument,
  printDocument,
  resolvePartCodes,
  updateDocument,
} from '../api/endpoints'
import { errorText } from '../api/errorText'
import type {
  CreditLimitExceeded,
  Document,
  DocumentType,
  DocumentWarning,
  NumberSeries,
} from '../api/types'
import { useAuth } from '../hooks/useAuth'
import { useApi } from '../hooks/useApi'
import { useDepot, withDepot } from '../hooks/useDepot'
import { useDepots } from '../hooks/useDepots'
import { useDownload } from '../hooks/useDownload'
import { useIdempotencyKey } from '../hooks/useIdempotencyKey'
import { useOrgId } from '../hooks/useOrgId'
import { useToast } from '../context/ToastContext'
import {
  BILLS,
  billTypeFor,
  canPriceDiff,
  isPriceDiff,
  isReturn,
  isWaybill,
  needsOriginal,
  originTypes,
  PREFIX,
  returnTypeFor,
  stockDirection,
} from '../lib/documents'
import { formatDateTime, formatDay, formatQty, todayIso } from '../lib/format'
import { formatNumber } from '../lib/numbering'
import { cn } from '@/lib/utils'
import type { AccountHit } from '../components/AccountPicker'
import ConflictDialog from '../components/ConflictDialog'
import ConfirmDialog from '../components/ui/ConfirmDialog'
import { Button } from '../components/ui/button'
import { Chip } from '../components/ui/chip'
import SelectCompany from '../components/layout/SelectCompany'
import HeaderForm from './document/HeaderForm'
import { LinesGrid, type LinesGridHandle } from './document/LinesGrid'
import SidePanel from './document/SidePanel'
import CreditLimitDialog from './document/CreditLimitDialog'
import CancelDialog, { type Dependent } from './document/CancelDialog'
import { STATUS_TONE } from './document/status'
import {
  availableHere,
  blankHeader,
  blankLine,
  dueFor,
  headerOf,
  isFilled,
  lineOf,
  newKey,
  previewEffects,
  toWrite,
  totalsOf,
  withPart,
  type EditLine,
  type Header,
} from './document/model'
import { parseLinesCsv } from './document/csvLines'

export default function DocumentPage() {
  const orgId = useOrgId()
  const { documentId = 'new' } = useParams()
  const location = useLocation()
  // A new document keeps its editor when the URL takes the id after the first save.
  const [session, setSession] = useState({ id: documentId, key: `${documentId}${location.search}` })
  if (
    session.id !== documentId ||
    (documentId === 'new' && session.key !== `new${location.search}`)
  ) {
    const keep = (location.state as { keep?: boolean } | null)?.keep
    setSession({ id: documentId, key: keep ? session.key : `${documentId}${location.search}` })
  }
  return orgId ? <Editor key={session.key} orgId={orgId} /> : <SelectCompany />
}

type Busy = null | 'save' | 'post' | 'delete' | 'cancel' | 'load'

function Editor({ orgId }: Readonly<{ orgId: number }>) {
  const { t, i18n } = useTranslation(['documents', 'errors', 'common'])
  const { documentId } = useParams()
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const toast = useToast()
  const admin = useAuth().user?.role === 'admin'
  const { depotId: topDepot } = useDepot()
  const depots = useDepots()
  const download = useDownload()
  const saveKey = useIdempotencyKey()
  const postKey = useIdempotencyKey()
  const cancelKey = useIdempotencyKey()
  const grid = useRef<LinesGridHandle>(null)
  const fileInput = useRef<HTMLInputElement>(null)

  const isNew = documentId === 'new'
  const id = isNew ? null : Number(documentId)
  const firstDepot = useMemo(
    () => [...depots].sort((a, b) => a.code.localeCompare(b.code, undefined, { numeric: true }))[0],
    [depots],
  )

  const [doc, setDoc] = useState<Document | null>(null)
  const [header, setHeader] = useState<Header>(() =>
    blankHeader(
      (params.get('type') as DocumentType | null) ?? 'SALES_INVOICE_WITH_WAYBILL',
      topDepot,
      todayIso(),
    ),
  )
  const [seed, setSeed] = useState<EditLine[]>([])
  const [lines, setLinesState] = useState<EditLine[]>([])
  const linesRef = useRef<EditLine[]>([])
  const [dirty, setDirty] = useState(isNew)
  const [busy, setBusy] = useState<Busy>(isNew ? null : 'load')
  const [error, setError] = useState('')
  const [lineMsg, setLineMsg] = useState<string | null>(null)
  const [badRow, setBadRow] = useState<number | null>(null)
  const [conflict, setConflict] = useState(false)
  const [credit, setCredit] = useState<{ details: CreditLimitExceeded; error: string } | null>(null)
  const [cancelling, setCancelling] = useState<{ error: string; dependents: Dependent[] } | null>(
    null,
  )
  const [deleting, setDeleting] = useState(false)
  const [notFound, setNotFound] = useState(false)

  const setLines = useCallback((next: EditLine[], reseed = false) => {
    linesRef.current = next
    setLinesState(next)
    if (reseed) setSeed(next)
  }, [])

  const status = doc?.status ?? 'DRAFT'
  const editable = status === 'DRAFT' && busy !== 'load'
  const depotId = header.depotId ?? firstDepot?.id ?? null
  const pd = isPriceDiff(header.type)

  const sicilId = header.account?.sicilId ?? null
  const summary = useApi(sicilId ? () => getSicilSummary(orgId, sicilId) : null, [orgId, sicilId])
  const card = summary.data
  const members = useApi(() => listMembers(orgId), [orgId]).data ?? []

  const originId = needsOriginal(header.type) ? header.linkedDocumentId : null
  const original = useApi(originId ? () => getDocument(orgId, originId) : null, [orgId, originId])
  const origin = originId && original.data?.id === originId ? original.data : null

  const linkTypes = needsOriginal(header.type)
    ? originTypes(header.type)
    : BILLS[header.type]
      ? [BILLS[header.type]!]
      : []
  const linkKey = linkTypes.join(',')
  const links = useApi(
    editable && sicilId && depotId && linkTypes.length
      ? () =>
          gridDocuments(orgId, {
            startRow: 0,
            endRow: 100,
            types: linkTypes,
            statuses: ['POSTED'],
            sicilId,
            depotId: isReturn(header.type) ? null : depotId,
            sortModel: [{ colId: 'documentDate', sort: 'desc' }],
          })
      : null,
    [orgId, linkKey, sicilId, depotId, editable],
  )
  const linkRows = (links.data?.rows ?? []).filter(
    (r) => !isReturn(header.type) || !r.waybillDocumentNo,
  )

  const prefix = PREFIX[header.type]
  const year = Number(header.documentDate.slice(0, 4))
  const series = useApi(
    status === 'DRAFT' && depotId ? () => listNumberSeries(orgId, { prefix, depotId, year }) : null,
    [orgId, prefix, depotId, year, status],
  )
  const depotCode = depots.find((d) => d.id === depotId)?.code ?? String(depotId ?? '')
  const nextNo = useMemo(() => {
    if (!series.data || !depotId) return ''
    const s: NumberSeries = series.data[0] ?? {
      id: 0,
      prefix: prefix as NumberSeries['prefix'],
      depotId,
      year,
      width: 6,
      nextValue: 1,
      version: 0,
    }
    return formatNumber(s, depotCode)
  }, [series.data, depotId, depotCode, prefix, year])
  const number = doc?.documentNo ?? nextNo

  // ---- loading ----

  const accountOf = (d: Document, code?: string): AccountHit => ({
    sicilId: d.sicilId,
    accountId: null,
    hesapNo: code ?? '',
    hesapAdi: d.sicilName ?? '',
  })

  const load = useCallback(
    (d: Document) => {
      setDoc(d)
      setHeader((h) =>
        headerOf(
          d,
          h.account?.sicilId === d.sicilId && h.account.hesapNo
            ? h.account
            : accountOf(d, undefined),
        ),
      )
      setLines(d.lines.map(lineOf), true)
      setDirty(false)
      setBadRow(null)
      setLineMsg(null)
    },
    [setLines],
  )

  useEffect(() => {
    if (isNew || id === null || doc?.id === id) return
    let live = true
    getDocument(orgId, id)
      .then((d) => live && load(d))
      .catch((e) => {
        if (!live) return
        if (e instanceof ApiError && e.status === 404) setNotFound(true)
        else setError(errorText(t, e))
      })
      .finally(() => live && setBusy(null))
    return () => {
      live = false
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [orgId, id])

  // A new bill, return or price difference starts from its waybill or original.
  useEffect(() => {
    if (!isNew) return
    const from = Number(params.get('waybill') || params.get('original') || 0)
    if (!from) return
    getDocument(orgId, from)
      .then((src) => fillFrom(src, params.get('waybill') ? 'waybill' : 'original'))
      .catch((e) => setError(errorText(t, e)))
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  function fillFrom(src: Document, as: 'waybill' | 'original') {
    const account = accountOf(src)
    if (as === 'waybill') {
      const type = billTypeFor(src.type)
      if (!type) return
      setHeader((h) => ({
        ...blankHeader(type, src.depotId, h.documentDate),
        account,
        waybillDocumentId: src.id,
        salesRepUserId: src.salesRepUserId ?? null,
      }))
      setLines(
        src.lines.map((l) => ({
          ...lineOf(l),
          key: newKey(),
          billedLineId: l.id,
          reservedQty: '0',
        })),
        true,
      )
      return
    }
    const type =
      (params.get('type') as DocumentType | null) ?? returnTypeFor(src.type) ?? 'SALES_RETURN'
    setHeader((h) => ({
      ...blankHeader(type, src.depotId, h.documentDate),
      account,
      linkedDocumentId: src.id,
      salesRepUserId: src.salesRepUserId ?? null,
    }))
    setLines(linesFromOriginal(src, type), true)
  }

  function linesFromOriginal(src: Document, type: DocumentType): EditLine[] {
    if (isPriceDiff(type)) {
      return src.lines.map((l) =>
        blankLine({
          originLineId: l.id,
          partId: l.partId ?? null,
          stockNo: l.stockNo ?? '',
          name: l.name ?? '',
          qty: '1',
          vatPct: l.vatPct ?? '20',
        }),
      )
    }
    return src.lines
      .map((l) => ({ l, left: Number(l.qty) - Number(l.returnedQty ?? 0) }))
      .filter((x) => x.left > 0)
      .map(({ l, left }) => ({
        ...lineOf(l),
        key: newKey(),
        qty: String(left),
        originLineId: l.id,
        billedLineId: null,
        reservedQty: '0',
      }))
  }

  // ---- editing ----

  const patch = (p: Partial<Header>) => {
    setHeader((h) => ({ ...h, ...p }))
    setDirty(true)
    setError('')
  }

  function changeType(type: DocumentType) {
    setHeader((h) => {
      const keepLink =
        needsOriginal(type) &&
        needsOriginal(h.type) &&
        originTypes(type).join() === originTypes(h.type).join()
      return {
        ...h,
        type,
        linkedDocumentId: keepLink ? h.linkedDocumentId : null,
        waybillDocumentId: BILLS[type] === BILLS[h.type] ? h.waybillDocumentId : null,
        priceDirection: isPriceDiff(type) ? (h.priceDirection ?? 'INCREASE') : null,
        shipDate: isPriceDiff(type) ? null : (h.shipDate ?? h.documentDate),
      }
    })
    if (isPriceDiff(type) !== pd || (needsOriginal(type) && !needsOriginal(header.type))) {
      setLines([], true)
    }
    setDirty(true)
  }

  async function changeLink(linkId: number | null) {
    setDirty(true)
    if (needsOriginal(header.type)) {
      setHeader((h) => ({ ...h, linkedDocumentId: linkId }))
      if (!linkId) return setLines([], true)
      const src = await getDocument(orgId, linkId).catch(() => null)
      if (src) setLines(linesFromOriginal(src, header.type), true)
      return
    }
    setHeader((h) => ({ ...h, waybillDocumentId: linkId }))
    if (!linkId) {
      setLines(
        linesRef.current.map((l) => ({ ...l, billedLineId: null })),
        true,
      )
      return
    }
    const src = await getDocument(orgId, linkId).catch(() => null)
    if (src) {
      setLines(
        src.lines.map((l) => ({
          ...lineOf(l),
          key: newKey(),
          billedLineId: l.id,
          reservedQty: '0',
        })),
        true,
      )
    }
  }

  const onLines = useCallback(
    (next: EditLine[]) => {
      setLines(next)
      setDirty(true)
      setBadRow(null)
    },
    [setLines],
  )

  const caps = useMemo(() => {
    if (!isReturn(header.type) || !origin) return null
    return new Map(origin.lines.map((l) => [l.id, Number(l.qty) - Number(l.returnedQty ?? 0)]))
  }, [header.type, origin])

  // The card code comes with the summary.
  const account =
    header.account && !header.account.hesapNo && card?.sicilId === header.account.sicilId
      ? { ...header.account, hesapNo: card.accountCode ?? '', accountId: card.accountId }
      : header.account

  const totals = useMemo(() => totalsOf(lines, header.type), [lines, header.type])
  const due = dueFor(header, card?.paymentTermDays)
  const effects =
    doc && !dirty && doc.effects
      ? doc.effects
      : previewEffects({ ...header, depotId, account }, lines, totals, card, origin?.type ?? null)

  // ---- server calls ----

  function missing(): string | null {
    if (!header.account) return t('need.account')
    if (!depotId) return t('need.depot')
    if (needsOriginal(header.type) && !header.linkedDocumentId) return t('need.original')
    return null
  }

  async function save(): Promise<Document | null> {
    grid.current?.stopEditing()
    const why = missing()
    if (why) {
      setError(why)
      return null
    }
    const body = toWrite({ ...header, depotId, dueDate: due }, linesRef.current)
    setBusy('save')
    setError('')
    try {
      let d: Document
      if (doc) {
        d = await updateDocument(orgId, doc, body)
      } else {
        d = await createDocument(orgId, body, saveKey.keyFor(body))
        saveKey.reset()
      }
      load(d)
      if (!doc) {
        navigate(withDepot(`/invoices/${d.id}`, topDepot), { replace: true, state: { keep: true } })
      }
      return d
    } catch (e) {
      fail(e)
      return null
    } finally {
      setBusy(null)
    }
  }

  function fail(e: unknown) {
    if (e instanceof ApiError && e.status === 412) {
      setConflict(true)
      return
    }
    const row = e instanceof ApiError ? /^lines\[(\d+)\]/.exec(e.field ?? '') : null
    if (row) setBadRow(Number(row[1]))
    const msg = errorText(t, e, { date: header.documentDate })
    setError(row ? t('onLine', { row: Number(row[1]) + 1, msg }) : msg)
  }

  function negative(ws: DocumentWarning[] | undefined) {
    for (const w of ws ?? []) {
      if (w.code !== 'NEGATIVE_STOCK') continue
      const l = lines.find((x) => x.partId === w.partId)
      toast.info(
        t('negativeStock', {
          part: l?.stockNo ?? w.partId,
          code: depots.find((d) => d.id === w.depotId)?.code ?? w.depotId,
          qty: formatQty(w.onHandAfter),
        }),
      )
    }
  }

  async function post(reason?: string) {
    let d = doc
    if (!d || dirty) d = await save()
    if (!d) return
    setBusy('post')
    setError('')
    try {
      const posted = await postDocument(
        orgId,
        d,
        postKey.keyFor({ id: d.id, version: d.version, reason }),
        reason ? { reason } : undefined,
      )
      postKey.reset()
      setCredit(null)
      load(posted)
      toast.success(t('posted', { no: posted.documentNo }))
      negative(posted.warnings)
    } catch (e) {
      if (e instanceof ApiError && e.code === 'ERR_CREDIT_LIMIT_EXCEEDED' && e.details) {
        setCredit({ details: e.details, error: '' })
      } else if (credit) {
        setCredit({ ...credit, error: errorText(t, e, { date: header.documentDate }) })
      } else {
        fail(e)
      }
    } finally {
      setBusy(null)
    }
  }

  async function cancel(reason: string) {
    if (!doc) return
    setBusy('cancel')
    try {
      const c = await cancelDocument(orgId, doc, reason, cancelKey.keyFor({ id: doc.id, reason }))
      cancelKey.reset()
      setCancelling(null)
      load(c)
      toast.success(t('cancelled', { no: c.documentNo }))
      negative(c.warnings)
    } catch (e) {
      if (e instanceof ApiError && e.status === 412) {
        setCancelling(null)
        setConflict(true)
        return
      }
      let dependents: Dependent[] = []
      if (e instanceof ApiError && e.code === 'ERR_DOCUMENT_HAS_DEPENDENTS') {
        const fresh = await getDocument(orgId, doc.id).catch(() => doc)
        dependents = await Promise.all(
          (fresh.dependentDocumentIds ?? []).map((depId) =>
            getDocument(orgId, depId)
              .then((x) => ({ id: depId, no: x.documentNo ?? `#${depId}` }))
              .catch(() => ({ id: depId, no: `#${depId}` })),
          ),
        )
      }
      setCancelling({ error: errorText(t, e, { date: todayIso() }), dependents })
    } finally {
      setBusy(null)
    }
  }

  async function remove() {
    if (!doc) return
    setBusy('delete')
    try {
      await deleteDocument(orgId, doc)
      toast.success(t('deleted'))
      navigate(withDepot('/invoices', topDepot))
    } catch (e) {
      setDeleting(false)
      fail(e)
    } finally {
      setBusy(null)
    }
  }

  async function importCsv(file: File) {
    const rows = parseLinesCsv(await file.text())
    if (!rows.length) {
      setLineMsg(t('csv.empty'))
      return
    }
    try {
      const res = await resolvePartCodes(
        orgId,
        rows.map((r) => r.code),
        depotId,
      )
      const added: EditLine[] = []
      const skipped: string[] = []
      res.forEach((r, i) => {
        if (r.status !== 'FOUND' || !r.part) {
          skipped.push(`${rows[i].code} (${t(`csv.${r.status}`)})`)
          return
        }
        const l = withPart(blankLine(), r.part, card)
        added.push({
          ...l,
          qty: rows[i].qty || l.qty,
          unitPrice: rows[i].price || l.unitPrice,
        })
      })
      const kept = linesRef.current.filter((l) => isFilled(l, header.type))
      setLines([...kept, ...added], true)
      setDirty(true)
      setLineMsg(
        skipped.length
          ? t('csv.skipped', { count: added.length, skipped: skipped.join(', ') })
          : t('csv.added', { count: added.length }),
      )
    } catch (e) {
      setLineMsg(errorText(t, e))
    }
  }

  // ---- keys: F2 stays in the editor (the global part finder is for other pages) ----

  function onKeyDown(e: KeyboardEvent<HTMLDivElement>) {
    if (e.key === 'F2' || e.key === 'Insert') {
      e.stopPropagation()
      if (!editable) return
      const inGrid = (e.target as HTMLElement).closest('.fk-grid')
      if (!inGrid) {
        e.preventDefault()
        if (e.key === 'Insert') grid.current?.addLine()
        else grid.current?.editPart()
      }
    }
  }

  // ---- view ----

  const warnings: DocumentWarning[] = useMemo(() => {
    if (status !== 'DRAFT') return []
    if (doc && !dirty) return doc.warnings
    if (stockDirection(header.type, header.waybillDocumentId !== null) !== 'OUT') return []
    const out: DocumentWarning[] = []
    lines.forEach((l, i) => {
      const a = availableHere(l, depotId)
      if (l.partId !== null && a !== null && l.qty && a < Number(l.qty)) {
        out.push({
          code: 'STOCK_SHORT',
          lineNo: i + 1,
          partId: l.partId,
          depotId,
          requested: l.qty,
          available: String(a),
        })
      }
    })
    return out
  }, [status, doc, dirty, lines, header.type, header.waybillDocumentId, depotId])

  if (notFound) {
    return (
      <div className="flex flex-col gap-3">
        <h1 className="text-2xl font-semibold">{t('notFound')}</h1>
        <Link to={withDepot('/invoices', topDepot)}>{t('backToList')}</Link>
      </div>
    )
  }

  const partsFixed =
    header.waybillDocumentId !== null || (isReturn(header.type) && originId !== null)
  const canAdd = editable && !partsFixed
  const saving = busy === 'save'
  const posting = busy === 'post'
  const followUps = doc && status === 'POSTED' ? followUpsOf(doc) : []

  return (
    <div className="flex flex-col gap-3" onKeyDown={onKeyDown}>
      <div className="flex items-center gap-3">
        <h1
          className={cn(
            'min-w-[180px] whitespace-nowrap font-mono text-[22px] font-medium',
            !doc?.documentNo && 'text-fk-secondary',
          )}
          title={doc?.documentNo ? undefined : t('numberOnPost')}
        >
          {number || t('newDocument')}
        </h1>
        <Chip tone={STATUS_TONE[status]}>{t(`status.${status}`)}</Chip>
        {pd && header.priceDirection && (
          <Chip tone={header.priceDirection === 'INCREASE' ? 'blue' : 'grey'}>
            {t(`dir.${header.priceDirection}`)}
          </Chip>
        )}
        {doc?.creditOverride && (
          <span title={doc.creditOverride.reason}>
            <Chip tone="amber">{t('overridden')}</Chip>
          </span>
        )}
        <div className="flex-1" />
        <Button
          variant="outline"
          disabled={!doc || download.busy !== null}
          onClick={() =>
            doc &&
            download.run('pdf', `${doc.documentNo ?? t('draftFile')}.pdf`, () =>
              printDocument(orgId, doc.id),
            )
          }
        >
          {t('pdf')}
        </Button>
        {status === 'DRAFT' && (
          <>
            {doc && (
              <Button variant="danger" disabled={busy !== null} onClick={() => setDeleting(true)}>
                {t('delete')}
              </Button>
            )}
            <Button
              variant="outline"
              disabled={busy !== null || !dirty}
              onClick={() => void save()}
              className="min-w-[112px]"
            >
              {saving ? t('saving') : t('saveDraft')}
            </Button>
            <Button disabled={busy !== null} onClick={() => void post()} className="min-w-[88px]">
              {posting ? t('posting') : t('post')}
            </Button>
          </>
        )}
        {status === 'POSTED' && (
          <>
            {followUps.map((f) => (
              <Button key={f.key} variant="outline" asChild>
                <Link to={withDepot(f.to, topDepot)}>{t(f.key)}</Link>
              </Button>
            ))}
            <Button
              variant="danger"
              disabled={busy !== null}
              onClick={() => setCancelling({ error: '', dependents: [] })}
            >
              {t('cancelDocument')}
            </Button>
          </>
        )}
      </div>
      <StatusLine doc={doc} />
      <HeaderForm
        orgId={orgId}
        header={{ ...header, depotId, account }}
        due={due}
        editable={editable}
        locked={partsFixed || (isNew && Boolean(params.get('original') || params.get('waybill')))}
        depots={depots}
        members={members}
        cardSalesRep={null}
        links={linkRows}
        linkError={null}
        onChange={patch}
        onType={changeType}
        onLink={(l) => void changeLink(l)}
      />
      {error && (
        <p role="alert" className="text-13 text-fk-negative">
          {error}
        </p>
      )}
      <div className="flex items-start gap-4 max-[1320px]:flex-col max-[1320px]:items-stretch">
        <section aria-label={t('lines')} className="flex min-w-0 flex-1 flex-col">
          <div className="mb-2 flex items-center gap-2">
            <Button
              variant="outline"
              size="sm"
              disabled={!canAdd}
              onClick={() => grid.current?.addLine()}
            >
              {t('addLine')} <Kbd>Ins</Kbd>
            </Button>
            <Button
              variant="outline"
              size="sm"
              disabled={!editable || header.waybillDocumentId !== null || lines.length === 0}
              onClick={() => grid.current?.deleteFocused()}
            >
              {t('deleteLine')} <Kbd>Del</Kbd>
            </Button>
            {!pd && (
              <>
                <Button
                  variant="outline"
                  size="sm"
                  disabled={!canAdd}
                  onClick={() => fileInput.current?.click()}
                >
                  {t('importCsv')}
                </Button>
                <input
                  ref={fileInput}
                  type="file"
                  accept=".csv,text/csv,text/plain"
                  aria-label={t('importCsv')}
                  className="hidden"
                  onChange={(e) => {
                    const f = e.target.files?.[0]
                    e.target.value = ''
                    if (f) void importCsv(f)
                  }}
                />
              </>
            )}
            <div className="flex-1" />
            {editable && (
              <span className="flex items-center gap-1.5 whitespace-nowrap text-xs text-fk-muted">
                <Kbd>Enter</Kbd> {t('keys.edit')} <Kbd>Tab</Kbd> {t('keys.next')} <Kbd>F2</Kbd>{' '}
                {t('keys.search')}
              </span>
            )}
          </div>
          <div className="overflow-hidden rounded-lg border border-fk-border bg-fk-surface">
            {busy !== 'load' && (
              <LinesGrid
                ref={grid}
                key={`${header.type === 'PRICE_DIFFERENCE'}-${i18n.language}`}
                orgId={orgId}
                depots={depots}
                depotId={depotId}
                date={header.documentDate}
                card={card}
                amountOnly={pd}
                editable={editable}
                canAdd={canAdd}
                canDelete={editable && header.waybillDocumentId === null}
                partsFixed={partsFixed}
                qtyFixed={header.waybillDocumentId !== null}
                seed={seed}
                rows={lines}
                onChange={onLines}
                onInvalid={setLineMsg}
                originLines={pd ? (origin?.lines ?? null) : null}
                caps={caps}
                badRow={badRow}
              />
            )}
          </div>
          {lineMsg && (
            <p role="status" className="px-0.5 pt-2 text-13 text-fk-negative">
              {lineMsg}
            </p>
          )}
          <Warnings
            warnings={warnings}
            lines={lines}
            depots={depots}
            expiresAt={status === 'DRAFT' && !dirty ? (doc?.reservation?.expiresAt ?? null) : null}
          />
        </section>
        <div className="mt-[38px] max-[1320px]:mt-0">
          <SidePanel
            totals={totals}
            effects={effects}
            status={doc ? status : null}
            depots={depots}
          />
        </div>
      </div>
      {conflict && (
        <ConflictDialog
          title={t('conflict.title')}
          message={t('conflict.body')}
          onReload={() => {
            setConflict(false)
            if (doc) {
              setBusy('load')
              getDocument(orgId, doc.id)
                .then(load)
                .catch((e) => setError(errorText(t, e)))
                .finally(() => setBusy(null))
            }
          }}
        />
      )}
      {credit && (
        <CreditLimitDialog
          details={credit.details}
          admin={admin}
          busy={posting}
          error={credit.error}
          onOverride={(reason) => void post(reason)}
          onClose={() => setCredit(null)}
        />
      )}
      {cancelling && doc && (
        <CancelDialog
          number={doc.documentNo ?? ''}
          busy={busy === 'cancel'}
          error={cancelling.error}
          dependents={cancelling.dependents}
          onCancel={(r) => void cancel(r)}
          onClose={() => setCancelling(null)}
        />
      )}
      <ConfirmDialog
        open={deleting}
        title={t('deleteDialog.title')}
        message={t('deleteDialog.body')}
        confirmText={t('delete')}
        destructive
        loading={busy === 'delete'}
        onConfirm={() => void remove()}
        onClose={() => setDeleting(false)}
      />
    </div>
  )
}

function followUpsOf(d: Document): { key: string; to: string }[] {
  const out: { key: string; to: string }[] = []
  if (isWaybill(d.type) && !d.billedByDocumentId && billTypeFor(d.type)) {
    out.push({ key: 'billWaybill', to: `/invoices/new?waybill=${d.id}` })
  }
  const ret = returnTypeFor(d.type)
  if (ret && !d.waybillDocumentId) {
    out.push({ key: 'createReturn', to: `/invoices/new?type=${ret}&original=${d.id}` })
  }
  if (canPriceDiff(d.type)) {
    out.push({ key: 'createPriceDiff', to: `/invoices/new?type=PRICE_DIFFERENCE&original=${d.id}` })
  }
  return out
}

function StatusLine({ doc }: Readonly<{ doc: Document | null }>) {
  const { t } = useTranslation('documents')
  if (!doc || doc.status === 'DRAFT') return null
  const bits: React.ReactNode[] = []
  if (doc.postedAt) bits.push(t('postedAt', { at: formatDateTime(doc.postedAt) }))
  if (doc.journalEntryId) {
    bits.push(
      <Link key="je" to={`/journal/${doc.journalEntryId}`}>
        {t('entry', { id: doc.journalEntryId })}
      </Link>,
    )
  }
  if (doc.linkedDocumentId) {
    bits.push(
      <Link key="ln" to={`/invoices/${doc.linkedDocumentId}`} className="font-mono">
        {t('linked', { no: doc.linkedDocumentNo ?? `#${doc.linkedDocumentId}` })}
      </Link>,
    )
  }
  if (doc.waybillDocumentId) {
    bits.push(
      <Link key="wb" to={`/invoices/${doc.waybillDocumentId}`} className="font-mono">
        {t('bills', { no: doc.waybillDocumentNo ?? `#${doc.waybillDocumentId}` })}
      </Link>,
    )
  }
  if (doc.billedByDocumentId) {
    bits.push(
      <Link key="bb" to={`/invoices/${doc.billedByDocumentId}`}>
        {t('billedBy')}
      </Link>,
    )
  }
  if (doc.status === 'CANCELLED') {
    bits.push(
      <span key="cx" className="text-fk-negative">
        {t('cancelledOn', { date: formatDay(doc.cancelDate), reason: doc.cancelReason ?? '' })}
      </span>,
    )
    if (doc.reversalEntryId) {
      bits.push(
        <Link key="rv" to={`/journal/${doc.reversalEntryId}`}>
          {t('reversal', { id: doc.reversalEntryId })}
        </Link>,
      )
    }
  }
  if (doc.creditOverride) {
    bits.push(
      t('overrideNote', {
        user: doc.creditOverride.userName ?? doc.creditOverride.userId,
        reason: doc.creditOverride.reason,
      }),
    )
  }
  return (
    <div className="-mt-1 flex flex-wrap items-center gap-x-2 text-13 text-fk-muted">
      {bits.map((b, i) => (
        <span key={i} className="flex items-center gap-2 whitespace-nowrap">
          {i > 0 && <span aria-hidden="true">·</span>}
          {b}
        </span>
      ))}
    </div>
  )
}

function Warnings({
  warnings,
  lines,
  depots,
  expiresAt,
}: Readonly<{
  warnings: DocumentWarning[]
  lines: EditLine[]
  depots: { id?: number; code: string }[]
  expiresAt: string | null
}>) {
  const { t } = useTranslation('documents')
  const part = (w: DocumentWarning) =>
    (w.lineNo ? lines[w.lineNo - 1]?.stockNo : undefined) ??
    lines.find((l) => l.partId === w.partId)?.stockNo ??
    ''
  const code = (id: number | null | undefined) => depots.find((d) => d.id === id)?.code ?? id
  if (!warnings.length && !expiresAt) return null
  return (
    <ul aria-label={t('warnings')} className="flex flex-col gap-1.5 px-0.5 pt-2.5 text-13">
      {warnings.map((w, i) => (
        <li key={i} className="flex items-center gap-2">
          <Chip tone={w.code === 'STOCK_SHORT' ? 'red' : 'amber'}>{t(`warn.${w.code}`)}</Chip>
          <span className={w.code === 'STOCK_SHORT' ? 'text-fk-negative' : 'text-fk-secondary'}>
            {t(`warnText.${w.code}`, {
              line: w.lineNo,
              part: part(w),
              requested: formatQty(w.requested),
              available: formatQty(w.available),
              depot: code(w.depotId),
            })}
          </span>
        </li>
      ))}
      {expiresAt && (
        <li className="flex items-center gap-2 text-fk-muted">
          <Chip tone="blue">{t('warn.HELD')}</Chip>
          <span>{t('heldUntil', { at: formatDateTime(expiresAt) })}</span>
        </li>
      )}
    </ul>
  )
}

function Kbd({ children }: Readonly<{ children: React.ReactNode }>) {
  return (
    <kbd className="rounded border border-b-2 border-fk-input bg-fk-surface px-[5px] py-px font-mono text-2xs font-medium text-fk-secondary">
      {children}
    </kbd>
  )
}
