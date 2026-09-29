import axios from 'axios';
import { createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';
import { defaultValue, ICari } from 'app/shared/model/cari.model';
import { createEntitySlice, EntityState, IListQueryParams } from 'app/shared/reducers/reducer.utils';

const initialState: EntityState<ICari> = {
  loading: false,
  errorMessage: null,
  entities: [],
  entity: defaultValue,
  entityLoading: false,
  updating: false,
  totalItems: 0,
  updateSuccess: false,
  deleting: false,
  deleteSuccess: false,
};

const apiUrl = 'api/caris';
const entityName = 'cari';

// Async Actions

export const getEntities = createAsyncThunk(`${entityName}/fetch_entities`, async (queryListParams: IListQueryParams) => {
  const requestUrl = `${apiUrl}?${Object.keys(queryListParams)
    .map(key => `${key}=${queryListParams[key]}`)
    .join('&')}`;
  return axios.get<ICari[]>(requestUrl);
});

export const getExcelReport = createAsyncThunk(`${entityName}/report_entities`, async (queryListParams: IListQueryParams) => {
  const requestUrl = `${apiUrl}/report?${Object.keys(queryListParams)
    .map(key => `${key}=${queryListParams[key]}`)
    .join('&')}`;
  return axios.get<Blob>(requestUrl, { responseType: 'blob' });
});

export const CariSlice = createEntitySlice<ICari, EntityState<ICari>>({
  name: entityName,
  initialState,
  extraReducers(builder) {
    builder
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        state.loading = false;
        state.entities = action.payload.data;
        state.totalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(getExcelReport), (state, action) => {
        state.loading = false;
      })
      .addMatcher(isPending(getEntities, getExcelReport), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      });
  },
});

export const { reset } = CariSlice.actions;

// Reducer
export default CariSlice.reducer;
