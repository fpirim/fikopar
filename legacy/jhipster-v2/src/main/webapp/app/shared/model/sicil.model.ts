import { ICari } from 'app/shared/model/cari.model';
import { SicilTipi } from 'app/shared/model/enumerations/sicil-tipi.model';

export interface ISicil {
  id?: number;
  hesapno?: string | null;
  hesapadi?: string | null;
  adresi?: string | null;
  ilce?: string | null;
  sehir?: string | null;
  telefon?: string | null;
  vda?: string | null;
  vno?: string | null;
  tcno?: string | null;
  gun?: number | null;
  iskonto?: number | null;
  siciltipi?: SicilTipi | null;
  carises?: ICari[] | null;
}

export const defaultValue: Readonly<ISicil> = {
  hesapno: '',
  hesapadi: '',
  adresi: '',
  ilce: '',
  sehir: '',
  telefon: '',
  vda: '',
  vno: '',
  tcno: '',
  gun: 0,
  iskonto: 0,
  siciltipi: SicilTipi.MUSTERI,
};
