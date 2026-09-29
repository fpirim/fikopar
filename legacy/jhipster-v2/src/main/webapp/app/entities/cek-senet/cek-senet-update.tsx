import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, getGeneratedCekSenetNo, resetEntity, updateEntity } from './cek-senet.reducer';
import { getEntities as getSicils } from '../sicil/sicil.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, ICekSenet } from 'app/shared/model/cek-senet.model';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Fade, Grid, Menu, MenuItem } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { ICekSenetDetay } from 'app/shared/model/cek-senet-detay.model';
import { GridColDef } from '@mui/x-data-grid-pro';
import DataGrid from 'app/shared/core-components/datagrid';
import { tlCurrencyOutput } from 'app/shared/NumberFormat';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import DateCard from 'app/shared/components/date-card';
import { APP_LOCAL_DATE_FORMAT, EVRAK_DATE_FORMAT } from 'app/config/constants';
import AccountBalanceIcon from '@mui/icons-material/AccountBalance';
import SavingsIcon from '@mui/icons-material/Savings';
import PriceCheckIcon from '@mui/icons-material/PriceCheck';
import MoneyOffIcon from '@mui/icons-material/MoneyOff';
import ThumbDownOffAltIcon from '@mui/icons-material/ThumbDownOffAlt';
import AccountBalanceWalletIcon from '@mui/icons-material/AccountBalanceWallet';
import { ConfirmOperationDialog, IOperationParameters } from 'app/entities/cek-senet/cek-senet-islemleri';
import { IslemTipi } from 'app/shared/model/enumerations/islem-tipi.model';

export const CekSenetUpdate = ({ id, showModal, handleClose }: { id?: any; showModal: boolean; handleClose: () => void }) => {
  const dispatch = useAppDispatch();

  const entityName = 'cekSenet';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);
  const depoTipiValues = Object.keys(DepoTipi);
  const evrakTipiValues = [EvrakTipi.BORC_SENETI, EvrakTipi.BORC_CEKI, EvrakTipi.ALACAK_SENETI, EvrakTipi.ALACAK_CEKI];

  const { handleSubmit, control, setError, clearErrors, watch, setValue, reset } = useForm<
    ICekSenet & {
      toplam: number;
    }
  >({
    defaultValues: { ...defaultValue, toplam: 0 },
    values: { ...entity, toplam: 0 },
    mode: 'all',
  });

  useEffect(() => {
    if (fieldErrors) {
      fieldErrors.map(fieldError => {
        setError(fieldError.field, {
          type: 'custom',
          message: fieldError.message,
          // message: translate(`error.${fieldError.message}`, fieldError.parameters)
        });
      });
    } else {
      clearErrors();
    }
  }, [fieldErrors, setError, clearErrors]);

  useEffect(() => {
    if (!showModal) {
      dispatch(resetEntity());
      clearErrors();
      reset();
    } else if (id !== null) {
      dispatch(getEntity(id));
    }
  }, [id, showModal, reset]);

  useEffect(() => {
    if (showModal && entity.id == null) {
      dispatch(
        getGeneratedCekSenetNo({
          evraktipi: entity.evraktipi,
          depotipi: entity.depotipi,
          tarihi: entity.tarihi,
        })
      )
        .unwrap()
        .then(res => setValue('ceksenetno', res.data));
    }
  }, [showModal, entity]);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const onSubmit = values => {
    if (id !== null) {
      dispatch(
        updateEntity({
          ...entity,
          ...values,
        })
      );
    } else {
      dispatch(
        createEntity({
          ...entity,
          ...values,
        })
      );
    }
  };

  const [cekSenetDetayRows, setCekSenetDetayRows] = useState<ICekSenetDetay[]>([]);
  const [showDeleteModal, setShowDeleteModal] = React.useState(false);
  const handleDeleteClose = () => setShowDeleteModal(false);

  useEffect(() => {
    const subscription = watch((values, { name }) => {
      if (values && values.id == null && (name === 'depotipi' || name === 'evraktipi' || name == 'tarihi')) {
        dispatch(
          getGeneratedCekSenetNo({
            evraktipi: values.evraktipi,
            depotipi: values.depotipi,
            tarihi: values.tarihi,
          })
        )
          .unwrap()
          .then(res => setValue('ceksenetno', res.data));
      }
      if (name === undefined && values.cekSenetDetays) {
        setCekSenetDetayRows(values.cekSenetDetays);
      }
    });
    return subscription.unsubscribe;
  }, [watch, setValue]);

  const cekSenetDetayColumns: GridColDef<ICekSenetDetay>[] = [
    {
      field: 'id',
      headerName: translate(`fikoparApp.cekSenetDetay.id`),
      flex: 0.5,
      sortable: false,
    },
    {
      field: 'makbuzno',
      headerName: translate(`fikoparApp.makbuz.makbuzno`),
      flex: 0.7,
      valueGetter: params => params.row.makbuz?.makbuzno ?? '',
    },
    {
      field: 'tarihi',
      headerName: translate(`fikoparApp.${entityName}.tarihi`),
      flex: 0.7,
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
      field: 'islemtipi',
      headerName: translate(`fikoparApp.cekSenetDetay.islemtipi`),
      flex: 0.6,
      valueGetter: params => translate(`fikoparApp.IslemTipi.${params.row.islemtipi}`),
    },
    {
      field: 'tutar',
      type: 'number',
      headerName: translate(`fikoparApp.cekSenetDetay.tutar`),
      flex: 0.6,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
    },
  ];

  // Islemler

  const [anchorElIslemler, setAnchorElIslemler] = React.useState<null | HTMLElement>(null);
  const openIslemler = Boolean(anchorElIslemler);
  const handleClickIslemler = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElIslemler(event.currentTarget);
  };
  const handleCloseIslemler = () => {
    setAnchorElIslemler(null);
  };

  const [openOperationDialog, setOpenOperationDialog] = React.useState<boolean>(false);
  const [operation, setOperation] = React.useState<IslemTipi | string>();

  const handleCloseOperation = () => {
    setOpenOperationDialog(false);
  };

  const handleOperation = (params: IOperationParameters) => {
    console.log('--------------', params);
    handleCloseOperation();
  };

  const processOperation = (event: React.MouseEvent<HTMLLIElement>) => {
    const { value } = event.currentTarget.dataset;
    handleCloseIslemler();
    setOperation(value);
    setOpenOperationDialog(true);
  };

  return (
    <Dialog
      maxWidth="xl"
      open={showModal}
      onClose={handleClose}
      aria-labelledby={`${entityName}-edit-dialog-title`}
      onKeyUp={event => {
        if (event.key === 'Escape') {
          handleClose();
        }
        // else if (event.key === 'Enter') {
        //   handleSubmit(onSubmit)();
        // }
      }}
    >
      <DialogTitle id={`${entityName}-edit-dialog-title`}>{translate(`fikoparApp.${entityName}.detail.title`)}</DialogTitle>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          <Grid container spacing={2}>
            <Grid container item sm={6} spacing={1}>
              <Grid item sm={6}>
                <CTextField
                  name="id"
                  type="text"
                  control={control}
                  fullWidth
                  label={translate('global.field.id')}
                  size="small"
                  disabled={true}
                />
              </Grid>
              <Grid item sm={6}>
                <CTextField
                  name="ceksenetno"
                  label={translate(`fikoparApp.${entityName}.ceksenetno`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={8}>
                <CAutoComplete
                  name="evraktipi"
                  control={control}
                  fullWidth
                  getOptionLabel={option => translate(`fikoparApp.EvrakTipi.${option}`)}
                  options={evrakTipiValues}
                  label={translate(`fikoparApp.${entityName}.evraktipi`)}
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={4}>
                <CAutoComplete
                  name="depotipi"
                  control={control}
                  fullWidth
                  getOptionLabel={option => translate(`fikoparApp.DepoTipi.${option}`)}
                  options={depoTipiValues}
                  label={translate(`fikoparApp.${entityName}.depotipi`)}
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <EntityAutoCompleteField
                  name="sicil"
                  control={control}
                  fullWidth
                  getOptionLabel={EntityAutoCompleteLabelOptions.SicilLabelOption}
                  renderOption={EntityAutoCompleteRenderOptions.SicilRenderOption}
                  label={translate(`fikoparApp.sicil.detail.title`)}
                  size="small"
                  disabled={entityLoading || updating}
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
            </Grid>
            <Grid container item sm={6} spacing={1}>
              <Grid item sm={6}>
                <CDatePicker
                  name="tarihi"
                  label={translate(`fikoparApp.${entityName}.tarihi`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={6}>
                <CDatePicker
                  name="vade"
                  label={translate(`fikoparApp.${entityName}.vade`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CTextField
                  name="aciklama"
                  label={translate(`fikoparApp.${entityName}.aciklama`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="tutar"
                  label={translate(`fikoparApp.${entityName}.tutar`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
            </Grid>
            <Grid item sm={12}>
              <DataGrid
                sx={{
                  height: 300,
                }}
                columns={cekSenetDetayColumns}
                rows={cekSenetDetayRows}
                disableColumnFilter
                disableColumnMenu
                disableRowSelectionOnClick
                hideFooter
                density="compact"
              />
            </Grid>
            <Grid container item sm={9} spacing={1}>
              <Button
                id="fade-button"
                aria-controls={openIslemler ? 'fade-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={openIslemler ? 'true' : undefined}
                onClick={handleClickIslemler}
              >
                {translate(`fikoparApp.cekSenetDetay.islemler.title`)}
              </Button>
              <Menu
                MenuListProps={{
                  'aria-labelledby': 'fade-button',
                }}
                id="composition-menu"
                anchorEl={anchorElIslemler}
                open={openIslemler}
                TransitionComponent={Fade}
                onClose={handleCloseIslemler}
              >
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.BANKAYA_VERME}>
                  <AccountBalanceIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.BANKAYA_VERME.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.BANKADAN_TAHSIL}>
                  <SavingsIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.BANKADAN_TAHSIL.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.ELDEN_TAHSIL}>
                  <PriceCheckIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.ELDEN_TAHSIL.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.CIRO}>
                  <AccountBalanceWalletIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.CIRO.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.PROTESTO}>
                  <MoneyOffIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.PROTESTO.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={IslemTipi.IADE}>
                  <ThumbDownOffAltIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.IADE.title`)}
                </MenuItem>
                <MenuItem disabled={id === null || entityLoading} onClick={processOperation} data-value={'CANCEL'}>
                  <CancelIcon />
                  &nbsp;
                  {translate(`fikoparApp.cekSenetDetay.islemler.CANCEL.title`)}
                </MenuItem>
              </Menu>
            </Grid>
          </Grid>
        </DialogContent>
      </form>
      <DialogActions>
        <Button
          variant="contained"
          color="secondary"
          startIcon={<CancelIcon />}
          disabled={entityLoading || updating}
          onClick={handleClose}
          tabIndex={1}
        >
          {translate('entity.action.back')}
        </Button>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          disabled={entityLoading || updating}
          onClick={handleSubmit(onSubmit)}
        >
          {translate('entity.action.save')}
        </Button>
      </DialogActions>
      <ConfirmOperationDialog
        open={openOperationDialog}
        handleClose={handleCloseOperation}
        handleOperation={handleOperation}
        operation={operation}
      />
    </Dialog>
  );
};
export default CekSenetUpdate;
