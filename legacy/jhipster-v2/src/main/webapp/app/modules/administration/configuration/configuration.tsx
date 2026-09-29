import React, { useEffect, useState } from 'react';
import { translate } from 'react-jhipster';

import { getConfigurations, getEnv } from '../administration.reducer';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import {
  Box,
  Container,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from '@mui/material';

export const ConfigurationPage = () => {
  const [filter, setFilter] = useState('');
  const [reversePrefix, setReversePrefix] = useState(false);
  const [reverseProperties, setReverseProperties] = useState(false);
  const dispatch = useAppDispatch();

  const configuration = useAppSelector(state => state.administration.configuration);

  useEffect(() => {
    dispatch(getConfigurations());
    dispatch(getEnv());
  }, []);

  const changeFilter = evt => setFilter(evt.target.value);

  const envFilterFn = configProp => configProp.toUpperCase().includes(filter.toUpperCase());

  const propsFilterFn = configProp => configProp.prefix.toUpperCase().includes(filter.toUpperCase());

  const changeReversePrefix = () => setReversePrefix(!reversePrefix);

  const changeReverseProperties = () => setReverseProperties(!reverseProperties);

  const getContextList = contexts =>
    Object.values(contexts)
      .map((v: any) => v.beans)
      .reduce((acc, e) => ({ ...acc, ...e }));

  const configProps = configuration?.configProps ?? {};

  const env = configuration?.env ?? {};

  return (
    <Container
      maxWidth="xl"
      sx={{
        marginTop: '2rem',
      }}
    >
      <Grid container spacing={2}>
        <Grid item sm={12}>
          <Typography variant="h4">{translate('configuration.title')}</Typography>
        </Grid>
        <Grid item sm={12}>
          <TextField label={translate('configuration.filter')} size="small" fullWidth value={filter} onChange={changeFilter} />
        </Grid>
        <Grid item sm={12}>
          <Typography variant="h6">Spring configuration</Typography>
        </Grid>
        <Grid item sm={12}>
          <TableContainer component={Box} sx={{ width: '100%' }}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell component="th" scope="row" onClick={changeReversePrefix}>
                    {translate('configuration.table.prefix')}
                  </TableCell>
                  <TableCell component="th" scope="row" onClick={changeReverseProperties}>
                    {translate('configuration.table.properties')}
                  </TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {configProps.contexts
                  ? Object.values(getContextList(configProps.contexts))
                      .filter(propsFilterFn)
                      .map((property: any, propIndex) => (
                        <TableRow key={propIndex}>
                          <TableCell
                            sx={{
                              verticalAlign: 'top',
                            }}
                          >
                            {property['prefix']}
                          </TableCell>
                          <TableCell
                            sx={{
                              verticalAlign: 'top',
                            }}
                          >
                            <Table size="small">
                              <TableBody>
                                {Object.keys(property['properties']).map((propKey, index) => (
                                  <TableRow key={index}>
                                    <TableCell sx={{ verticalAlign: 'top', border: 'none' }}>{propKey}</TableCell>
                                    <TableCell sx={{ border: 'none' }}>
                                      <Box display="flex" justifyContent="flex-end">
                                        <Typography
                                          variant="body2"
                                          sx={{
                                            overflowWrap: 'anywhere',
                                            backgroundColor: theme => theme.palette.text.disabled,
                                            color: 'white',
                                            fontWeight: 'bold',
                                            borderRadius: '4px',
                                            padding: '4px',
                                            lineHeight: '1.1',
                                          }}
                                        >
                                          {JSON.stringify(property['properties'][propKey])}
                                        </Typography>
                                      </Box>
                                    </TableCell>
                                  </TableRow>
                                ))}
                              </TableBody>
                            </Table>
                          </TableCell>
                        </TableRow>
                      ))
                  : null}
              </TableBody>
            </Table>
          </TableContainer>
        </Grid>
        {env.propertySources
          ? env.propertySources.map((envKey, envIndex) => (
              <Grid item sm={12} key={envIndex}>
                <Typography variant="h6">{envKey.name}</Typography>
                <TableContainer component={Box}>
                  <Table sx={{ minWidth: 400 }} aria-label="simple table">
                    <TableHead>
                      <TableRow key={envIndex}>
                        <TableCell component="th" scope="row">
                          Property
                        </TableCell>
                        <TableCell component="th" scope="row">
                          Value
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.keys(envKey.properties)
                        .filter(envFilterFn)
                        .map((propKey, propIndex) => (
                          <TableRow key={propIndex}>
                            <TableCell sx={{ verticalAlign: 'top', border: 'none' }}>{propKey}</TableCell>
                            <TableCell sx={{ border: 'none' }}>
                              <Box display="flex" justifyContent="flex-end">
                                <Typography
                                  variant="body2"
                                  sx={{
                                    overflowWrap: 'anywhere',
                                    backgroundColor: theme => theme.palette.text.disabled,
                                    color: 'white',
                                    fontWeight: 'bold',
                                    borderRadius: '4px',
                                    padding: '4px',
                                    lineHeight: '1.1',
                                  }}
                                >
                                  {typeof envKey.properties[propKey].value === 'string'
                                    ? envKey.properties[propKey].value
                                    : JSON.stringify(envKey.properties[propKey].value)}
                                </Typography>
                              </Box>
                            </TableCell>
                          </TableRow>
                        ))}
                    </TableBody>
                  </Table>
                </TableContainer>
              </Grid>
            ))
          : null}
      </Grid>
    </Container>
  );
};

export default ConfigurationPage;
