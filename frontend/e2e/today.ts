// Dates relative to the day the specs run on. MSW specs freeze the browser on E2E_TODAY (default: the day the mock
// data was cut); real specs follow the backend, which dates by today in Europe/Istanbul.
export const mswToday = process.env.E2E_TODAY ?? '2026-09-29'

export function istanbulToday(): string {
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'Europe/Istanbul' }).format(new Date())
}

export function addDays(iso: string, days: number): string {
  const d = new Date(`${iso}T00:00:00Z`)
  d.setUTCDate(d.getUTCDate() + days)
  return d.toISOString().slice(0, 10)
}

// yyyy-mm-dd as the UI shows it (dd.mm.yyyy).
export function shown(iso: string): string {
  const [y, m, d] = iso.split('-')
  return `${d}.${m}.${y}`
}
