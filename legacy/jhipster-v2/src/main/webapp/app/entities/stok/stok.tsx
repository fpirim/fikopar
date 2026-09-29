import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './stok.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { IStok } from 'app/shared/model/stok.model';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import { getEntities as getKatalogs } from 'app/entities/katalog/katalog.reducer';
import { useForm } from 'react-hook-form';
import { IKatalog } from 'app/shared/model/katalog.model';
import EntityEdit from 'app/entities/stok/stok-update';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';

export const Stok = () => {
  const { control, watch } = useForm<{ katalog: IKatalog }>();
  const [defaultSearchParams, setDefaultSearchParams] = useState<any>();

  useEffect(() => {
    const subscription = watch(data => {
      if (data && data.katalog) {
        setDefaultSearchParams({
          'katalogId.equals': data.katalog.id,
        });
      } else {
        setDefaultSearchParams(undefined);
      }
    });
    return subscription.unsubscribe;
  }, [watch]);

  const entityName = 'stok';

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);
  const updating = useAppSelector(state => state[entityName].updating);
  const deleting = useAppSelector(state => state[entityName].deleting);
  const deleteSuccess = useAppSelector(state => state[entityName].deleteSuccess);

  const { searchComponent, dataGridPaginationProps, handleSyncList } = usePagination({
    getEntities,
    loading: loading || deleting || updating,
    searchParams: ['stokno.contains', 'parcano.contains', 'tipi.contains', 'cinsi.contains', 'marka.contains'],
    defaultParams: defaultSearchParams,
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

  const columns: GridColDef<IStok>[] = [
    { field: 'id', headerName: translate(`fikoparApp.${entityName}.id`), flex: 1 },
    { field: 'stokno', headerName: translate(`fikoparApp.${entityName}.stokno`), flex: 1 },
    { field: 'parcano', headerName: translate(`fikoparApp.${entityName}.parcano`), flex: 1 },
    { field: 'tipi', headerName: translate(`fikoparApp.${entityName}.tipi`), flex: 1 },
    { field: 'cinsi', headerName: translate(`fikoparApp.${entityName}.cinsi`), flex: 1 },
    { field: 'marka', headerName: translate(`fikoparApp.${entityName}.marka`), flex: 1 },
    {
      field: 'fiyati',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.fiyati`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
    {
      field: 'katalog',
      headerName: translate(`fikoparApp.${entityName}.katalog`),
      flex: 1,
      valueGetter: params => (params.row.katalog ? params.row.katalog.katalogadi : ''),
    },
  ];

  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <EntityAutoCompleteField
            name="katalog"
            control={control}
            fullWidth
            getOptionLabel={EntityAutoCompleteLabelOptions.KatalogLabelOption}
            renderOption={EntityAutoCompleteRenderOptions.KatalogRenderOption}
            label={translate(`fikoparApp.katalog.detail.title`)}
            size="small"
            disabled={loading || deleting || updating}
            entityName="katalog"
            fetchEntities={getKatalogs}
            searchParams={['katalogadi.contains']}
          />
        </Grid>
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

export default Stok;
