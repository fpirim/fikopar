import React, { useEffect } from 'react';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, resetEntity, updateEntity } from './cari-devir.reducer';
import { getEntities as getSicils } from '../sicil/sicil.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, ICariDevir } from 'app/shared/model/cari-devir.model';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';
import CDatePicker from 'app/shared/core-components/core-datepicker';

export const CariDevirUpate = ({ id, showModal, handleClose }: { id?: any; showModal: boolean; handleClose: () => void }) => {
  const dispatch = useAppDispatch();

  const entityName = 'cariDevir';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);
  const depoTipiValues = Object.keys(DepoTipi);

  const { handleSubmit, control, setError, clearErrors, watch, setValue, reset } = useForm<ICariDevir>({
    defaultValues: defaultValue,
    values: entity,
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

  useEffect(() => {
    const subscription = watch((values, { name }) => {
      if (name === 'borc' && values.alacak !== 0) {
        setValue('alacak', 0);
      }
      if (name === 'alacak' && values.borc !== 0) {
        setValue('borc', 0);
      }
    });
    return subscription.unsubscribe;
  }, [watch]);

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
              <CDatePicker
                name="yil"
                label={translate(`fikoparApp.${entityName}.yil`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
                views={['year']}
                inputFormat={'YYYY0101'}
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
              <CCurrencyTextField
                name="borc"
                label={translate(`fikoparApp.${entityName}.borc`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CCurrencyTextField
                name="alacak"
                label={translate(`fikoparApp.${entityName}.alacak`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
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
    </Dialog>
  );
};
export default CariDevirUpate;
