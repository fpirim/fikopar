import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import HareketSicil from './hareket-sicil';

const HareketSicilRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<HareketSicil />} />
  </ErrorBoundaryRoutes>
);

export default HareketSicilRoutes;
