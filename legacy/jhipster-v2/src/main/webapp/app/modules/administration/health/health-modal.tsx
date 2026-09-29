import React from 'react';
import { translate } from 'react-jhipster';
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
} from '@mui/material';

const formatDiskSpaceOutput = rawValue => {
  // Should display storage space in an human readable unit
  const val = rawValue / 1073741824;
  if (val > 1) {
    // Value
    return val.toFixed(2) + ' GB';
  }
  return (rawValue / 1048576).toFixed(2) + ' MB';
};

const HealthModal = ({ handleClose, healthObject, showModal }) => {
  const data = healthObject.details || {};
  return (
    <Dialog open={showModal} onClose={handleClose} aria-labelledby="health-dialog-title">
      <DialogTitle id="health-dialog-title">{healthObject.name}</DialogTitle>
      <DialogContent>
        <TableContainer component={Box}>
          <Table sx={{ minWidth: 400 }} aria-label="simple table">
            <TableHead>
              <TableRow>
                <TableCell component="th" scope="row">
                  {translate('health.details.name')}
                </TableCell>
                <TableCell component="th" scope="row">
                  {translate('health.details.value')}
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {Object.keys(data).map((key, index) => (
                <TableRow key={index}>
                  <TableCell>{key}</TableCell>
                  <TableCell>{healthObject.name === 'diskSpace' ? formatDiskSpaceOutput(data[key]) : JSON.stringify(data[key])}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </DialogContent>
      <DialogActions>
        <Button variant="contained" onClick={handleClose} autoFocus>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default HealthModal;
