import { ISicil } from 'app/shared/model/sicil.model';
import { IMakbuz } from 'app/shared/model/makbuz.model';
import { ICekSenet } from 'app/shared/model/cek-senet.model';
import { IslemTipi } from 'app/shared/model/enumerations/islem-tipi.model';

export interface ICekSenetDetay {
  id?: number;
  islemtipi?: IslemTipi | null;
  tarihi?: string | null;
  tutar?: number | null;
  sicil?: ISicil | null;
  makbuz?: IMakbuz | null;
  cekSenet?: ICekSenet | null;
}

export const defaultValue: Readonly<ICekSenetDetay> = {};
