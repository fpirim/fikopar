import React, { useEffect } from 'react';
import {
  CacheMetrics,
  DatasourceMetrics,
  EndpointsRequestsMetrics,
  GarbageCollectorMetrics,
  HttpRequestMetrics,
  JvmMemory,
  JvmThreads,
  SystemMetrics,
  translate,
} from 'react-jhipster';

import { APP_TIMESTAMP_FORMAT, APP_TWO_DIGITS_AFTER_POINT_NUMBER_FORMAT, APP_WHOLE_NUMBER_FORMAT } from 'app/config/constants';
import { getSystemMetrics, getSystemThreadDump } from '../administration.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { Button, Container, Grid, Typography } from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';

export const MetricsPage = () => {
  const dispatch = useAppDispatch();
  const metrics = useAppSelector(state => state.administration.metrics);
  const isFetching = useAppSelector(state => state.administration.loading);
  const threadDump = useAppSelector(state => state.administration.threadDump);

  useEffect(() => {
    dispatch(getSystemMetrics());
    dispatch(getSystemThreadDump());
  }, []);

  const getMetrics = () => {
    if (!isFetching) {
      dispatch(getSystemMetrics());
      dispatch(getSystemThreadDump());
    }
  };

  return (
    <Container
      maxWidth="xl"
      sx={{
        marginTop: '2rem',
      }}
    >
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Typography variant="h5">{translate('metrics.title')}</Typography>
        </Grid>
        <Grid item sm={12}>
          <Button
            variant="contained"
            startIcon={<RefreshIcon />}
            color={isFetching ? 'secondary' : 'primary'}
            disabled={isFetching}
            onClick={getMetrics}
          >
            {translate('health.refresh.button')}
          </Button>
        </Grid>
        <hr />
        <Grid container spacing={2}>
          <Grid item sm={12}>
            <Typography variant="h6">{translate('metrics.jvm.title')}</Typography>
            <Grid container spacing={2}>
              <Grid item md={4}>
                {metrics?.jvm ? <JvmMemory jvmMetrics={metrics.jvm} wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT} /> : ''}
              </Grid>
              <Grid item md={4}>
                {threadDump ? <JvmThreads jvmThreads={threadDump} wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT} /> : ''}
              </Grid>
              <Grid item md={4}>
                {metrics?.processMetrics ? (
                  <SystemMetrics
                    systemMetrics={metrics.processMetrics}
                    wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT}
                    timestampFormat={APP_TIMESTAMP_FORMAT}
                  />
                ) : (
                  ''
                )}
              </Grid>
            </Grid>
          </Grid>
        </Grid>

        {metrics?.garbageCollector ? (
          <GarbageCollectorMetrics garbageCollectorMetrics={metrics.garbageCollector} wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT} />
        ) : (
          ''
        )}
        {metrics && metrics['http.server.requests'] ? (
          <HttpRequestMetrics
            requestMetrics={metrics['http.server.requests']}
            twoDigitAfterPointFormat={APP_TWO_DIGITS_AFTER_POINT_NUMBER_FORMAT}
            wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT}
          />
        ) : (
          ''
        )}
        {metrics?.services ? (
          <EndpointsRequestsMetrics endpointsRequestsMetrics={metrics.services} wholeNumberFormat={APP_WHOLE_NUMBER_FORMAT} />
        ) : (
          ''
        )}

        {metrics?.cache ? (
          <Grid container spacing={2}>
            <Grid item sm={12}>
              <CacheMetrics cacheMetrics={metrics.cache} twoDigitAfterPointFormat={APP_TWO_DIGITS_AFTER_POINT_NUMBER_FORMAT} />
            </Grid>
          </Grid>
        ) : (
          ''
        )}

        {metrics?.databases && JSON.stringify(metrics.databases) !== '{}' ? (
          <Grid container spacing={2}>
            <Grid item sm={12}>
              <DatasourceMetrics
                datasourceMetrics={metrics.databases}
                twoDigitAfterPointFormat={APP_TWO_DIGITS_AFTER_POINT_NUMBER_FORMAT}
              />
            </Grid>
          </Grid>
        ) : (
          ''
        )}
      </Grid>
    </Container>
  );
};

export default MetricsPage;
