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

export interface Sicil {
  id: number
  organization_id: number
  hesap_no: string
  hesap_adi: string
  adres: string
  ilce: string
  sehir: string
  v_dairesi: string
  v_no: string
  tc_no: string
  gun: string
  iskonto: number
  sicil_tipi: SicilTipi
  telefonlar: SicilTelefon[]
  created_at: string
  updated_at: string
}
