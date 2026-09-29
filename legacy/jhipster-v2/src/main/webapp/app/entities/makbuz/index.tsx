import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Makbuz from './makbuz';

const MakbuzRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Makbuz />} />
  </ErrorBoundaryRoutes>
);

export default MakbuzRoutes;
