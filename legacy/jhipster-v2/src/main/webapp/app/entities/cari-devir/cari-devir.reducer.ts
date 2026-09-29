import axios from 'axios';
import { createAction, createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { defaultValue, ICariDevir } from 'app/shared/model/cari-devir.model';
import { createEntitySlice, EntityState, IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

const initialState: EntityState<ICariDevir> = {
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

const apiUrl = 'api/cari-devirs';
const entityName = 'cariDevir';

// Async Actions

export const getEntities = createAsyncThunk(`${entityName}/fetch_entities`, async (queryParams: IQueryParams) => {
  const requestUrl = `${apiUrl}?${Object.keys(queryParams)
    .map(key => `${key}=${queryParams[key]}`)
    .join('&')}`;
  return axios.get<ICariDevir[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  `${entityName}/fetch_entity`,
  async (id: string) => {
    const requestUrl = `${apiUrl}/${id}`;
    return axios.get<ICariDevir>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  `${entityName}/create_entity`,
  async (user: ICariDevir) => await axios.post<ICariDevir>(apiUrl, user),
  {
    serializeError: serializeAxiosError,
  }
);

export const updateEntity = createAsyncThunk(
  `${entityName}/update_entity`,
  async (entity: ICariDevir) => await axios.put<ICariDevir>(apiUrl + `/${entity.id}`, entity),
  {
    serializeError: serializeAxiosError,
  }
);

export const partialUpdateEntity = createAsyncThunk(
  `${entityName}/partial_update_entity`,
  async (entity: ICariDevir, thunkAPI) => {
    return axios.patch<ICariDevir>(`${apiUrl}/${entity.id}`, cleanEntity(entity));
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

export const CariDevirSlice = createEntitySlice<ICariDevir, EntityState<ICariDevir>>({
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

export const { reset } = CariDevirSlice.actions;

// Reducer
export default CariDevirSlice.reducer;
