import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Katalog from './katalog';
import Stok from './stok';
import Cari from './cari';
import CariToplam from './cari-toplam';
import Sicil from './sicil';
import HareketSicil from './hareket-sicil';
import CariDevir from './cari-devir';
import CekSenet from './cek-senet';
import Makbuz from './makbuz';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="katalog/*" element={<Katalog/>}/>
        <Route path="stok/*" element={<Stok />} />
        <Route path="caritoplam/*" element={<CariToplam />} />
        <Route path="cari/*" element={<Cari />} />
        <Route path="sicil/*" element={<Sicil />} />
        <Route path="hareket-sicil/*" element={<HareketSicil />} />
        <Route path="cari-devir/*" element={<CariDevir />} />
        <Route path="cek-senet/*" element={<CekSenet />} />
        <Route path="makbuz/*" element={<Makbuz />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
