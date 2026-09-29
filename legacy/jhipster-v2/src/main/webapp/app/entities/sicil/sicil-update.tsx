import React, { useEffect } from 'react';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, resetEntity, updateEntity } from './sicil.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, ISicil } from 'app/shared/model/sicil.model';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import CPhoneTextField from 'app/shared/core-components/core-phonetextfield';
import { SicilTipi } from 'app/shared/model/enumerations/sicil-tipi.model';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';

export const SicilUpdate = ({ id, showModal, handleClose }: { id?: any; showModal: boolean; handleClose: () => void }) => {
  const dispatch = useAppDispatch();

  const entityName = 'sicil';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);
  const sicilTipiValues = Object.keys(SicilTipi);

  const { handleSubmit, control, setError, clearErrors, reset } = useForm<ISicil>({
    defaultValues: defaultValue,
    values: entity,
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

  return (
    <Dialog
      maxWidth="md"
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
            <Grid item md={8}>
              {translate(`fikoparApp.${entityName}.home.createOrEditLabel`)}
            </Grid>
            <Grid item sm={12}>
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
            <Grid item sm={12}>
              <CTextField
                name="hesapno"
                label={translate(`fikoparApp.${entityName}.hesapno`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CTextField
                name="hesapadi"
                label={translate(`fikoparApp.${entityName}.hesapadi`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
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
                rows={4}
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
            <Grid item sm={6}>
              <CTextField
                name="vda"
                label={translate(`fikoparApp.${entityName}.vda`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={6}>
              <CTextField
                name="vno"
                label={translate(`fikoparApp.${entityName}.vno`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={6}>
              <CPhoneTextField
                name="telefon"
                label={translate(`fikoparApp.${entityName}.telefon`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={6}>
              <CTextField
                name="tcno"
                label={translate(`fikoparApp.${entityName}.tcno`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={6}>
              <CCurrencyTextField
                name="gun"
                label={translate(`fikoparApp.${entityName}.gun`)}
                currencySymbol=""
                decimalPlaces={0}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={6}>
              <CCurrencyTextField
                name="iskonto"
                currencySymbol="%"
                decimalPlaces={4}
                label={translate(`fikoparApp.${entityName}.iskonto`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CAutoComplete
                name="siciltipi"
                control={control}
                fullWidth
                getOptionLabel={option => translate(`fikoparApp.SicilTipi.${option}`)}
                options={sicilTipiValues}
                label={translate(`fikoparApp.${entityName}.siciltipi`)}
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12} gap={1} display="flex"></Grid>
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
    </Dialog>
  );
};
export default SicilUpdate;
