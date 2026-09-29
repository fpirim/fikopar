import katalog from 'app/entities/katalog/katalog.reducer';
import stok from 'app/entities/stok/stok.reducer';
import cari from 'app/entities/cari/cari.reducer';
import caritoplam from 'app/entities/cari-toplam/cari-toplam.reducer';
import sicil from 'app/entities/sicil/sicil.reducer';
import hareketSicil from 'app/entities/hareket-sicil/hareket-sicil.reducer';
import cariDevir from 'app/entities/cari-devir/cari-devir.reducer';
import cekSenet from 'app/entities/cek-senet/cek-senet.reducer';
import makbuz from 'app/entities/makbuz/makbuz.reducer';
/* jhipster-needle-add-reducer-import - JHipster will add reducer here */

const entitiesReducers = {
  katalog,
  stok,
  caritoplam,
  cari,
  sicil,
  hareketSicil,
  cariDevir,
  cekSenet,
  makbuz,
  /* jhipster-needle-add-reducer-combine - JHipster will add reducer here */
};

export default entitiesReducers;
