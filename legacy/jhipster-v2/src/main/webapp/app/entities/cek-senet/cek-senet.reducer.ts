import axios from 'axios';
import { createAction, createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { defaultValue, ICekSenet } from 'app/shared/model/cek-senet.model';
import { createEntitySlice, EntityState, IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';
import { EvrakTipi } from 'app/shared/model/enumerations/evrak-tipi.model';
import { DepoTipi } from 'app/shared/model/enumerations/depo-tipi.model';

const initialState: EntityState<ICekSenet> = {
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

const apiUrl = 'api/cek-senets';
const entityName = 'cekSenet';

// Async Actions

export const getGeneratedCekSenetNo = createAsyncThunk(
  `${entityName}/generate`,
  async (queryParams: { evraktipi?: EvrakTipi; depotipi?: DepoTipi; tarihi?: string }) => {
    const requestUrl = `${apiUrl}/generate?${Object.keys(queryParams)
      .filter(key => queryParams[key] !== undefined && queryParams[key] !== null)
      .map(key => `${key}=${queryParams[key]}`)
      .join('&')}`;
    return axios.get<string>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const getEntities = createAsyncThunk(`${entityName}/fetch_entities`, async (queryParams: IQueryParams) => {
  const requestUrl = `${apiUrl}?${Object.keys(queryParams)
    .map(key => `${key}=${queryParams[key]}`)
    .join('&')}`;
  return axios.get<ICekSenet[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  `${entityName}/fetch_entity`,
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<ICekSenet>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  `${entityName}/create_entity`,
  async (user: ICekSenet) => await axios.post<ICekSenet>(apiUrl, user),
  {
    serializeError: serializeAxiosError,
  }
);

export const updateEntity = createAsyncThunk(
  `${entityName}/update_entity`,
  async (entity: ICekSenet) => await axios.put<ICekSenet>(apiUrl + `/${entity.id}`, entity),
  {
    serializeError: serializeAxiosError,
  }
);

export const partialUpdateEntity = createAsyncThunk(
  `${entityName}/partial_update_entity`,
  async (entity: ICekSenet, thunkAPI) => {
    return axios.patch<ICekSenet>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
  },
  { serializeError: serializeAxiosError }
);

export const deleteEntities = createAsyncThunk(
  `${entityName}/delete_entities`,
  async (ids: any[]) => {
    return await axios.delete<any>(`${apiUrl}`, { data: ids });
  },
  { serializeError: serializeAxiosError }
);

export const resetEntity = createAction(`${entityName}/reset_entity`);

export const CekSenetSlice = createEntitySlice<ICekSenet, EntityState<ICekSenet>>({
  name: entityName,
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getEntity.fulfilled, (state, action) => {
        state.entityLoading = false;
        state.entity = action.payload.data;
      })
      .addCase(deleteEntities.fulfilled, state => {
        state.deleting = false;
        state.deleteSuccess = true;
      })
      .addCase(resetEntity, state => {
        state.entityLoading = false;
        state.entity = defaultValue;
      })
      .addMatcher(isFulfilled(getEntities), (state, action) => {
        state.loading = false;
        state.entities = action.payload.data;
        state.totalItems = parseInt(action.payload.headers['x-total-count'], 10);
      })
      .addMatcher(isFulfilled(createEntity, updateEntity), (state, action) => {
        state.updating = false;
        state.entityLoading = false;
        state.loading = false;
        state.updateSuccess = true;
        state.entity = action.payload.data;
      })
      .addMatcher(isPending(getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.entityLoading = true;
      })
      .addMatcher(isPending(getEntities, getEntity, partialUpdateEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity, partialUpdateEntity), state => {
        state.errorMessage = null;
        state.fieldErrors = null;
        state.updateSuccess = false;
        state.updating = true;
      })
      .addMatcher(isPending(deleteEntities), state => {
        state.errorMessage = null;
        state.deleteSuccess = false;
        state.deleting = true;
      });
  },
});

export const { reset } = CekSenetSlice.actions;

// Reducer
export default CekSenetSlice.reducer;
