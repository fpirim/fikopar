import axios from 'axios';
import { createAction, createAsyncThunk, isFulfilled, isPending } from '@reduxjs/toolkit';

import { defaultValue, IUser } from 'app/shared/model/user.model';
import { createEntitySlice, EntityState, IQueryParams, serializeAxiosError } from 'app/shared/reducers/reducer.utils';

interface EntityStateWithAuthorities<T> extends EntityState<T> {
  authorities: string[];
}

const initialState: EntityStateWithAuthorities<IUser> = {
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
  authorities: [],
};

const apiUrl = 'api/users';
const adminUrl = 'api/admin/users';
const entityName = 'userManagement';

export const getRoles = createAsyncThunk(`${entityName}/fetch_roles`, async () => {
  return axios.get<any[]>(`api/authorities`);
});

export const getUsersAsAdmin = createAsyncThunk(`${entityName}/fetch_users_as_admin`, async (query: IQueryParams) => {
  const requestUrl = `${adminUrl}?${Object.keys(query)
    .map(key => `${key}=${query[key]}`)
    .join('&')}`;
  return axios.get<IUser[]>(requestUrl);
});

// Async Actions

export const getEntities = createAsyncThunk(`${entityName}/fetch_entities`, async (queryParams: IQueryParams) => {
  const requestUrl = `${apiUrl}?${Object.keys(queryParams)
    .map(key => `${key}=${queryParams[key]}`)
    .join('&')}`;
  return axios.get<IUser[]>(requestUrl);
});

export const getEntity = createAsyncThunk(
  `${entityName}/fetch_entity`,
  async (id: string) => {
    const requestUrl = `${adminUrl}/${id}`;
    return axios.get<IUser>(requestUrl);
  },
  { serializeError: serializeAxiosError }
);

export const createEntity = createAsyncThunk(
  `${entityName}/create_entity`,
  async (user: IUser) => await axios.post<IUser>(adminUrl, user),
  {
    serializeError: serializeAxiosError,
  }
);

export const updateEntity = createAsyncThunk(`${entityName}/update_entity`, async (user: IUser) => await axios.put<IUser>(adminUrl, user), {
  serializeError: serializeAxiosError,
});

export const deleteEntities = createAsyncThunk(
  `${entityName}/delete_entities`,
  async (ids: any[]) => {
    return await axios.delete<any>(`${adminUrl}`, { data: ids });
  },
  { serializeError: serializeAxiosError }
);

export const resetEntity = createAction(`${entityName}/reset_entity`);

export const UserManagementSlice = createEntitySlice<IUser, EntityStateWithAuthorities<IUser>>({
  name: 'userManagement',
  initialState,
  extraReducers(builder) {
    builder
      .addCase(getRoles.fulfilled, (state, action) => {
        state.authorities = action.payload.data;
      })
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
      .addMatcher(isFulfilled(getEntities, getUsersAsAdmin), (state, action) => {
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
      .addMatcher(isPending(getEntities, getUsersAsAdmin, getEntity), state => {
        state.errorMessage = null;
        state.updateSuccess = false;
        state.loading = true;
      })
      .addMatcher(isPending(createEntity, updateEntity), state => {
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

export const { reset } = UserManagementSlice.actions;

// Reducer
export default UserManagementSlice.reducer;
