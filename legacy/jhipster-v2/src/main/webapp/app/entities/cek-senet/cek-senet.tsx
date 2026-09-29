import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { deleteEntities, getEntities } from './cek-senet.reducer';
import { useAppSelector } from 'app/config/store';
import { Box, Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { usePagination } from 'app/shared/hooks/use-pagination';
import { ICekSenet } from 'app/shared/model/cek-senet.model';
import { useDialogEdit } from 'app/shared/hooks/use-dialog-edit';
import EntityEdit from './cek-senet-update';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import DateCard from 'app/shared/components/date-card';
import { APP_LOCAL_DATE_FORMAT, EVRAK_DATE_FORMAT } from 'app/config/constants';
import { useForm } from 'react-hook-form';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import dayjs from 'dayjs';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import { getEntities as getSicils } from 'app/entities/sicil/sicil.reducer';
import { ISicil } from 'app/shared/model/sicil.model';

export const CekSenet = () => {
  const entityName = 'cekSenet';

  const [defaultSearchParams, setDefaultSearchParams] = useState<any>({
    sicil: {
      equals: null,
    },
    evraktipi: {
      equals: null,
    },
    depotipi: {
      equals: null,
    },
    tarihi: {
      greaterThanOrEqual: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
      lessThanOrEqual: dayjs().format(EVRAK_DATE_FORMAT),
    },
    vade: {
      greaterThanOrEqual: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
      lessThanOrEqual: dayjs().endOf('year').add(1, 'year').format(EVRAK_DATE_FORMAT),
    },
  });

  const { control, watch } = useForm<{
    sicil: {
      equals: ISicil | null;
    };
    evraktipi: {
      equals: EvrakTipi | null;
    };
    depotipi: {
      equals: DepoTipi | null;
    };
    tarihi: {
      greaterThanOrEqual: string;
      lessThanOrEqual: string;
    };
    vade: {
      greaterThanOrEqual: string;
      lessThanOrEqual: string;
    };
  }>({
    defaultValues: defaultSearchParams,
  });

  const depoTipiValues = Object.keys(DepoTipi);
  const evrakTipiValues = [EvrakTipi.BORC_SENETI, EvrakTipi.BORC_CEKI, EvrakTipi.ALACAK_SENETI, EvrakTipi.ALACAK_CEKI];

  useEffect(() => {
    const subscription = watch(data => {
      if (data) {
        const { sicil, ...rest } = data;
        setDefaultSearchParams({
          ...rest,
          ...(sicil && sicil.equals ? { sicilId: { equals: sicil.equals.id } } : {}),
        });
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

  const columns: GridColDef<ICekSenet>[] = [
    {
      field: 'id',
      headerName: translate(`fikoparApp.${entityName}.id`),
      flex: 0.2,
    },
    {
      field: 'ceksenetno',
      headerName: translate(`fikoparApp.${entityName}.ceksenetno`),
      flex: 0.7,
    },
    {
      field: 'tarihi',
      headerName: translate(`fikoparApp.${entityName}.tarihi`),
      flex: 0.5,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={APP_LOCAL_DATE_FORMAT} />,
    },
    {
      field: 'depotipi',
      headerName: translate(`fikoparApp.${entityName}.depotipi`),
      flex: 0.5,
      valueGetter: params => translate(`fikoparApp.DepoTipi.${params.row.depotipi}`),
    },
    {
      field: 'evraktipi',
      headerName: translate(`fikoparApp.${entityName}.evraktipi`),
      flex: 0.7,
      valueGetter: params => translate(`fikoparApp.EvrakTipi.${params.row.evraktipi}`),
    },
    {
      field: 'vade',
      headerName: translate(`fikoparApp.${entityName}.vade`),
      flex: 0.5,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={APP_LOCAL_DATE_FORMAT} />,
    },
    {
      field: 'hesapno',
      headerName: translate(`fikoparApp.sicil.hesapno`),
      flex: 0.7,
      valueGetter: params => params.row.sicil?.hesapno ?? '',
    },
    {
      field: 'hesapadi',
      headerName: translate(`fikoparApp.sicil.hesapadi`),
      flex: 1.2,
      valueGetter: params => params.row.sicil?.hesapadi ?? '',
    },
    {
      field: 'tutar',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.tutar`),
      flex: 0.7,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
  ];
  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item container spacing={2} sm={8}>
          <Grid item sm={9}>
            <CAutoComplete
              name="evraktipi.equals"
              control={control}
              fullWidth
              getOptionLabel={option => translate(`fikoparApp.EvrakTipi.${option}`)}
              options={evrakTipiValues}
              label={translate(`fikoparApp.${entityName}.evraktipi`)}
              size="small"
              disabled={loading}
            />
          </Grid>
          <Grid item sm={3}>
            <CAutoComplete
              name="depotipi.equals"
              control={control}
              fullWidth
              getOptionLabel={option => translate(`fikoparApp.DepoTipi.${option}`)}
              options={depoTipiValues}
              label={translate(`fikoparApp.${entityName}.depotipi`)}
              size="small"
              disabled={loading}
            />
          </Grid>
          <Grid item sm={12}>
            <Box
              sx={{
                display: 'flex',
                gap: 1,
              }}
            >
              <EntityAutoCompleteField
                name="sicil.equals"
                control={control}
                fullWidth
                getOptionLabel={EntityAutoCompleteLabelOptions.SicilLabelOption}
                renderOption={EntityAutoCompleteRenderOptions.SicilRenderOption}
                label={translate(`fikoparApp.sicil.detail.title`)}
                size="small"
                disabled={loading}
                entityName="sicil"
                fetchEntities={getSicils}
                searchParams={[
                  'hesapno.contains',
                  'hesapadi.contains',
                  'ilce.contains',
                  'sehir.contains',
                  'vda.contains',
                  'vno.contains',
                  'tcno.contains',
                ]}
              />
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
        <Grid item container spacing={2} sm={2}>
          <Grid item sm={12}>
            <CDatePicker
              name="vade.greaterThanOrEqual"
              label={translate(`entity.action.vade.baslangic`)}
              control={control}
              fullWidth
              size="small"
              disabled={loading}
            />
          </Grid>
          <Grid item sm={12}>
            <CDatePicker
              name="vade.lessThanOrEqual"
              label={translate(`entity.action.vade.bitis`)}
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

export default CekSenet;
