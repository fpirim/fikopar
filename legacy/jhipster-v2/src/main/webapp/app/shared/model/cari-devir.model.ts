import { ISicil } from 'app/shared/model/sicil.model';
import { ICari } from 'app/shared/model/cari.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import dayjs from 'dayjs';
import { EVRAK_DATE_FORMAT } from 'app/config/constants';

export interface ICariDevir {
  id?: number | null;
  depotipi?: DepoTipi | null;
  yil?: string | null;
  borc?: number | null;
  alacak?: number | null;
  sicil?: ISicil | null;
  cari?: ICari | null;
}

export const defaultValue: Readonly<ICariDevir> = {
  id: null,
  depotipi: DepoTipi.DEPO1,
  yil: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
  alacak: 0,
  sicil: null,
  cari: null,
};
