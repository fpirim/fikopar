import React from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './cari-devir.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { ICariDevir } from 'app/shared/model/cari-devir.model';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';
import EntityEdit from 'app/entities/cari-devir/cari-devir-update';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import DateCard from 'app/shared/components/date-card';
import { EVRAK_DATE_FORMAT, ONLY_YEAR } from 'app/config/constants';

export const CariDevir = () => {
  const entityName = 'cariDevir';

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);
  const updating = useAppSelector(state => state[entityName].updating);
  const deleting = useAppSelector(state => state[entityName].deleting);
  const deleteSuccess = useAppSelector(state => state[entityName].deleteSuccess);

  const { searchComponent, dataGridPaginationProps, handleSyncList } = usePagination({
    getEntities,
    loading: loading || deleting || updating,
    searchParams: [
      'hesapno.contains',
      'hesapadi.contains',
      'ilce.contains',
      'sehir.contains',
      'vda.contains',
      'vno.contains',
      'tcno.contains',
    ],
  });

  const { handleClose, showModal, editId, handleEdit, dataGridEditProps } = useDialogEdit({ handleSyncList });

  const { actions, dataGridActionProps } = useEntityAction({
    handleSyncList,
    loading: loading || deleting || updating,
    handleNew: handleEdit,
    deleteEntities,
    deleteSuccess,
    entityName,
  });

  const columns: GridColDef<ICariDevir>[] = [
    { field: 'id', headerName: translate(`fikoparApp.${entityName}.id`), flex: 1 },
    {
      field: 'depotipi',
      headerName: translate(`fikoparApp.${entityName}.depotipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.DepoTipi.${params.row.depotipi}`),
    },
    {
      field: 'yil',
      headerName: translate(`fikoparApp.${entityName}.yil`),
      flex: 1,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={ONLY_YEAR} />,
    },
    {
      field: 'hesapno',
      headerName: translate(`fikoparApp.sicil.hesapno`),
      flex: 1,
      valueGetter: params => params.row.sicil?.hesapno ?? '',
    },
    {
      field: 'hesapadi',
      headerName: translate(`fikoparApp.sicil.hesapadi`),
      flex: 1,
      valueGetter: params => params.row.sicil?.hesapadi ?? '',
    },
    {
      field: 'borc',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.borc`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
    {
      field: 'alacak',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.alacak`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
  ];
  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Box
            sx={{
              display: 'flex',
              gap: 1,
            }}
          >
            {searchComponent}
            {actions}
          </Box>
        </Grid>
        <Grid item sm={12}>
          <DataGrid
            columns={columns}
            rows={entities}
            rowCount={totalItems}
            disableColumnFilter
            disableColumnMenu
            disableRowSelectionOnClick
            disableCellFocusOutline
            {...dataGridEditProps}
            {...dataGridActionProps}
            {...dataGridPaginationProps}
          />
        </Grid>
      </Grid>
      <EntityEdit showModal={showModal} handleClose={handleClose} id={editId} />
    </Paper>
  );
};

export default CariDevir;
