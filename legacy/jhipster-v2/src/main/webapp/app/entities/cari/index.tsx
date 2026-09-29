import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Cari from './cari';

const CariRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Cari />} />
  </ErrorBoundaryRoutes>
);

export default CariRoutes;
