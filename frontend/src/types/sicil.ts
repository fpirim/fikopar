// Mirrors backend models.SicilTipi / models.TelefonTipi.
export type SicilTipi = 'FIRMA' | 'MUSTERI' | 'BANKA'

export const SICIL_TIPLERI: SicilTipi[] = ['FIRMA', 'MUSTERI', 'BANKA']

export type TelefonTipi = 'CEP' | 'EV' | 'IS' | 'FAKS' | 'DIGER'

export const TELEFON_TIPLERI: TelefonTipi[] = ['CEP', 'EV', 'IS', 'FAKS', 'DIGER']

export interface SicilTelefon {
  id?: number
  tipi: TelefonTipi
  numara: string
}
