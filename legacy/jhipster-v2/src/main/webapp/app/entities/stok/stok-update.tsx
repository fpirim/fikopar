import React, { useEffect } from 'react';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, resetEntity, updateEntity } from './stok.reducer';
import { getEntities as getKatalogs } from '../katalog/katalog.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, IStok } from 'app/shared/model/stok.model';
import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import CCurrencyTextField from 'app/shared/core-components/core-currencytextfield';

export const StokUpdate = ({ id, showModal, handleClose }: { id?: any; showModal: boolean; handleClose: () => void }) => {
  const dispatch = useAppDispatch();

  const entityName = 'stok';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);

  const { handleSubmit, control, setError, clearErrors, reset } = useForm<IStok>({
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
                name="stokno"
                label={translate(`fikoparApp.${entityName}.stokno`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CTextField
                name="parcano"
                label={translate(`fikoparApp.${entityName}.parcano`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CTextField
                name="tipi"
                label={translate(`fikoparApp.${entityName}.tipi`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CTextField
                name="cinsi"
                label={translate(`fikoparApp.${entityName}.cinsi`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CTextField
                name="marka"
                label={translate(`fikoparApp.${entityName}.marka`)}
                control={control}
                fullWidth
                size="small"
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <CCurrencyTextField
                name="fiyati"
                label={translate(`fikoparApp.${entityName}.fiyati`)}
                control={control}
                size="small"
                fullWidth
                disabled={entityLoading || updating}
              />
            </Grid>
            <Grid item sm={12}>
              <EntityAutoCompleteField
                name="katalog"
                control={control}
                fullWidth
                getOptionLabel={EntityAutoCompleteLabelOptions.KatalogLabelOption}
                renderOption={EntityAutoCompleteRenderOptions.KatalogRenderOption}
                label={translate(`fikoparApp.katalog.detail.title`)}
                size="small"
                disabled={entityLoading || updating}
                entityName="katalog"
                fetchEntities={getKatalogs}
                searchParams={['katalogadi.contains']}
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
export default StokUpdate;
