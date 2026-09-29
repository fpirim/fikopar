import React from 'react';
import { translate, Translate } from 'react-jhipster';

import { useAppSelector } from 'app/config/store';
import { GridColDef } from '@mui/x-data-grid-pro';
import DataGrid from 'app/shared/core-components/datagrid';
import { Container, Grid, Typography } from '@mui/material';

export const TrackerPage = () => {
  const activities = useAppSelector(state => state.administration.tracker.activities);

  const columns: GridColDef[] = [
    { field: 'userLogin', headerName: translate('tracker.table.userlogin'), flex: 1 },
    { field: 'ipAddress', headerName: translate('tracker.table.ipaddress'), flex: 1 },
    { field: 'page', headerName: translate('tracker.table.page'), flex: 1 },
    { field: 'time', headerName: translate('tracker.table.time'), flex: 1 },
  ];

  const rows = activities.map((activity, i) => ({
    id: i,
    userLogin: activity.userLogin,
    ipAddress: activity.ipAddress,
    page: activity.page,
    time: activity.time,
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
            <Translate contentKey="tracker.title">Health Checks</Translate>
          </Typography>
        </Grid>
        <Grid item sm={12}>
          <DataGrid
            columns={columns}
            rows={rows}
            disableColumnFilter
            disableColumnMenu
            disableRowSelectionOnClick
            disableCellFocusOutline
            hideFooter
          />
        </Grid>
      </Grid>
    </Container>
  );
};

export default TrackerPage;
