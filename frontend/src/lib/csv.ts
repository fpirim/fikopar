import { numberSeparators } from './format'

export interface CsvColumn<T> {
  header: string
  value: (row: T) => string
}

const cell = (v: string, sep: string) =>
  /["\r\n]/.test(v) || v.includes(sep) ? `"${v.replace(/"/g, '""')}"` : v

// TR spreadsheets expect ';' because ',' is the decimal mark there.
export function toCsv<T>(rows: T[], columns: CsvColumn<T>[]): string {
  const sep = numberSeparators().decimal === ',' ? ';' : ','
  const line = (cells: string[]) => cells.map((c) => cell(c, sep)).join(sep)
  const out = [line(columns.map((c) => c.header)), ...rows.map((r) => line(columns.map((c) => c.value(r))))]
  return `﻿${out.join('\r\n')}\r\n`
}

export function saveBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

export function saveCsv<T>(rows: T[], columns: CsvColumn<T>[], filename: string): void {
  saveBlob(new Blob([toCsv(rows, columns)], { type: 'text/csv;charset=utf-8' }), filename)
}
