import React from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './katalog.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { IKatalog } from 'app/shared/model/katalog.model';
import EntityEdit from 'app/entities/katalog/katalog-update';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';

export const Katalog = () => {
  const entityName = 'katalog';

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);
  const updating = useAppSelector(state => state[entityName].updating);
  const deleting = useAppSelector(state => state[entityName].deleting);
  const deleteSuccess = useAppSelector(state => state[entityName].deleteSuccess);

  const { searchComponent, dataGridPaginationProps, handleSyncList } = usePagination({
    getEntities,
    loading: loading || deleting || updating,
    searchParams: ['katalogadi.contains'],
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

  const columns: GridColDef<IKatalog>[] = [
    { field: 'id', headerName: translate(`fikoparApp.${entityName}.id`), flex: 1 },
    { field: 'katalogadi', headerName: translate(`fikoparApp.${entityName}.katalogadi`), flex: 1 },
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

export default Katalog;
