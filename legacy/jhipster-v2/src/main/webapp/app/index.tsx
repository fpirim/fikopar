import React from 'react';
import '@fontsource/roboto/300.css';
import '@fontsource/roboto/400.css';
import '@fontsource/roboto/500.css';
import '@fontsource/roboto/700.css';
import { createRoot } from 'react-dom/client';
import { Provider } from 'react-redux';
import { bindActionCreators } from 'redux';

import getStore from 'app/config/store';
import { registerLocale } from 'app/config/translation';
import setupAxiosInterceptors from 'app/config/axios-interceptor';
import { clearAuthentication } from 'app/shared/reducers/authentication';
import ErrorBoundary from 'app/shared/error/error-boundary';
import AppComponent from 'app/app';
import { loadIcons } from 'app/config/icon-loader';
import { LicenseInfo } from '@mui/x-data-grid-pro';

const store = getStore();
registerLocale(store);

const actions = bindActionCreators({ clearAuthentication }, store.dispatch);
setupAxiosInterceptors(() => actions.clearAuthentication('login.error.unauthorized'));

loadIcons();

const rootEl = document.getElementById('root');
const root = createRoot(rootEl);

LicenseInfo.setLicenseKey('REDACTED');

Number.prototype.round = function (places) {
  return +(Math.round(Number(this + 'e+' + places)) + 'e-' + places);
};

const render = Component =>
  root.render(
    <ErrorBoundary>
      <Provider store={store}>
        <Component />
      </Provider>
    </ErrorBoundary>
  );

render(AppComponent);
