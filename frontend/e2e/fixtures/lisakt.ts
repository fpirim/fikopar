// Synthetic LISAKT price list: 319-byte fixed-width records in CP857, CRLF line ends.
export interface LisaktRecord {
  op: '0' | '1' | '2'
  catalog: number
  stockNo: string
  name?: string
  brand?: string
  oem?: string
  // Price in kuruş precision, e.g. 2750 → 00000027500000 (scale 4).
  price: number
  // yyyymmdd; 00000000 takes the job date.
  validFrom?: string
}

const CP857: Record<string, number> = {
  Ç: 0x80,
  ü: 0x81,
  ç: 0x87,
  ı: 0x8d,
  ö: 0x94,
  İ: 0x98,
  Ö: 0x99,
  Ü: 0x9a,
  Ş: 0x9e,
  ş: 0x9f,
  Ğ: 0xa6,
  ğ: 0xa7,
}

const pad = (s: string, n: number) => {
  if ([...s].length > n) throw new Error(`${s} is longer than ${n}`)
  return s + ' '.repeat(n - [...s].length)
}
const num = (n: number, width: number) => String(n).padStart(width, '0')

function record(r: LisaktRecord): string {
  const rec =
    r.op +
    num(r.catalog, 4) +
    pad(r.stockNo, 15) +
    pad('', 30) +
    pad(r.oem ?? '', 30) +
    pad('', 30) +
    pad(r.name ?? '', 60) +
    pad(r.brand ?? '', 30) +
    '0'.repeat(32) +
    num(Math.round(r.price * 10000), 14) +
    pad('', 10) +
    (r.validFrom ?? '00000000') +
    '00001' +
    '0'.repeat(15) +
    '0000' +
    pad('', 15) +
    '0'.repeat(16)
  if ([...rec].length !== 319) throw new Error(`record is ${[...rec].length} bytes`)
  return rec
}

export function lisakt(records: LisaktRecord[]): Buffer {
  const text = records.map((r) => record(r) + '\r\n').join('')
  return Buffer.from(
    [...text].map((ch) => {
      const code = ch.codePointAt(0)!
      if (code < 0x80) return code
      const b = CP857[ch]
      if (b === undefined) throw new Error(`${ch} is not in the CP857 table`)
      return b
    }),
  )
}
