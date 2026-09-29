import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Sicil from './sicil';

const SicilRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Sicil />} />
  </ErrorBoundaryRoutes>
);

export default SicilRoutes;
