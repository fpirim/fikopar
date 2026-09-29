import { IKatalog } from 'app/shared/model/katalog.model';

export interface IStok {
  id?: number | null;
  stokno?: string | null;
  parcano?: string | null;
  tipi?: string | null;
  cinsi?: string | null;
  marka?: string | null;
  fiyati?: number | null;
  katalog?: IKatalog | null;
}

export const defaultValue: Readonly<IStok> = {
  id: null,
  stokno: '',
  parcano: '',
  tipi: '',
  cinsi: '',
  marka: '',
  fiyati: 0,
  katalog: null,
};
