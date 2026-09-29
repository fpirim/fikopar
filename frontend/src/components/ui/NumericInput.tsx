import { useEffect, useRef } from 'react'
import AutoNumeric from 'autonumeric'
import { useTranslation } from 'react-i18next'
import { numberSeparators } from '../../lib/format'

// AutoNumeric wrapper that reproduces the legacy CCurrencyTextField caret behaviour (see e2e/numeric-input.spec.ts).
export interface NumericInputProps {
  value: number | string | null | undefined
  onChange: (value: string) => void
  decimalPlaces?: number
  minimumValue?: string
  maximumValue?: string
  digitGroupSeparator?: string
  decimalCharacter?: string
  decimalCharacterAlternative?: string
  currencySymbol?: string
  // false: an empty input stays empty instead of showing a padded zero.
  padZeroOnEmpty?: boolean
  leadingZero?: 'allow' | 'deny' | 'keep'
  textAlign?: 'left' | 'right' | 'center'
  disabled?: boolean
  required?: boolean
  placeholder?: string
  className?: string
  id?: string
  name?: string
  autoFocus?: boolean
  ariaLabel?: string
}

function displayToNumeric(display: string, decimalChar: string): string {
  const idx = display.indexOf(decimalChar)
  const intRaw = idx >= 0 ? display.substring(0, idx) : display
  const decRaw = idx >= 0 ? display.substring(idx + 1) : ''
  const intPart = intRaw.replace(/[^0-9-]/g, '') || '0'
  const decPart = decRaw.replace(/\D/g, '')
  return decPart ? `${intPart}.${decPart}` : intPart
}

function numericToDisplay(
  numeric: string,
  opts: {
    decimalPlaces: number
    decimalCharacter: string
    digitGroupSeparator: string
    currencySymbol: string | undefined
  },
): string {
  if (!numeric) return ''
  const isNeg = numeric.startsWith('-')
  const abs = isNeg ? numeric.substring(1) : numeric
  const dotIdx = abs.indexOf('.')
  const intRaw = dotIdx >= 0 ? abs.substring(0, dotIdx) : abs
  const decRaw = dotIdx >= 0 ? abs.substring(dotIdx + 1) : ''
  const paddedDec =
    opts.decimalPlaces > 0
      ? (decRaw + '0'.repeat(opts.decimalPlaces)).substring(0, opts.decimalPlaces)
      : ''
  // O(n) loop instead of a lookahead regex (ReDoS, Sonar S5852).
  const digits = intRaw || '0'
  let groupedInt = ''
  for (let i = 0; i < digits.length; i++) {
    if (i > 0 && (digits.length - i) % 3 === 0) groupedInt += opts.digitGroupSeparator
    groupedInt += digits[i]
  }
  const sign = isNeg ? '-' : ''
  const decimal = paddedDec ? opts.decimalCharacter + paddedDec : ''
  return (opts.currencySymbol ?? '') + sign + groupedInt + decimal
}

const isDigit = (k: string) => /^\d$/.test(k)
const isSign = (k: string) => k === '+' || k === '-'
const isDelete = (k: string) => k === 'Backspace' || k === 'Delete'

interface KeyCtx {
  el: HTMLInputElement
  a: AutoNumeric
  anRef: { current: AutoNumeric | null }
  decCh: string
  decimalPlaces: number
  currencySymbol: string | undefined
  replaceNextDigitAt: { current: number | null }
  pendingCursor: { current: { pos: number; key: string } | null }
  keySeq: { current: number }
}

// Caret moves are deferred a frame so they land after AutoNumeric's own. When keys arrive faster
// than frames, a move computed for an earlier key must not land after a later one (it would put
// "9" in front of "8": 1,234,567.98). Each keydown bumps the sequence; stale moves are dropped.
function afterFrame(
  el: HTMLInputElement,
  anRef: { current: AutoNumeric | null },
  keySeq: { current: number },
  move: () => void,
) {
  const seq = keySeq.current
  requestAnimationFrame(() => {
    if (!anRef.current || keySeq.current !== seq) return
    if (document.activeElement !== el) return
    move()
  })
}

// Module-level to keep call sites shallow (Sonar S2004).
function queueCursor(
  el: HTMLInputElement,
  anRef: { current: AutoNumeric | null },
  keySeq: { current: number },
  computePos: () => number | null,
) {
  afterFrame(el, anRef, keySeq, () => {
    const pos = computePos()
    if (pos != null) el.setSelectionRange(pos, pos)
  })
}

// Stops AutoNumeric from turning "0" + digit into "05".
function tryLeadingZeroReplace(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, decCh } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if (!isDigit(e.key) || allSelected || start !== end) return false
  const dec = v.indexOf(decCh)
  const intEnd = dec >= 0 ? dec : v.length
  if (start !== intEnd) return false
  const intStr = v.substring(0, intEnd).replace(/[^0-9-]/g, '')
  if (intStr !== '0') return false
  e.preventDefault()
  e.stopImmediatePropagation()
  const zeroPos = v.substring(0, intEnd).search(/0/)
  if (zeroPos < 0) return true
  const newDisplay = v.substring(0, zeroPos) + e.key + v.substring(zeroPos + 1)
  a.set(displayToNumeric(newDisplay, decCh))
  const newDec = el.value.indexOf(decCh)
  const newIntEnd = newDec >= 0 ? newDec : el.value.length
  el.setSelectionRange(newIntEnd, newIntEnd)
  return true
}

function tryFreshZeroReplace(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, anRef, decCh, replaceNextDigitAt, keySeq } = ctx
  if (replaceNextDigitAt.current == null || !isDigit(e.key)) return false
  const v = el.value
  const pos = replaceNextDigitAt.current
  replaceNextDigitAt.current = null
  if (pos < 0 || pos >= v.length || !/\d/.test(v[pos])) return false
  e.preventDefault()
  e.stopImmediatePropagation()
  const newDisplay = v.substring(0, pos) + e.key + v.substring(pos + 1)
  a.set(displayToNumeric(newDisplay, decCh))
  const targetCaret = pos + 1
  afterFrame(el, anRef, keySeq, () => {
    const newDec = el.value.indexOf(decCh)
    const oldDec = v.indexOf(decCh)
    const oldIntLen = oldDec >= 0 ? oldDec : v.length
    const newIntLen = newDec >= 0 ? newDec : el.value.length
    const offset = pos < oldIntLen ? newIntLen - oldIntLen : 0
    const finalCaret = Math.min(targetCaret + offset, el.value.length)
    el.setSelectionRange(finalCaret, finalCaret)
  })
  return true
}

function tryAllSelectedDigitOrSign(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, decCh, decimalPlaces, currencySymbol } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if (!allSelected || (!isDigit(e.key) && !isSign(e.key))) return false
  e.preventDefault()
  e.stopImmediatePropagation()
  const digit = e.key === '+' || e.key === '-' ? e.key + '0' : e.key
  const padded = decimalPlaces > 0 ? `${digit}.${'0'.repeat(decimalPlaces)}` : digit
  const expectedDisplay =
    decimalPlaces > 0
      ? (currencySymbol ?? '') + digit + decCh + '0'.repeat(decimalPlaces)
      : (currencySymbol ?? '') + digit
  const caret = expectedDisplay.indexOf(decCh)
  const caretPos = caret >= 0 ? caret : expectedDisplay.length
  a.set(padded)
  // Some browsers hide trailing zeros while focused; write the padded display directly.
  if (el.value !== expectedDisplay) {
    el.value = expectedDisplay
  }
  el.setSelectionRange(caretPos, caretPos)
  return true
}

function tryDecimalSeparator(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, decCh } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if ((e.key !== decCh && e.key !== '.' && e.key !== ',') || allSelected) return false
  const dec = v.indexOf(decCh)
  if (dec === -1) return false
  e.preventDefault()
  e.stopImmediatePropagation()
  el.setSelectionRange(dec + 1, dec + 1)
  return true
}

function tryAllSelectedDelete(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, decCh } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if (!allSelected || !isDelete(e.key)) return false
  e.preventDefault()
  e.stopImmediatePropagation()
  a.set(0)
  const newV = el.value
  const newDec = newV.indexOf(decCh)
  const caret = newDec >= 0 ? newDec : newV.length
  el.setSelectionRange(caret, caret)
  return true
}

// Block the delete when already "0" so AutoNumeric leaves no "%" residue.
function backspaceIntegerPart(e: KeyboardEvent, v: string, oldDec: number): void {
  const intStr = v.substring(0, oldDec >= 0 ? oldDec : v.length).replace(/[^0-9-]/g, '')
  if (intStr === '0' || intStr === '') {
    e.preventDefault()
    e.stopImmediatePropagation()
  }
}

function tryBackspace(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, decCh, replaceNextDigitAt } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if (e.key !== 'Backspace' || allSelected) return false

  const oldDec = v.indexOf(decCh)
  const isInDecimal = oldDec >= 0 && start > oldDec

  if (!isInDecimal) {
    backspaceIntegerPart(e, v, oldDec)
    return true
  }

  if (start !== end) return true

  let prev = start - 1
  while (prev > oldDec && !/\d/.test(v[prev])) prev--
  if (prev <= oldDec) return true

  e.preventDefault()
  e.stopImmediatePropagation()
  const newDisplay = v.substring(0, prev) + '0' + v.substring(prev + 1)
  a.set(displayToNumeric(newDisplay, decCh))
  const newV = el.value
  const newDec = newV.indexOf(decCh)
  const intLenNew = newDec >= 0 ? newDec : newV.length
  const selPos = prev + (intLenNew - oldDec)
  if (selPos >= 0 && selPos < newV.length) {
    el.setSelectionRange(selPos, selPos)
    replaceNextDigitAt.current = selPos
  } else {
    el.setSelectionRange(prev, prev)
  }
  return true
}

function tryDelete(e: KeyboardEvent, ctx: KeyCtx): boolean {
  const { el, a, decCh, pendingCursor } = ctx
  const v = el.value
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const allSelected = v.length > 0 && end - start === v.length
  if (e.key !== 'Delete' || start !== end || allSelected) return false
  let next = start
  while (next < v.length && !/\d/.test(v[next])) next++
  if (next < v.length) {
    e.preventDefault()
    e.stopImmediatePropagation()
    const newDisplay = v.substring(0, next) + '0' + v.substring(next + 1)
    a.set(displayToNumeric(newDisplay, decCh))
    el.setSelectionRange(start, start)
    // Leave a flag so AutoNumeric's keyup does not move the caret.
    pendingCursor.current = { pos: start, key: 'Delete' }
  }
  return true
}

// Separators follow the UI language unless given; a language switch remounts the input.
export default function NumericInput(props: Readonly<NumericInputProps>) {
  const { i18n } = useTranslation()
  const sep = numberSeparators()
  const decimalCharacter = props.decimalCharacter ?? sep.decimal
  const digitGroupSeparator = props.digitGroupSeparator ?? sep.group
  const decimalCharacterAlternative =
    props.decimalCharacterAlternative ?? (decimalCharacter === ',' ? '.' : undefined)
  return (
    <NumericInputCore
      key={`${i18n.language}:${decimalCharacter}${digitGroupSeparator}`}
      {...props}
      decimalCharacter={decimalCharacter}
      digitGroupSeparator={digitGroupSeparator}
      decimalCharacterAlternative={decimalCharacterAlternative}
    />
  )
}

function NumericInputCore({
  value,
  onChange,
  decimalPlaces = 2,
  minimumValue,
  maximumValue,
  digitGroupSeparator = '.',
  decimalCharacter = ',',
  decimalCharacterAlternative,
  currencySymbol,
  padZeroOnEmpty = true,
  leadingZero = 'keep',
  textAlign = 'right',
  disabled,
  required,
  placeholder,
  className,
  id,
  name,
  autoFocus,
  ariaLabel,
}: Readonly<NumericInputProps>) {
  const inputRef = useRef<HTMLInputElement>(null)
  const anRef = useRef<AutoNumeric | null>(null)
  const onChangeRef = useRef(onChange)
  onChangeRef.current = onChange
  const initialRef = useRef(value)
  // Keeps AutoNumeric's keyup from overriding the caret set in keydown.
  const pendingCursorRef = useRef<{ pos: number; key: string } | null>(null)
  const replaceNextDigitAtRef = useRef<number | null>(null)
  const keySeqRef = useRef(0)
  const decRef = useRef(decimalCharacter)
  decRef.current = decimalCharacter

  useEffect(() => {
    const el = inputRef.current
    if (!el) return
    const initial = initialRef.current == null ? '' : String(initialRef.current)

    const opts: Record<string, unknown> = {
      decimalPlaces,
      decimalPlacesRawValue: decimalPlaces,
      decimalPlacesShownOnFocus: decimalPlaces,
      decimalPlacesShownOnBlur: decimalPlaces,
      digitGroupSeparator,
      decimalCharacter,
      leadingZero,
      allowDecimalPadding: true,
      emptyInputBehavior: padZeroOnEmpty ? 'zero' : 'null',
      watchExternalChanges: false,
    }
    if (decimalCharacterAlternative) opts.decimalCharacterAlternative = decimalCharacterAlternative
    if (minimumValue !== undefined) opts.minimumValue = minimumValue
    if (maximumValue !== undefined) opts.maximumValue = maximumValue
    if (currencySymbol !== undefined && currencySymbol !== '') opts.currencySymbol = currencySymbol

    const an = new AutoNumeric(el, initial === '' ? null : initial, opts as never)
    anRef.current = an
    if (!padZeroOnEmpty) {
      const initialNum = initial === '' || initial == null ? 0 : Number.parseFloat(initial)
      if (initialNum === 0) {
        an.clear()
        el.value = ''
      }
    }

    // Only rewrite when padding is missing, otherwise valid typing updates would be clobbered.
    const enforceExpectedDisplay = () => {
      if (decimalPlaces === 0) return false
      if (!anRef.current) return false
      const v = el.value
      const decIdx = v.indexOf(decRef.current)
      if (decIdx >= 0) {
        const decPortion = v.substring(decIdx + 1)
        const decDigits = (decPortion.match(/\d/g) || []).length
        if (decDigits === decimalPlaces) return false
      }
      if (!padZeroOnEmpty && document.activeElement !== el) {
        const numDigits = (v.match(/\d/g) || []).length
        if (numDigits === 0) return false
      }
      // AutoNumeric may null the value after a delete; default to 0 to keep the display padded.
      let numeric = anRef.current.getNumericString()
      if (numeric == null || numeric === '') {
        if (document.activeElement !== el) return false
        numeric = '0'
      }
      const expected = numericToDisplay(numeric, {
        decimalPlaces,
        decimalCharacter,
        digitGroupSeparator,
        currencySymbol,
      })
      if (el.value !== expected) {
        const oldStart = el.selectionStart ?? expected.length
        const oldEnd = el.selectionEnd ?? oldStart
        el.value = expected
        const newStart = Math.min(oldStart, expected.length)
        const newEnd = Math.min(oldEnd, expected.length)
        el.setSelectionRange(newStart, newEnd)
        return true
      }
      return false
    }

    const handleFormatted = () => {
      const a = anRef.current
      if (!a) return
      onChangeRef.current(a.getNumericString() ?? '')
      enforceExpectedDisplay()
    }
    el.addEventListener('autoNumeric:formatted', handleFormatted)

    // Select synchronously: in rAF AutoNumeric's async focus handler can move the caret first.
    const handleFocus = () => {
      if (!el.value && anRef.current) {
        anRef.current.set(0)
      }
      if (el.value.length > 0) {
        el.setSelectionRange(0, el.value.length)
      }
    }
    el.addEventListener('focus', handleFocus)

    // AutoNumeric may reformat asynchronously on blur, so empty again on each tick.
    const handleBlur = () => {
      if (padZeroOnEmpty || !anRef.current) return
      const num = anRef.current.getNumber()
      if (num !== 0 && num !== null) return
      const clear = () => {
        if (!anRef.current) return
        if (document.activeElement === el) return
        const n = anRef.current.getNumber()
        if (n !== 0 && n !== null) return
        anRef.current.clear()
        if (el.value !== '') el.value = ''
      }
      clear()
      onChangeRef.current('')
      queueMicrotask(clear)
      requestAnimationFrame(clear)
      setTimeout(clear, 50)
      setTimeout(clear, 200)
    }
    el.addEventListener('blur', handleBlur)

    const makeCtx = (a: AutoNumeric): KeyCtx => ({
      el,
      a,
      anRef,
      decCh: decRef.current,
      decimalPlaces,
      currencySymbol,
      replaceNextDigitAt: replaceNextDigitAtRef,
      pendingCursor: pendingCursorRef,
      keySeq: keySeqRef,
    })

    const handleKeyDown = (e: KeyboardEvent) => {
      keySeqRef.current++
      const a = anRef.current
      if (!a) return
      const ctx = makeCtx(a)
      if (tryLeadingZeroReplace(e, ctx)) return
      if (tryFreshZeroReplace(e, ctx)) return
      if (replaceNextDigitAtRef.current != null && e.key !== 'Backspace') {
        replaceNextDigitAtRef.current = null
      }
      if (tryAllSelectedDigitOrSign(e, ctx)) return
      if (tryDecimalSeparator(e, ctx)) return
      if (tryAllSelectedDelete(e, ctx)) return
      if (tryBackspace(e, ctx)) return
      tryDelete(e, ctx)
    }
    // capture: true → run before AutoNumeric's keydown listener.
    el.addEventListener('keydown', handleKeyDown, true)

    const handleKeyUp = (e: KeyboardEvent) => {
      const a = anRef.current
      if (!a) return

      if (pendingCursorRef.current) {
        const { pos } = pendingCursorRef.current
        pendingCursorRef.current = null
        afterFrame(el, anRef, keySeqRef, () => el.setSelectionRange(pos, pos))
        // Also set synchronously so it holds without waiting for rAF.
        el.setSelectionRange(pos, pos)
        return
      }

      if (isDigit(e.key) || isSign(e.key)) {
        enforceExpectedDisplay()
        queueCursor(el, anRef, keySeqRef, () => {
          const v = el.value
          const start = el.selectionStart ?? 0
          const end = el.selectionEnd ?? 0
          const dec = v.indexOf(decRef.current)
          if (dec === -1) return v.length
          if (start < dec || start !== end) return dec
          return null
        })
      } else if (e.key === decimalCharacter || e.key === '.' || e.key === ',') {
        queueCursor(el, anRef, keySeqRef, () => {
          const dec = el.value.indexOf(decRef.current)
          return dec === -1 ? null : dec + 1
        })
      } else if (e.key === 'Backspace' || e.key === 'Delete') {
        enforceExpectedDisplay()
      }
    }
    el.addEventListener('keyup', handleKeyUp)

    return () => {
      el.removeEventListener('autoNumeric:formatted', handleFormatted)
      el.removeEventListener('focus', handleFocus)
      el.removeEventListener('blur', handleBlur)
      el.removeEventListener('keydown', handleKeyDown, true)
      el.removeEventListener('keyup', handleKeyUp)
      an.remove()
      anRef.current = null
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <input
      ref={inputRef}
      type="text"
      inputMode="decimal"
      id={id}
      name={name}
      disabled={disabled}
      required={required}
      placeholder={placeholder}
      autoFocus={autoFocus}
      aria-label={ariaLabel}
      style={{ textAlign }}
      className={
        className ??
        'w-full rounded-lg border bg-background px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-ring'
      }
    />
  )
}
