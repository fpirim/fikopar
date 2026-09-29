import { IHareketSicil } from 'app/shared/model/hareket-sicil.model';

export interface IHareketDetay {
  id?: number;
  stokno?: string | null;
  parcano?: string | null;
  tipi?: string | null;
  cinsi?: string | null;
  marka?: string | null;
  iskonto?: number | null;
  kdv?: number | null;
  adet?: number | null;
  fiyati?: number | null;
}

export const defaultValue: Readonly<IHareketDetay> = {
  id: null,
  stokno: '',
  parcano: '',
  tipi: '',
  cinsi: '',
  marka: '',
  iskonto: 0,
  kdv: 0,
  adet: 0,
  fiyati: 0,
};
