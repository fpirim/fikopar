import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Katalog from './katalog';

const KatalogRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Katalog />} />
  </ErrorBoundaryRoutes>
);

export default KatalogRoutes;
