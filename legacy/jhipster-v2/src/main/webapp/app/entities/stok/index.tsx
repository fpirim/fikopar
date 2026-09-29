import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Stok from './stok';

const StokRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Stok />} />
  </ErrorBoundaryRoutes>
);

export default StokRoutes;
