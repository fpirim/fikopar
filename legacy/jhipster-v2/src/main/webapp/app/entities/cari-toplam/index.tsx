import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CariToplam from './cari-toplam';

const CariToplamRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CariToplam />} />
  </ErrorBoundaryRoutes>
);

export default CariToplamRoutes;
