import React from 'react';
import { Route } from 'react-router-dom';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';
import UserManagement from './user-management';
import UserManagementUpdate from './user-management-update';

const UserManagementRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<UserManagement />} />
    <Route path="new" element={<UserManagementUpdate />} />
    <Route path=":login" element={<UserManagementUpdate />} />
  </ErrorBoundaryRoutes>
);

export default UserManagementRoutes;
