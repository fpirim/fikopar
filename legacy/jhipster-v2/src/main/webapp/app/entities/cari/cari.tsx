import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { getEntities, getExcelReport } from './cari.reducer';
import { useAppSelector } from 'app/config/store';
import { Grid, Paper } from '@mui/material';
import { GridColDef } from '@mui/x-data-grid-pro';
import { useEntityAction } from 'app/shared/hooks/use-entity-action';
import DataGrid from 'app/shared/core-components/datagrid';
import { ICari } from 'app/shared/model/cari.model';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import DateCard from 'app/shared/components/date-card';
import { APP_LOCAL_DATE_FORMAT, EVRAK_DATE_FORMAT } from 'app/config/constants';
import { useForm } from 'react-hook-form';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import dayjs from 'dayjs';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import { ISicil } from 'app/shared/model/sicil.model';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import { getEntities as getSicils } from 'app/entities/sicil/sicil.reducer';
import { useList } from 'app/shared/hooks/use-list';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';

export const Cari = () => {
  const entityName = 'cari';

  const depoTipiValues = Object.keys(DepoTipi);

  const [defaultSearchParams, setDefaultSearchParams] = useState<any>({
    sicilId: {
      equals: -1,
    },
    depotipi: {
      equals: null,
    },
    tarih: {
      greaterThanOrEqual: dayjs().startOf('year').format(EVRAK_DATE_FORMAT),
      lessThanOrEqual: dayjs().format(EVRAK_DATE_FORMAT),
    },
  });

  const { control, watch, setValue } = useForm<{
    toplamlar: {
      borc: number;
      alacak: number;
      borcBakiye: number;
      alacakBakiye: number;
      depo1Borc: number;
      depo1Alacak: number;
      depo1BorcBakiye: number;
      depo1AlacakBakiye: number;
      depo2Borc: number;
      depo2Alacak: number;
      depo2BorcBakiye: number;
      depo2AlacakBakiye: number;
    };
    sicil: {
      equals: ISicil;
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
        depo1Borc: 0,
        depo1Alacak: 0,
        depo1BorcBakiye: 0,
        depo1AlacakBakiye: 0,
        depo2Borc: 0,
        depo2Alacak: 0,
        depo2BorcBakiye: 0,
        depo2AlacakBakiye: 0,
      },
    },
  });

  useEffect(() => {
    const subscription = watch((data, { name }) => {
      if (name && name.startsWith('toplamlar')) {
        return;
      }
      if (data) {
        const { sicil, toplamlar, ...rest } = data;
        setDefaultSearchParams({
          ...rest,
          sicilId: {
            equals: sicil.equals?.id ?? -1,
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
        depo1Borc: acc.depo1Borc + (row.depotipi === DepoTipi.DEPO1 ? row.borc : 0),
        depo1Alacak: acc.depo1Alacak + (row.depotipi === DepoTipi.DEPO1 ? row.alacak : 0),
        depo2Borc: acc.depo2Borc + (row.depotipi === DepoTipi.DEPO2 ? row.borc : 0),
        depo2Alacak: acc.depo2Alacak + (row.depotipi === DepoTipi.DEPO2 ? row.alacak : 0),
      }),
      {
        borc: 0,
        alacak: 0,
        borcBakiye: 0,
        alacakBakiye: 0,
        depo1Borc: 0,
        depo1Alacak: 0,
        depo1BorcBakiye: 0,
        depo1AlacakBakiye: 0,
        depo2Borc: 0,
        depo2Alacak: 0,
        depo2BorcBakiye: 0,
        depo2AlacakBakiye: 0,
      }
    );

    setValue('toplamlar', {
      ...sums,
      borcBakiye: sums.borc > sums.alacak ? sums.borc - sums.alacak : 0,
      alacakBakiye: sums.alacak > sums.borc ? sums.alacak - sums.borc : 0,
      depo1BorcBakiye: sums.depo1Borc > sums.depo1Alacak ? sums.depo1Borc - sums.depo1Alacak : 0,
      depo1AlacakBakiye: sums.depo1Alacak > sums.depo1Borc ? sums.depo1Alacak - sums.depo1Borc : 0,
      depo2BorcBakiye: sums.depo2Borc > sums.depo2Alacak ? sums.depo2Borc - sums.depo2Alacak : 0,
      depo2AlacakBakiye: sums.depo2Alacak > sums.depo2Borc ? sums.depo2Alacak - sums.depo2Borc : 0,
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

  const columns: GridColDef<ICari>[] = [
    {
      field: 'id',
      headerName: translate(`fikoparApp.${entityName}.id`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'tarih',
      headerName: translate(`fikoparApp.${entityName}.tarih`),
      flex: 1,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={APP_LOCAL_DATE_FORMAT} />,
      sortable: false,
    },
    {
      field: 'evrakno',
      headerName: translate(`fikoparApp.${entityName}.evrakno`),
      flex: 1,
      sortable: false,
    },
    {
      field: 'vade',
      headerName: translate(`fikoparApp.${entityName}.vade`),
      flex: 1,
      renderCell: ({ value }) => <DateCard date={value} sourceFormat={EVRAK_DATE_FORMAT} format={APP_LOCAL_DATE_FORMAT} />,
      sortable: false,
    },
    {
      field: 'tipi',
      headerName: translate(`fikoparApp.${entityName}.tipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.EvrakTipi.${params.row.tipi}`),
      sortable: false,
    },
    {
      field: 'depotipi',
      headerName: translate(`fikoparApp.${entityName}.depotipi`),
      flex: 1,
      valueGetter: params => translate(`fikoparApp.DepoTipi.${params.row.depotipi}`),
      sortable: false,
    },
    {
      field: 'aciklama',
      headerName: translate(`fikoparApp.${entityName}.aciklama`),
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
  ];
  return (
    <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
      <Grid container spacing={2}>
        <Grid item container spacing={2} sm={10}>
          <Grid item sm={12}>
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
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo1Borc"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo1Borc`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo1Alacak"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo1Alacak`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo1BorcBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo1BorcBakiye`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo1AlacakBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo1AlacakBakiye`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo2Borc"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo2Borc`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo2Alacak"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo2Alacak`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={8}></Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo2BorcBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo2BorcBakiye`)}
            control={control}
            fullWidth
            size="small"
            disabled
          />
        </Grid>
        <Grid item sm={2}>
          <CCurrencyTextField
            name="toplamlar.depo2AlacakBakiye"
            label={translate(`fikoparApp.${entityName}.toplamlar.depo2AlacakBakiye`)}
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

export default Cari;
