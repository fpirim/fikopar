import { ISicil } from 'app/shared/model/sicil.model';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';

export interface ICari {
  id?: number;
  tipi?: EvrakTipi | null;
  evrakno?: string | null;
  depotipi?: DepoTipi | null;
  tarih?: string | null;
  vade?: string | null;
  gun?: number | null;
  aciklama?: string | null;
  borc?: number | null;
  alacak?: number | null;
  sicil?: ISicil | null;
}

export const defaultValue: Readonly<ICari> = {};
