import React from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './sicil.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { ISicil } from 'app/shared/model/sicil.model';
import EntityEdit from 'app/entities/sicil/sicil-update';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';

export const Sicil = () => {
  const entityName = 'sicil';

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);
  const updating = useAppSelector(state => state[entityName].updating);
  const deleting = useAppSelector(state => state[entityName].deleting);
  const deleteSuccess = useAppSelector(state => state[entityName].deleteSuccess);

  const { searchComponent, dataGridPaginationProps, handleSyncList } = usePagination({
    getEntities,
    loading: loading || deleting || updating,
    searchParams: ['hesapno.contains', 'hesapadi.contains', 'ilce.contains', 'sehir.contains', 'vda.contains', 'vno.contains'],
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

  const columns: GridColDef<ISicil>[] = [
    { field: 'id', headerName: translate(`fikoparApp.${entityName}.id`), flex: 1 },
    { field: 'hesapno', headerName: translate(`fikoparApp.${entityName}.hesapno`), flex: 1 },
    { field: 'hesapadi', headerName: translate(`fikoparApp.${entityName}.hesapadi`), flex: 1 },
    { field: 'ilce', headerName: translate(`fikoparApp.${entityName}.ilce`), flex: 1 },
    { field: 'sehir', headerName: translate(`fikoparApp.${entityName}.sehir`), flex: 1 },
    { field: 'vda', headerName: translate(`fikoparApp.${entityName}.vda`), flex: 1 },
    { field: 'vno', headerName: translate(`fikoparApp.${entityName}.vno`), flex: 1 },
    {
      field: 'siciltipi',
      headerName: translate(`fikoparApp.${entityName}.siciltipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.SicilTipi.${params.row.siciltipi}`),
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

export default Sicil;
