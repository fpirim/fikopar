import 'react-toastify/dist/ReactToastify.css';
import 'app/config/dayjs.ts';

import React, { useEffect } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { toast, ToastContainer } from 'react-toastify';
import { Storage } from 'react-jhipster';

import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getSession } from 'app/shared/reducers/authentication';
import { getProfile } from 'app/shared/reducers/application-profile';
import Header from 'app/shared/layout/header/header';
import Footer from 'app/shared/layout/footer/footer';
import { hasAnyAuthority } from 'app/shared/auth/private-route';
import ErrorBoundary from 'app/shared/error/error-boundary';
import { AUTHORITIES } from 'app/config/constants';
import AppRoutes from 'app/routes';
import { Box, createTheme, CssBaseline, ThemeProvider } from '@mui/material';

const baseHref = document.querySelector('base').getAttribute('href').replace(/\/$/, '');

export const ColorModeContext = React.createContext({
  toggleColorMode: () => {},
});
export const App = () => {
  const dispatch = useAppDispatch();

  useEffect(() => {
    dispatch(getSession());
    dispatch(getProfile());
  }, []);

  const currentLocale = useAppSelector(state => state.locale.currentLocale);
  const isAuthenticated = useAppSelector(state => state.authentication.isAuthenticated);
  const isAdmin = useAppSelector(state => hasAnyAuthority(state.authentication.account.authorities, [AUTHORITIES.ADMIN]));
  const ribbonEnv = useAppSelector(state => state.applicationProfile.ribbonEnv);
  const isInProduction = useAppSelector(state => state.applicationProfile.inProduction);
  const isOpenAPIEnabled = useAppSelector(state => state.applicationProfile.isOpenAPIEnabled);

  const [mode, setMode] = React.useState<'light' | 'dark'>(Storage.local.get('mode') || 'light');

  const colorMode = React.useMemo(
    () => ({
      toggleColorMode: () => {
        setMode(prevMode => {
          const newMode = prevMode === 'light' ? 'dark' : 'light';
          Storage.local.set('mode', newMode);
          return newMode;
        });
      },
    }),
    [mode]
  );

  const theme = React.useMemo(
    () =>
      createTheme({
        palette: {
          mode,
        },
      }),
    [mode]
  );

  const paddingTop = '60px';
  return (
    <BrowserRouter basename={baseHref}>
      <ColorModeContext.Provider value={colorMode}>
        <ThemeProvider theme={theme}>
          <Box
            sx={{
              display: 'flex',
            }}
          >
            <CssBaseline />
            <ToastContainer position={toast.POSITION.TOP_LEFT} className="toastify-container" toastClassName="toastify-toast" />
            <ErrorBoundary>
              <Header
                isAuthenticated={isAuthenticated}
                isAdmin={isAdmin}
                currentLocale={currentLocale}
                ribbonEnv={ribbonEnv}
                isInProduction={isInProduction}
                isOpenAPIEnabled={isOpenAPIEnabled}
              />
            </ErrorBoundary>
            <Box
              component="main"
              sx={{
                backgroundColor: theme => (theme.palette.mode === 'light' ? theme.palette.grey[100] : theme.palette.grey[900]),
                height: 'calc(100vh - 4.1em)',
                width: '100%',
                display: 'flex',
                overflow: 'auto',
                marginTop: '4.1em',
              }}
            >
              <Box sx={{ display: 'flex', flexDirection: 'column', width: '100%' }}>
                <ErrorBoundary>
                  <AppRoutes />
                  <Footer />
                </ErrorBoundary>
              </Box>
            </Box>
          </Box>
        </ThemeProvider>
      </ColorModeContext.Provider>
    </BrowserRouter>
  );
};

export default App;
