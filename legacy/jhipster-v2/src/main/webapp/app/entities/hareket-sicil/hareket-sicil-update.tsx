import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, getGeneratedEvrakNo, resetEntity, updateEntity } from './hareket-sicil.reducer';
import { getEntities as getSicils } from '../sicil/sicil.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, IHareketSicil } from 'app/shared/model/hareket-sicil.model';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import CPhoneTextField from 'app/shared/core-components/core-phonetextfield';
import { defaultValue as defaultHareketDetayValue, IHareketDetay } from 'app/shared/model/hareket-detay.model';
import { GridColDef, GridRowModel, GridRowModesModel, GridRowSelectionModel } from '@mui/x-data-grid-pro';
import DataGrid from 'app/shared/core-components/datagrid';
import { decimal4Output, tlCurrencyOutput, tlCurrencyOutputWith4Decimal } from 'app/shared/NumberFormat';
import DataGridCurrencyTextField from 'app/shared/core-components/core-currencytextfield/datagrid-core-currencytextfield';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import DeleteIcon from '@mui/icons-material/Delete';
import { ConfirmDeleteDialog } from 'app/shared/hooks/use-entity-action';

type IIndexedHareketDetay = IHareketDetay & { internalId: number };

export const HareketSicilUpdate = ({ id, showModal, handleClose }: { id?: any; showModal: boolean; handleClose: () => void }) => {
  const dispatch = useAppDispatch();

  const entityName = 'hareketSicil';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);
  const depoTipiValues = Object.keys(DepoTipi);
  const evrakTipiValues = [
    EvrakTipi.IRSALIYELI_SATIS_FATURASI,
    EvrakTipi.SATIS_FATURASI,
    EvrakTipi.SATIS_IRSALIYESI,
    EvrakTipi.IRSALIYELI_ALIS_FATURASI,
    EvrakTipi.ALIS_FATURASI,
    EvrakTipi.ALIS_IRSALIYESI,
  ];

  const { handleSubmit, control, setError, clearErrors, watch, setValue, reset } = useForm<
    IHareketSicil & {
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
    if (showModal && entity.id == null && entity.depotipi === DepoTipi.DEPO2) {
      dispatch(
        getGeneratedEvrakNo({
          evraktipi: entity.evraktipi,
          depotipi: entity.depotipi,
          tarihi: entity.tarihi,
        })
      )
        .unwrap()
        .then(res => setValue('evrakno', res.data));
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

  const [hareketDetayRows, setHareketDetayRows] = useState<IHareketDetay[]>([]);
  const [hareketDetayRowsWithNewRow, setHareketDetayRowsWithNewRow] = useState<IIndexedHareketDetay[]>([]);
  const [rowSelectionModel, setRowSelectionModel] = React.useState<GridRowSelectionModel>([]);
  const [rowModesModel, setRowModesModel] = React.useState<GridRowModesModel>({});
  const [showDeleteModal, setShowDeleteModal] = React.useState(false);
  const handleDeleteClose = () => setShowDeleteModal(false);

  useEffect(() => {
    const subscription = watch((values, { name }) => {
      if (values && values.id == null && (name === 'depotipi' || name === 'evraktipi' || name === 'tarihi')) {
        if (values.depotipi === DepoTipi.DEPO2) {
          dispatch(
            getGeneratedEvrakNo({
              evraktipi: values.evraktipi,
              depotipi: values.depotipi,
              tarihi: values.tarihi,
            })
          )
            .unwrap()
            .then(res => setValue('evrakno', res.data));
        }
      }
      if (name === 'sicil') {
        setValue('hesapno', values.sicil?.hesapno ?? '');
        setValue('hesapadi', values.sicil?.hesapadi ?? '');
        setValue('adresi', values.sicil?.adresi ?? '');
        setValue('ilce', values.sicil?.ilce ?? '');
        setValue('sehir', values.sicil?.sehir ?? '');
        setValue('vda', values.sicil?.vda ?? '');
        setValue('tcno', values.sicil?.tcno ?? '');
        setValue('vno', values.sicil?.vno ?? '');
        setValue('telefon', values.sicil?.telefon ?? '');
        setValue('gun', values.sicil?.gun ?? 0);
      } else if (name === undefined || name === 'hareketDetays') {
        if (name === undefined && values.hareketDetays) {
          setHareketDetayRows(values.hareketDetays);
        }
        let totals = {
          toplam: 0,
          isktoplam: 0,
          kdvtoplam: 0,
        };
        if (values.hareketDetays) {
          totals = values.hareketDetays?.reduce<typeof totals>((acc, val) => {
            const tutari = val.adet * val.fiyati;
            const iskonto = (tutari * val.iskonto) / 100;
            const kdv = ((tutari - iskonto) * val.kdv) / 100;
            return {
              toplam: acc.toplam + tutari,
              isktoplam: acc.isktoplam + iskonto,
              kdvtoplam: acc.kdvtoplam + kdv,
            };
          }, totals);
        }
        setValue('toplam', totals.toplam.round(2));
        setValue('isktoplam', totals.isktoplam.round(2));
        setValue('aratoplam', totals.toplam.round(2) - totals.isktoplam.round(2));
        setValue('kdvtoplam', totals.kdvtoplam.round(2));
        setValue('tutari', totals.toplam.round(2) - totals.isktoplam.round(2) + totals.kdvtoplam.round(2));
      }
    });
    return subscription.unsubscribe;
  }, [watch, setValue]);

  useEffect(() => {
    setHareketDetayRowsWithNewRow([
      ...hareketDetayRows.map((row, index) => ({ ...row, internalId: index + 1 })),
      {
        ...defaultHareketDetayValue,
        internalId: hareketDetayRows.length + 1,
      },
    ]);
    setValue('hareketDetays', hareketDetayRows);
  }, [hareketDetayRows, setValue]);

  const handleRowModesModelChange = (newRowModesModel: GridRowModesModel) => {
    setRowModesModel(newRowModesModel);
  };

  const handleDeleteSelectedRows = () => {
    setHareketDetayRows(prev => prev.filter((_, index) => !rowSelectionModel.includes(index + 1)));
    setRowSelectionModel([]);
  };

  const processRowUpdate = (newRow: GridRowModel<IIndexedHareketDetay>) => {
    const updatedRow = { ...newRow };
    setHareketDetayRows(prev => {
      if (updatedRow.internalId === prev.length + 1) {
        return [...prev, updatedRow];
      }
      return prev.map((row, index) => (index + 1 === newRow.internalId ? updatedRow : row));
    });
    return updatedRow;
  };

  const hareketDetayColumns: GridColDef<IIndexedHareketDetay>[] = [
    {
      field: 'internalId',
      headerName: translate(`fikoparApp.hareketDetay.id`),
      flex: 0.05,
      sortable: false,
    },
    {
      field: 'stokno',
      headerName: translate(`fikoparApp.hareketDetay.stokno`),
      flex: 0.1,
      sortable: false,
      editable: true,
    },
    {
      field: 'parcano',
      headerName: translate(`fikoparApp.hareketDetay.parcano`),
      flex: 0.09,
      sortable: false,
      editable: true,
    },
    {
      field: 'tipi',
      headerName: translate(`fikoparApp.hareketDetay.tipi`),
      flex: 0.07,
      sortable: false,
      editable: true,
    },
    {
      field: 'cinsi',
      headerName: translate(`fikoparApp.hareketDetay.cinsi`),
      flex: 0.26,
      sortable: false,
      editable: true,
    },
    {
      field: 'marka',
      headerName: translate(`fikoparApp.hareketDetay.marka`),
      flex: 0.09,
      sortable: false,
      editable: true,
    },
    {
      field: 'iskonto',
      type: 'number',
      headerName: translate(`fikoparApp.hareketDetay.iskonto`),
      flex: 0.06,
      sortable: false,
      editable: true,
      valueFormatter: ({ value }) => decimal4Output.format(value),
      renderEditCell: params => (
        <DataGridCurrencyTextField
          params={params}
          currencyTextFieldProps={{
            currencySymbol: '',
            decimalPlaces: 4,
          }}
        />
      ),
    },
    {
      field: 'kdv',
      type: 'number',
      headerName: translate(`fikoparApp.hareketDetay.kdv`),
      flex: 0.06,
      sortable: false,
      editable: true,
      valueFormatter: ({ value }) => decimal4Output.format(value),
      renderEditCell: params => (
        <DataGridCurrencyTextField
          params={params}
          currencyTextFieldProps={{
            currencySymbol: '',
            decimalPlaces: 4,
          }}
        />
      ),
    },
    {
      field: 'adet',
      type: 'number',
      headerName: translate(`fikoparApp.hareketDetay.adet`),
      flex: 0.08,
      sortable: false,
      editable: true,
      valueFormatter: ({ value }) => decimal4Output.format(value),
      renderEditCell: params => (
        <DataGridCurrencyTextField
          params={params}
          currencyTextFieldProps={{
            currencySymbol: '',
            decimalPlaces: 4,
          }}
        />
      ),
    },
    {
      field: 'fiyati',
      type: 'number',
      headerName: translate(`fikoparApp.hareketDetay.fiyati`),
      flex: 0.1,
      sortable: false,
      editable: true,
      valueFormatter: ({ value }) => tlCurrencyOutput.format(value),
      renderEditCell: params => (
        <DataGridCurrencyTextField
          params={params}
          currencyTextFieldProps={{
            decimalPlaces: 4,
          }}
        />
      ),
    },
    {
      field: 'tutari',
      type: 'number',
      headerName: translate(`fikoparApp.hareketDetay.tutari`),
      flex: 0.1,
      sortable: false,
      editable: false,
      valueGetter: params => Number(params.row.adet * params.row.fiyati).round(4),
      valueFormatter: ({ value }) => tlCurrencyOutputWith4Decimal.format(value),
    },
  ];

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
      <form
        onSubmit={() => {
          handleSubmit(onSubmit)();
        }}
      >
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
                  name="evrakno"
                  label={translate(`fikoparApp.${entityName}.evrakno`)}
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
              <Grid item sm={12}>
                <CTextField
                  name="adresi"
                  label={translate(`fikoparApp.${entityName}.adresi`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                  multiline
                  rows={3}
                />
              </Grid>
              <Grid item sm={6}>
                <CTextField
                  name="ilce"
                  label={translate(`fikoparApp.${entityName}.ilce`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={6}>
                <CTextField
                  name="sehir"
                  label={translate(`fikoparApp.${entityName}.sehir`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
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
                  name="sevktarihi"
                  label={translate(`fikoparApp.${entityName}.sevktarihi`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={6}>
                <CTextField
                  name="saati"
                  label={translate(`fikoparApp.${entityName}.saati`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={6}>
                <CCurrencyTextField
                  name="gun"
                  label={translate(`fikoparApp.${entityName}.gun`)}
                  control={control}
                  fullWidth
                  size="small"
                  currencySymbol=""
                  decimalPlaces={0}
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CTextField
                  name="tcno"
                  label={translate(`fikoparApp.${entityName}.tcno`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CTextField
                  name="vda"
                  label={translate(`fikoparApp.${entityName}.vda`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CTextField
                  name="vno"
                  label={translate(`fikoparApp.${entityName}.vno`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled={entityLoading || updating}
                />
              </Grid>
              <Grid item sm={12}>
                <CPhoneTextField
                  name="telefon"
                  label={translate(`fikoparApp.${entityName}.telefon`)}
                  control={control}
                  size="small"
                  fullWidth
                  disabled={entityLoading || updating}
                />
              </Grid>
            </Grid>
            <Grid item sm={12}>
              <DataGrid
                sx={{
                  height: 300,
                }}
                columns={hareketDetayColumns}
                rows={hareketDetayRowsWithNewRow}
                disableColumnFilter
                disableColumnMenu
                disableRowSelectionOnClick
                checkboxSelection
                hideFooter
                editMode="cell"
                density="compact"
                getRowId={row => row.internalId}
                rowModesModel={rowModesModel}
                onRowModesModelChange={handleRowModesModelChange}
                processRowUpdate={processRowUpdate}
                onRowSelectionModelChange={selectionModel => setRowSelectionModel(selectionModel)}
                rowSelectionModel={rowSelectionModel}
              />
            </Grid>
            <Grid container item sm={9} spacing={1}>
              <Grid item sm={2}>
                <Button
                  variant="contained"
                  startIcon={<DeleteIcon />}
                  color={entityLoading ? 'secondary' : 'error'}
                  disabled={entityLoading || !rowSelectionModel.length}
                  onClick={() => setShowDeleteModal(true)}
                >
                  {translate('entity.action.delete')}
                </Button>
              </Grid>
            </Grid>
            <Grid container item sm={3} spacing={1} justifyContent="right">
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="toplam"
                  label={translate(`fikoparApp.${entityName}.toplam`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled
                />
              </Grid>
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="isktoplam"
                  label={translate(`fikoparApp.${entityName}.isktoplam`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled
                />
              </Grid>
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="aratoplam"
                  label={translate(`fikoparApp.${entityName}.aratoplam`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled
                />
              </Grid>
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="kdvtoplam"
                  label={translate(`fikoparApp.${entityName}.kdvtoplam`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled
                />
              </Grid>
              <Grid item sm={12}>
                <CCurrencyTextField
                  name="tutari"
                  label={translate(`fikoparApp.${entityName}.tutari`)}
                  control={control}
                  fullWidth
                  size="small"
                  disabled
                />
              </Grid>
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
          onClick={() => {
            handleSubmit(onSubmit)();
          }}
        >
          {translate('entity.action.save')}
        </Button>
      </DialogActions>
      <ConfirmDeleteDialog
        open={showDeleteModal}
        handleClose={handleDeleteClose}
        handleDelete={() => {
          handleDeleteSelectedRows();
          handleDeleteClose();
        }}
        entityName="hareketDetay"
      />
    </Dialog>
  );
};
export default HareketSicilUpdate;
