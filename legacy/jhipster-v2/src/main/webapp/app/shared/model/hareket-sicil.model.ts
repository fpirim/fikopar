import { ISicil } from 'app/shared/model/sicil.model';
import { ICari } from 'app/shared/model/cari.model';
import { IHareketDetay } from 'app/shared/model/hareket-detay.model';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import dayjs from 'dayjs';
import { EVRAK_DATE_FORMAT, EVRAK_HOUR_FORMAT } from 'app/config/constants';

export interface IHareketSicil {
  id?: number;
  evraktipi?: EvrakTipi | null;
  evrakno?: string | null;
  tarihi?: string | null;
  sevktarihi?: string | null;
  saati?: string | null;
  hesapno?: string | null;
  hesapadi?: string | null;
  adresi?: string | null;
  ilce?: string | null;
  sehir?: string | null;
  vda?: string | null;
  tcno?: string | null;
  vno?: string | null;
  telefon?: string | null;
  gun?: number | null;
  tutari?: number | null;
  notlar?: string | null;
  depotipi?: DepoTipi | null;
  toplam?: number | null;
  isktoplam?: number | null;
  kdvtoplam?: number | null;
  aratoplam?: number | null;
  sicil?: ISicil | null;
  cari?: ICari | null;
  hareketDetays?: IHareketDetay[] | null;
}

export const defaultValue: Readonly<IHareketSicil> = {
  id: null,
  evraktipi: EvrakTipi.IRSALIYELI_SATIS_FATURASI,
  evrakno: '',
  tarihi: dayjs().format(EVRAK_DATE_FORMAT),
  sevktarihi: dayjs().format(EVRAK_DATE_FORMAT),
  saati: dayjs().format(EVRAK_HOUR_FORMAT),
  hesapno: '',
  hesapadi: '',
  adresi: '',
  ilce: '',
  sehir: '',
  vda: '',
  tcno: '',
  vno: '',
  telefon: '',
  gun: 0,
  notlar: '',
  depotipi: DepoTipi.DEPO1,
  tutari: 0,
  isktoplam: 0,
  kdvtoplam: 0,
  aratoplam: 0,
  sicil: null,
  cari: null,
  hareketDetays: [],
};
