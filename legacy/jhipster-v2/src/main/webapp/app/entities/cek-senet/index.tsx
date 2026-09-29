import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import CekSenet from './cek-senet';
const CekSenetRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<CekSenet />} />
  </ErrorBoundaryRoutes>
);

export default CekSenetRoutes;
