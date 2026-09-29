import { ISicil } from 'app/shared/model/sicil.model';
import { ICekSenetDetay } from 'app/shared/model/cek-senet-detay.model';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import dayjs from 'dayjs';
import { EVRAK_DATE_FORMAT } from 'app/config/constants';

export interface ICekSenet {
  id?: number;
  ceksenetno?: string | null;
  evraktipi?: EvrakTipi | null;
  depotipi?: DepoTipi | null;
  tarihi?: string | null;
  vade?: string | null;
  aciklama?: string | null;
  tutar?: number | null;
  sicil?: ISicil | null;
  cekSenetDetays?: ICekSenetDetay[] | null;
}

export const defaultValue: Readonly<ICekSenet> = {
  id: null,
  ceksenetno: '',
  evraktipi: EvrakTipi.BORC_SENETI,
  depotipi: DepoTipi.DEPO1,
  tarihi: dayjs().format(EVRAK_DATE_FORMAT),
  vade: dayjs().format(EVRAK_DATE_FORMAT),
  aciklama: '',
  tutar: 0,
  sicil: null,
  cekSenetDetays: [],
};
