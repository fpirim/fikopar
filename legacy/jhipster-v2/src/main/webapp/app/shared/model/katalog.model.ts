import { IStok } from 'app/shared/model/stok.model';

export interface IKatalog {
  id?: number;
  katalogadi?: string | null;
  stoks?: IStok[] | null;
}

export const defaultValue: Readonly<IKatalog> = {
  katalogadi: '',
  stoks: null,
};
