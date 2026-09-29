import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CariDevir from './cari-devir';

const CariDevirRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CariDevir />} />
  </ErrorBoundaryRoutes>
);

export default CariDevirRoutes;
