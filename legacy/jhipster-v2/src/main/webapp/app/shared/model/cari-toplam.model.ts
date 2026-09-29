import { ISicil } from 'app/shared/model/sicil.model';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';

export interface ICariToplam {
  sicil?: ISicil | null;
  borc?: number | null;
  alacak?: number | null;
}

export const defaultValue: Readonly<ICariToplam> = {};
