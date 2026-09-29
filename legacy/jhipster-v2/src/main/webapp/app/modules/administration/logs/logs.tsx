import React, { useCallback, useDeferredValue, useEffect, useMemo, useState } from 'react';
import { translate } from 'react-jhipster';

import { changeLogLevel, getLoggers } from '../administration.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { Box, Button, Container, Grid, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from '@mui/material';

export const LogsPage = () => {
  const [filter, setFilter] = useState('');
  const logs = useAppSelector(state => state.administration.logs);
  const isFetching = useAppSelector(state => state.administration.loading);
  const dispatch = useAppDispatch();
  const deferredFilter = useDeferredValue(filter);

  useEffect(() => {
    dispatch(getLoggers());
  }, []);

  const changeLevel = useCallback((loggerName, level) => () => dispatch(changeLogLevel(loggerName, level)), []);

  const changeFilter = useCallback(evt => setFilter(evt.target.value), []);

  const loggers = useMemo(
    () =>
      logs
        ? Object.entries(logs.loggers).map((e: any) => ({
            name: e[0],
            level: e[1].effectiveLevel,
          }))
        : [],
    [logs]
  );

  const list = useMemo(() => loggers.filter(l => l.name.toUpperCase().includes(filter.toUpperCase())), [loggers, deferredFilter]);

  return (
    <Container
      maxWidth="xl"
      sx={{
        marginTop: '2rem',
      }}
    >
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Typography variant="h5">{translate('logs.title')}</Typography>
        </Grid>
        <Grid item sm={12}>
          <Typography variant="h6">{translate('logs.nbloggers', { total: loggers.length })}</Typography>
        </Grid>
        <Grid item sm={12}>
          <TextField label={translate('logs.filter')} size="small" fullWidth value={filter} disabled={isFetching} onChange={changeFilter} />
        </Grid>
        <Grid item sm={12}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell component="th" scope="row">
                  {translate('logs.table.name')}
                </TableCell>
                <TableCell component="th" scope="row">
                  {translate('logs.table.level')}
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {list.map((logger, i) => (
                <TableRow key={`log-row-${i}`}>
                  <TableCell sx={{ width: '100%' }}>
                    <Typography variant="body2" sx={{ overflowWrap: 'anywhere' }}>
                      {logger.name}
                    </Typography>
                  </TableCell>
                  <TableCell>
                    <Box display="flex" justifyContent="flex-start" flexWrap="nowrap" gap={1}>
                      <Button
                        variant={logger.level === 'TRACE' ? 'contained' : 'outlined'}
                        size="small"
                        color="primary"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'TRACE')}
                      >
                        TRACE
                      </Button>
                      <Button
                        variant={logger.level === 'DEBUG' ? 'contained' : 'outlined'}
                        color="success"
                        size="small"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'DEBUG')}
                      >
                        DEBUG
                      </Button>
                      <Button
                        variant={logger.level === 'INFO' ? 'contained' : 'outlined'}
                        size="small"
                        color="info"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'INFO')}
                      >
                        INFO
                      </Button>
                      <Button
                        variant={logger.level === 'WARN' ? 'contained' : 'outlined'}
                        color="warning"
                        size="small"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'WARN')}
                      >
                        WARN
                      </Button>
                      <Button
                        variant={logger.level === 'ERROR' ? 'contained' : 'outlined'}
                        color="error"
                        size="small"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'ERROR')}
                      >
                        ERROR
                      </Button>
                      <Button
                        variant={logger.level === 'OFF' ? 'contained' : 'outlined'}
                        color="secondary"
                        size="small"
                        disabled={isFetching}
                        onClick={changeLevel(logger.name, 'OFF')}
                      >
                        OFF
                      </Button>
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Grid>
      </Grid>
    </Container>
  );
};

export default LogsPage;
