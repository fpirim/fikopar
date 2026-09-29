import { Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid } from '@mui/material';
import { translate } from 'react-jhipster';
import CancelIcon from '@mui/icons-material/Cancel';
import React, { useEffect } from 'react';
import SaveIcon from '@mui/icons-material/Save';
import { IslemTipi } from 'app/shared/model/enumerations/islem-tipi.model';
import CDatePicker from 'app/shared/core-components/core-datepicker';
import EntityAutoCompleteField from 'app/shared/components/entity-auto-complete-field';
import { EntityAutoCompleteLabelOptions, EntityAutoCompleteRenderOptions } from 'app/shared/components/entity-auto-complete-options';
import { getEntities as getSicils } from 'app/entities/sicil/sicil.reducer';
import { useForm } from 'react-hook-form';
import { ISicil } from 'app/shared/model/sicil.model';
import { EVRAK_DATE_FORMAT } from 'app/config/constants';
import dayjs from 'dayjs';
import * as yup from 'yup';
import { yupResolver } from '@hookform/resolvers/yup';

interface ConfirmOperationDialogProps {
  open: boolean;
  handleClose: () => void;
  handleOperation: (data: IOperationForm & { operation: IslemTipi | string }) => void;
  operation: IslemTipi | string;
}

interface IOperationForm {
  tarihi: string;
  sicil?: ISicil;
}

const operationDefaultValue: Readonly<IOperationForm> = {
  tarihi: dayjs().format(EVRAK_DATE_FORMAT),
  sicil: null,
};

export interface IOperationParameters extends IOperationForm {
  operation: IslemTipi | string;
}

export const ConfirmOperationDialog = ({ open, handleClose, operation, handleOperation }: ConfirmOperationDialogProps) => {
  const schema: yup.ObjectSchema<IOperationForm> = yup.object().shape({
    tarihi: yup.string().required(),
    sicil: yup.object<ISicil>().required(),
  });

  const { handleSubmit, control, reset } = useForm<IOperationForm>({
    defaultValues: operationDefaultValue,
    resolver: yupResolver(schema),
  });

  useEffect(() => {
    if (!open) {
      reset();
    }
  }, [open, reset]);

  const onSubmit = (values: IOperationForm) => {
    handleOperation({ operation, ...values });
  };

  return (
    <Dialog
      open={open}
      maxWidth="xl"
      onClose={handleClose}
      aria-labelledby="operation-dialog-title"
      aria-describedby="operation-dialog-description"
    >
      <DialogTitle id="operation-dialog-title">{translate(`fikoparApp.cekSenetDetay.islemler.${operation}.title`)}</DialogTitle>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          <Grid container spacing={2}>
            <Grid item sm={12}>
              <CDatePicker
                name="tarihi"
                label={translate(`fikoparApp.cekSenetDetay.islemler.tarihi`)}
                control={control}
                fullWidth
                size="small"
              />
            </Grid>
            {
              <Grid item sm={12}>
                <EntityAutoCompleteField
                  name="sicil"
                  control={control}
                  fullWidth
                  getOptionLabel={EntityAutoCompleteLabelOptions.SicilLabelOption}
                  renderOption={EntityAutoCompleteRenderOptions.SicilRenderOption}
                  label={translate(`fikoparApp.cekSenetDetay.islemler.sicil`)}
                  size="small"
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
            }
          </Grid>
        </DialogContent>
      </form>
      <DialogActions>
        <Button onClick={handleSubmit(onSubmit)} startIcon={<SaveIcon />} color="error">
          {translate('fikoparApp.cekSenetDetay.islemler.yes')}
        </Button>
        <Button onClick={handleClose} startIcon={<CancelIcon />} autoFocus color="primary">
          {translate('fikoparApp.cekSenetDetay.islemler.no')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
