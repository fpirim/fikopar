import { ISicil } from 'app/shared/model/sicil.model';
import { ICari } from 'app/shared/model/cari.model';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import dayjs from 'dayjs';
import { EVRAK_DATE_FORMAT } from 'app/config/constants';

export interface IMakbuz {
  id?: number;
  makbuzno?: string | null;
  evraktipi?: EvrakTipi | null;
  depotipi?: DepoTipi | null;
  tarihi?: string | null;
  vade?: string | null;
  aciklama?: string | null;
  tutari?: number | null;
  sicil?: ISicil | null;
  cari?: ICari | null;
}

export const defaultValue: Readonly<IMakbuz> = {
  id: null,
  makbuzno: '',
  evraktipi: EvrakTipi.TAHSILAT_MAKBUZU,
  depotipi: DepoTipi.DEPO1,
  tarihi: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
  vade: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
  aciklama: '',
  tutari: 0,
  sicil: null,
  cari: null,
};
