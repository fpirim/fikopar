import React, { useEffect, useState } from 'react';
import { translate, Translate } from 'react-jhipster';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import HealthModal from './health-modal';
import { getSystemHealth } from '../administration.reducer';
import { Button, Chip, Container, Grid, IconButton, Typography } from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import VisibilityIcon from '@mui/icons-material/Visibility';
import DataGrid from 'app/shared/core-components/datagrid';
import { GridColDef, GridValueGetterParams } from '@mui/x-data-grid-pro';

export const HealthPage = () => {
  const [healthObject, setHealthObject] = useState({});
  const [showModal, setShowModal] = useState(false);
  const dispatch = useAppDispatch();

  const health = useAppSelector(state => state.administration.health);
  const isFetching = useAppSelector(state => state.administration.loading);

  useEffect(() => {
    dispatch(getSystemHealth());
  }, []);

  const fetchSystemHealth = () => {
    if (!isFetching) {
      dispatch(getSystemHealth());
    }
  };

  const getSystemHealthInfo = (name, healthObj) => () => {
    setShowModal(true);
    setHealthObject({ ...healthObj, name });
  };

  const getBadgeType = (status: string) => (status !== 'UP' ? 'error' : 'success');

  const handleClose = () => setShowModal(false);

  const renderModal = () => <HealthModal healthObject={healthObject} handleClose={handleClose} showModal={showModal} />;

  const columns: GridColDef[] = [
    { field: 'service', headerName: translate('health.table.service'), flex: 1 },
    {
      field: 'status',
      headerName: translate('health.table.status'),
      renderCell: (params: GridValueGetterParams) => <Chip color={getBadgeType(params.value)} label={params.value} />,
      flex: 1,
    },
    {
      field: 'details',
      headerName: translate('health.details.details'),
      renderCell: (params: GridValueGetterParams) =>
        params.value ? (
          <IconButton onClick={getSystemHealthInfo(params.row.service, params.row)}>
            <VisibilityIcon />
          </IconButton>
        ) : null,
      flex: 1,
    },
  ];

  const data = (health || {}).components || {};
  const rows = Object.keys(data).map((configPropKey, configPropIndex) => ({
    id: configPropIndex,
    service: configPropKey,
    status: data[configPropKey].status,
    details: data[configPropKey].details,
  }));

  return (
    <Container
      maxWidth="xl"
      sx={{
        marginTop: '2rem',
      }}
    >
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Typography variant="h5">
            <Translate contentKey="health.title">Health Checks</Translate>
          </Typography>
        </Grid>
        <Grid item sm={12}>
          <Button
            variant="contained"
            startIcon={<RefreshIcon />}
            color={isFetching ? 'secondary' : 'primary'}
            disabled={isFetching}
            onClick={fetchSystemHealth}
          >
            {translate('health.refresh.button')}
          </Button>
        </Grid>
        <Grid item sm={12}>
          <DataGrid
            columns={columns}
            rows={rows}
            loading={isFetching}
            disableColumnFilter
            disableColumnMenu
            disableRowSelectionOnClick
            disableCellFocusOutline
            hideFooter
          />
        </Grid>
        {renderModal()}
      </Grid>
    </Container>
  );
};

export default HealthPage;
