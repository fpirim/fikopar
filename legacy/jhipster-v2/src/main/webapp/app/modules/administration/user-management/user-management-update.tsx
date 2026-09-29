import React, { useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { translate } from 'react-jhipster';
import { createEntity, getEntity, getRoles, resetEntity, updateEntity } from './user-management.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { useForm } from 'react-hook-form';
import { defaultValue, IUser } from 'app/shared/model/user.model';
import { Button, Container, Grid, Paper } from '@mui/material';
import CTextField from 'app/shared/core-components/core-textfield';
import CCheckField from 'app/shared/core-components/core-checkfield';
import CAutoComplete from 'app/shared/core-components/core-autocomplete';
import CancelIcon from '@mui/icons-material/Cancel';
import SaveIcon from '@mui/icons-material/Save';
import { languages, locales } from 'app/config/translation';

export const UserManagementUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { login } = useParams<'login'>();

  const entityName = 'userManagement';

  const entity = useAppSelector(state => state[entityName].entity);
  const fieldErrors = useAppSelector(state => state[entityName].fieldErrors);
  const entityLoading = useAppSelector(state => state[entityName].entityLoading);
  const updating = useAppSelector(state => state[entityName].updating);
  const authorities = useAppSelector(state => state[entityName].authorities);
  const updateSuccess = useAppSelector(state => state[entityName].updateSuccess);

  const { handleSubmit, control, setError, clearErrors } = useForm<IUser>({
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
    clearErrors();
    dispatch(getRoles());
    if (login) {
      dispatch(getEntity(login));
    } else {
      dispatch(resetEntity());
    }
  }, [login]);

  useEffect(() => {
    if (updateSuccess) {
      navigate(`/admin/user-management`, { replace: true });
    }
  }, [updateSuccess, navigate]);

  const onSubmit = values => {
    if (login) {
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
    <Container maxWidth="md">
      <Paper sx={{ padding: 2, margin: 2 }} elevation={3}>
        <Grid container spacing={2}>
          <Grid item md={8}>
            {translate('userManagement.home.createOrEditLabel')}
          </Grid>
          <Grid item sm={12}>
            <CTextField name="id" control={control} fullWidth label={translate('global.field.id')} size="small" disabled={true} />
          </Grid>
          <Grid item sm={12}>
            <CTextField
              name="login"
              label={translate('userManagement.login')}
              control={control}
              fullWidth
              size="small"
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12}>
            <CTextField
              name="firstName"
              label={translate('userManagement.firstName')}
              control={control}
              fullWidth
              size="small"
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12}>
            <CTextField
              name="lastName"
              label={translate('userManagement.lastName')}
              control={control}
              fullWidth
              size="small"
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12}>
            <CTextField
              name="email"
              label={translate('global.form.email.label')}
              placeholder={translate('global.form.email.placeholder')}
              control={control}
              fullWidth
              size="small"
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12}>
            <CCheckField
              name="activated"
              label={translate('userManagement.activated')}
              control={control}
              size="small"
              disabled={entityLoading || updating || login === undefined}
            />
          </Grid>
          <Grid item sm={12}>
            <CAutoComplete
              name="langKey"
              control={control}
              fullWidth
              getOptionLabel={option => languages[option]?.name ?? ''}
              options={locales}
              label={translate('userManagement.langKey')}
              size="small"
              disableClearable
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12}>
            <CAutoComplete
              name="authorities"
              control={control}
              fullWidth
              options={authorities}
              label={translate('userManagement.profiles')}
              size="small"
              multiple
              disabled={entityLoading || updating}
            />
          </Grid>
          <Grid item sm={12} gap={1} display="flex">
            <Button
              variant="contained"
              color="secondary"
              startIcon={<CancelIcon />}
              disabled={entityLoading || updating}
              onClick={() => navigate('/admin/user-management', { replace: true })}
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
          </Grid>
        </Grid>
      </Paper>
    </Container>
  );
};

/*
          <div>
            <Row className="justify-content-center">
              <Col md="8">
                <h1>
                  <Translate contentKey="userManagement.home.createOrEditLabel">Create or edit a User</Translate>
                </h1>
              </Col>
            </Row>
            <Row className="justify-content-center">
              <Col md="8">
                {loading ? (
                  <p>Loading...</p>
                ) : (
                  <ValidatedForm onSubmit={saveUser} defaultValues={user}>
                    {user.id ? (
                      <ValidatedField
                        type="text"
                        name="id"
                        required
                        readOnly
                        label={translate('global.field.id')}
                        validate={{required: true}}
                      />
                    ) : null}
                    <ValidatedField
                      type="text"
                      name="login"
                      label={translate('userManagement.login')}
                      validate={{
                        required: {
                          value: true,
                          message: translate('register.messages.validate.login.required'),
                        },
                        pattern: {
                          value: /^[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$|^[_.@A-Za-z0-9-]+$/,
                          message: translate('register.messages.validate.login.pattern'),
                        },
                        minLength: {
                          value: 1,
                          message: translate('register.messages.validate.login.minlength'),
                        },
                        maxLength: {
                          value: 50,
                          message: translate('register.messages.validate.login.maxlength'),
                        },
                      }}
                    />
                    <ValidatedField
                      type="text"
                      name="firstName"
                      label={translate('userManagement.firstName')}
                      validate={{
                        maxLength: {
                          value: 50,
                          message: translate('entity.validation.maxlength', {max: 50}),
                        },
                      }}
                    />
                    <ValidatedField
                      type="text"
                      name="lastName"
                      label={translate('userManagement.lastName')}
                      validate={{
                        maxLength: {
                          value: 50,
                          message: translate('entity.validation.maxlength', {max: 50}),
                        },
                      }}
                    />
                    <FormText>This field cannot be longer than 50 characters.</FormText>
                    <ValidatedField
                      name="email"
                      label={translate('global.form.email.label')}
                      placeholder={translate('global.form.email.placeholder')}
                      type="email"
                      validate={{
                        required: {
                          value: true,
                          message: translate('global.messages.validate.email.required'),
                        },
                        minLength: {
                          value: 5,
                          message: translate('global.messages.validate.email.minlength'),
                        },
                        maxLength: {
                          value: 254,
                          message: translate('global.messages.validate.email.maxlength'),
                        },
                        validate: v => isEmail(v) || translate('global.messages.validate.email.invalid'),
                      }}
                    />
                    <ValidatedField
                      type="checkbox"
                      name="activated"
                      check
                      value={true}
                      disabled={!id}
                      label={translate('userManagement.activated')}
                    />
                    <ValidatedField type="select" name="langKey" label={translate('userManagement.langKey')}>
                      {locales.map(locale => (
                        <option value={locale} key={locale}>
                          {languages[locale].name}
                        </option>
                      ))}
                    </ValidatedField>
                    <ValidatedField type="select" name="authorities" multiple
                                    label={translate('userManagement.profiles')}>
                      {authorities.map(role => (
                        <option value={role} key={role}>
                          {role}
                        </option>
                      ))}
                    </ValidatedField>
                    <Button tag={Link} to="/admin/user-management" replace color="info">
                      <FontAwesomeIcon icon="arrow-left"/>
                      &nbsp;
                      <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
                    </Button>
                    &nbsp;
                    <Button color="primary" type="submit" disabled={isInvalid || updating}>
                      <FontAwesomeIcon icon="save"/>
                      &nbsp;
                      <Translate contentKey="entity.action.save">Save</Translate>
                    </Button>
                  </ValidatedForm>
                )}
              </Col>
            </Row>
          </div>
          */

export default UserManagementUpdate;
