import React from 'react';
import { translate } from 'react-jhipster';
import { useForm } from 'react-hook-form';
import CTextField from 'app/shared/core-components/core-textfield';
import CCheckField from 'app/shared/core-components/core-checkfield';
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Grid, Link } from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import CancelIcon from '@mui/icons-material/Cancel';

export interface ILoginModalProps {
  showModal: boolean;
  loginError: boolean;
  handleLogin: (username: string, password: string, rememberMe: boolean) => void;
  handleClose: () => void;
}

const LoginModal = (props: ILoginModalProps) => {
  const onSubmit = ({ username, password, rememberMe }) => {
    props.handleLogin(username, password, rememberMe);
  };

  const {
    handleSubmit,
    control,
    formState: { errors, touchedFields },
  } = useForm();

  const { loginError, handleClose } = props;

  return (
    <Dialog
      open={props.showModal}
      onClose={handleClose}
      aria-labelledby="login-edit-dialog-title"
      onKeyUp={event => {
        if (event.key === 'Escape') {
          handleClose();
        } else if (event.key === 'Enter') {
          handleSubmit(onSubmit)();
        }
      }}
    >
      <DialogTitle id="login-edit-dialog-title">{translate(`login.title`)}</DialogTitle>
      <form onSubmit={handleSubmit(onSubmit)}>
        <DialogContent>
          <Grid container spacing={2}>
            <Grid item md={12}>
              {loginError ? <Alert severity="error">{translate(`login.messages.error.authentication`)}</Alert> : null}
            </Grid>
            <Grid item md={12}>
              <CTextField
                name="username"
                control={control}
                fullWidth
                label={translate('global.form.username.label')}
                placeholder={translate('global.form.username.placeholder')}
                size="small"
                autoFocus
              />
            </Grid>
            <Grid item md={12}>
              <CTextField
                name="password"
                type="password"
                control={control}
                fullWidth
                label={translate('login.form.password')}
                placeholder={translate('login.form.password.placeholder')}
                size="small"
              />
            </Grid>
            <Grid item md={12}>
              <CCheckField name="rememberMe" label={translate('login.form.rememberme')} control={control} value={true} />
            </Grid>
          </Grid>
          <Grid item md={12}>
            <Alert severity="warning">
              <Link color="inherit" href="/account/reset/request">
                {translate('login.password.forgot')}
              </Link>
            </Alert>
          </Grid>
          <Grid item md={12}>
            <Alert severity="warning">
              <span>{translate('global.messages.info.register.noaccount')}</span>{' '}
              <Link href="/account/register">{translate('global.messages.info.register.link')}</Link>
            </Alert>
          </Grid>
        </DialogContent>
      </form>
      <DialogActions>
        <Button
          variant="contained"
          color="secondary"
          startIcon={<CancelIcon />}
          // disabled={entityLoading || updating}
          onClick={handleClose}
          tabIndex={1}
        >
          {translate('entity.action.cancel')}
        </Button>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          // disabled={entityLoading || updating}
          onClick={handleSubmit(onSubmit)}
        >
          {translate('login.form.button')}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default LoginModal;
