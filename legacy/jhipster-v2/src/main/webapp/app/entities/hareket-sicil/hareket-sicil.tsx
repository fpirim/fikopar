import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './hareket-sicil.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { IHareketSicil } from 'app/shared/model/hareket-sicil.model';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';
import EntityEdit from './hareket-sicil-update';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import DateCard from 'app/shared/components/date-card';
import { APP_LOCAL_DATE_FORMAT, EVRAK_DATE_FORMAT } from 'app/config/constants';
import { useForm } from 'react-hook-form';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import dayjs from 'dayjs';

export const HareketSicil = () => {
  const entityName = 'hareketSicil';

  const [defaultSearchParams, setDefaultSearchParams] = useState<any>({
    evraktipi: {
      equals: EvrakTipi.IRSALIYELI_SATIS_FATURASI,
    },
    tarihi: {
      greaterThanOrEqual: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
      lessThanOrEqual: dayjs().format(EVRAK_DATE_FORMAT),
    },
  });

  const { control, watch } = useForm<{
    evraktipi: {
      equals: EvrakTipi;
    };
    tarihi: {
      greaterThanOrEqual: string;
      lessThanOrEqual: string;
    };
  }>({
    defaultValues: defaultSearchParams,
  });

  const evrakTipiValues = [
    EvrakTipi.IRSALIYELI_SATIS_FATURASI,
    EvrakTipi.SATIS_FATURASI,
    EvrakTipi.SATIS_IRSALIYESI,
    EvrakTipi.IRSALIYELI_ALIS_FATURASI,
    EvrakTipi.ALIS_FATURASI,
    EvrakTipi.ALIS_IRSALIYESI,
  ];

  useEffect(() => {
    const subscription = watch(data => {
      if (data) {
        setDefaultSearchParams(data);
      }
    });
    return subscription.unsubscribe;
  }, [watch]);

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
      'evrakno.contains',
      'hesapno.contains',
      'hesapadi.contains',
      'ilce.contains',
      'sehir.contains',
      'vda.contains',
      'vno.contains',
      'tcno.contains',
    ],
    defaultParams: defaultSearchParams,
    defaultSortBy: 'tarihi',
    defaultSortDir: 'asc',
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

  const columns: GridColDef<IHareketSicil>[] = [
    { field: 'id', headerName: translate(`fikoparApp.${entityName}.id`), flex: 1 },
    {
      field: 'tarihi',
      headerName: translate(`fikoparApp.${entityName}.tarihi`),
      flex: 1,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={APP_LOCAL_DATE_FORMAT} />,
    },
    {
      field: 'evraktipi',
      headerName: translate(`fikoparApp.${entityName}.evraktipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.EvrakTipi.${params.row.evraktipi}`),
    },
    {
      field: 'depotipi',
      headerName: translate(`fikoparApp.${entityName}.depotipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.DepoTipi.${params.row.depotipi}`),
    },
    {
      field: 'evrakno',
      headerName: translate(`fikoparApp.${entityName}.evrakno`),
      flex: 1,
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
      field: 'tutari',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.tutari`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
  ];
  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item container spacing={2} sm={10}>
          <Grid item sm={12}>
            <CAutoComplete
              name="evraktipi.equals"
              control={control}
              fullWidth
              getOptionLabel={option => translate(`fikoparApp.EvrakTipi.${option}`)}
              options={evrakTipiValues}
              label={translate(`fikoparApp.${entityName}.evraktipi`)}
              size="small"
              disabled={loading}
              disableClearable
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
        </Grid>
        <Grid item container spacing={2} sm={2}>
          <Grid item sm={12}>
            <CDatePicker
              name="tarihi.greaterThanOrEqual"
              label={translate(`entity.action.tarih.baslangic`)}
              control={control}
              fullWidth
              size="small"
              disabled={loading}
            />
          </Grid>
          <Grid item sm={12}>
            <CDatePicker
              name="tarihi.lessThanOrEqual"
              label={translate(`entity.action.tarih.bitis`)}
              control={control}
              fullWidth
              size="small"
              disabled={loading}
            />
          </Grid>
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

export default HareketSicil;
