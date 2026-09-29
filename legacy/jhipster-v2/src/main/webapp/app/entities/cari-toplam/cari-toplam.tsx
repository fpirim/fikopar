import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { getEntities, getExcelReport } from './cari-toplam.reducer';
import { useAppSelector } from 'app/config/store';
import { Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { ICariToplam } from 'app/shared/model/cari-toplam.model';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import { EVRAK_DATE_FORMAT } from 'app/config/constants';
import { useForm } from 'react-hook-form';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import dayjs from 'dayjs';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import { useList } from 'app/shared/hooks/use-list';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';
import CTextField from 'app/shared/core-components/core-textfield';

export const CariToplam = () => {
  const entityName = 'caritoplam';

  const depoTipiValues = Object.keys(DepoTipi);

  const [defaultSearchParams, setDefaultSearchParams] = useState<any>({
    filter: {
      contains: null,
    },
    depotipi: {
      equals: null,
    },
    tarih: {
      greaterThanOrEqual: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
      lessThanOrEqual: dayjs().endOf('year').format(EVRAK_DATE_FORMAT),
    },
  });

  const { control, watch, setValue } = useForm<{
    toplamlar: {
      borc: number;
      alacak: number;
      borcBakiye: number;
      alacakBakiye: number;
    };
    filter: {
      contains: string;
    };
    depotipi: {
      equals: DepoTipi;
    };
    tarih: {
      greaterThanOrEqual: string;
      lessThanOrEqual: string;
    };
  }>({
    defaultValues: {
      ...defaultSearchParams,
      toplamlar: {
        borc: 0,
        alacak: 0,
        borcBakiye: 0,
        alacakBakiye: 0,
      },
    },
  });

  useEffect(() => {
    const subscription = watch((data, { name }) => {
      if (name && name.startsWith('toplamlar')) {
        return;
      }
      if (data) {
        const { filter, toplamlar, ...rest } = data;
        setDefaultSearchParams({
          ...rest,
          filter: {
            contains: filter.contains ?? '',
          },
        });
      }
    });
    return subscription.unsubscribe;
  }, [watch]);

  const entities = useAppSelector(state => state[entityName].entities);
  const totalItems = useAppSelector(state => state[entityName].totalItems);
  const loading = useAppSelector(state => state[entityName].loading);

  useEffect(() => {
    const sums = entities.reduce(
      (acc, row) => ({
        borc: acc.borc + row.borc,
        alacak: acc.alacak + row.alacak,
      }),
      {
        borc: 0,
        alacak: 0,
        borcBakiye: 0,
        alacakBakiye: 0,
      }
    );

    setValue('toplamlar', {
      ...sums,
      borcBakiye: sums.borc > sums.alacak ? sums.borc - sums.alacak : 0,
      alacakBakiye: sums.alacak > sums.borc ? sums.alacak - sums.borc : 0,
    });
  }, [entities, setValue]);

  const { dataGridListProps, excelComponent, handleSyncList } = useList({
    getEntities,
    getExcelReport,
    loading,
    defaultParams: defaultSearchParams,
    defaultSortBy: 'tarih',
    defaultSortDir: 'asc',
  });

  const { dataGridActionProps } = useEntityAction({
    handleSyncList,
    loading,
    entityName,
  });

  const columns: GridColDef<ICariToplam>[] = [
    {
      field: 'hesapno',
      headerName: translate(`fikoparApp.sicil.hesapno`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'hesapadi',
      headerName: translate(`fikoparApp.sicil.hesapadi`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'ilce',
      headerName: translate(`fikoparApp.sicil.ilce`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'sehir',
      headerName: translate(`fikoparApp.sicil.sehir`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'borc',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.borc`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
      sortable: false,
    },
    {
      field: 'alacak',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.alacak`),
      flex: 1,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
      sortable: false,
    },
    {
      field: 'borcBakiye',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.borcBakiye`),
      flex: 1,
      valueGetter: params => (params.row.borc > params.row.alacak ? params.row.borc - params.row.alacak : 0),
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
      sortable: false,
    },
    {
      field: 'alacakBakiye',
      type: 'number',
      headerName: translate(`fikoparApp.${entityName}.alacakBakiye`),
      flex: 1,
      valueGetter: params => (params.row.alacak > params.row.borc ? params.row.alacak - params.row.borc : 0),
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
      sortable: false,
    },
  ];
  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item container spacing={2} sm={10}>
          <Grid item sm={12}>
            <CTextField
              name="filter.contains"
              control={control}
              label={translate(`fikoparApp.${entityName}.filter`)}
              size="small"
              fullWidth
            />
          </Grid>
          <Grid item sm={9}>
            {excelComponent}
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
          {/* <Grid item sm={12}>*/}
          {/*  <Box*/}
          {/*    sx={{*/}
          {/*      display: 'flex',*/}
          {/*      gap: 1,*/}
          {/*    }}*/}
          {/*  >*/}
          {/*    {searchComponent}*/}
          {/*    {actions}*/}
          {/*  </Box>*/}
          {/* </Grid>*/}
        </Grid>
        <Grid item container spacing={2} sm={2}>
          <Grid item sm={12}>
            <CDatePicker
              name="tarih.greaterThanOrEqual"
              label={translate(`entity.action.tarih.baslangic`)}
              control={control}
              fullWidth
              size="small"
              disabled={loading}
            />
          </Grid>
          <Grid item sm={12}>
            <CDatePicker
              name="tarih.lessThanOrEqual"
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
            hideFooter
            {...dataGridActionProps}
            {...dataGridListProps}
          />
        </Grid>
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.borc"
            label={translate(`fikoparApp.${entityName}.toplamlar.borc`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.alacak"
            label={translate(`fikoparApp.${entityName}.toplamlar.alacak`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.borcBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.borcBakiye`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.alacakBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.alacakBakiye`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
      </Grid>
    </Paper>
  );
};

export default CariToplam;
