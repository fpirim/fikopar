import {
  ActionReducerMapBuilder,
  AnyAction,
  AsyncThunk,
  createSlice,
  SerializedError,
  SliceCaseReducers,
  ValidateSliceCaseReducers,
} from '@reduxjs/toolkit';
import { AxiosError } from 'axios';

/**
 * Model for redux actions with pagination
 */
export type IQueryParams = { query?: string; page?: number; size?: number; sort?: string };

export type IListQueryParams = { query?: string; sort?: string };

/**
 * Useful types for working with actions
 */
type GenericAsyncThunk = AsyncThunk<unknown, unknown, any>;
export type PendingAction = ReturnType<GenericAsyncThunk['pending']>;
export type RejectedAction = ReturnType<GenericAsyncThunk['rejected']>;
export type FulfilledAction = ReturnType<GenericAsyncThunk['fulfilled']>;

/**
 * Check if the async action type is rejected
 */
export function isRejectedAction(action: AnyAction) {
  return action.type.endsWith('/rejected');
}

/**
 * Check if the async action type is pending
 */
export function isPendingAction(action: AnyAction) {
  return action.type.endsWith('/pending');
}

/**
 * Check if the async action type is completed
 */
export function isFulfilledAction(action: AnyAction) {
  return action.type.endsWith('/fulfilled');
}

const commonErrorProperties: Array<keyof SerializedError> = ['name', 'message', 'stack', 'code'];

/**
 * serialize function used for async action errors,
 * since the default function from Redux Toolkit strips useful info from axios errors
 */
export const serializeAxiosError = (value: any): AxiosError | SerializedError => {
  if (typeof value === 'object' && value !== null) {
    if (value.isAxiosError) {
      return value;
    } else {
      const simpleError: SerializedError = {};
      for (const property of commonErrorProperties) {
        if (typeof value[property] === 'string') {
          simpleError[property] = value[property];
        }
      }

      return simpleError;
    }
  }
  return { message: String(value) };
};

export interface FieldError<T> {
  objectName: string;
  field: T;
  message: string;
  parameters: any;
}

export interface EntityState<T> {
  loading: boolean;
  fieldErrors?: FieldError<keyof T>[];
  errorMessage: string | null;
  entities: ReadonlyArray<T>;
  entity: T;
  entityLoading: boolean;
  links?: any;
  updating: boolean;
  totalItems?: number;
  updateSuccess: boolean;
  deleting: boolean;
  deleteSuccess: boolean;
}

/**
 * A wrapper on top of createSlice from Redux Toolkit to extract
 * common reducers and matchers used by entities
 */
export const createEntitySlice = <T, E extends EntityState<T>>({
  name = '',
  initialState,
  reducers,
  extraReducers,
  skipRejectionHandling,
}: {
  name: string;
  initialState: E;
  reducers?: ValidateSliceCaseReducers<E, SliceCaseReducers<E>>;
  extraReducers?: (builder: ActionReducerMapBuilder<E>) => void;
  skipRejectionHandling?: boolean;
}) => {
  return createSlice({
    name,
    initialState,
    reducers: {
      /**
       * Reset the entity state to initial state
       */
      reset() {
        return initialState;
      },
      ...reducers,
    },
    extraReducers(builder) {
      extraReducers(builder);
      /*
       * Common rejection logic is handled here.
       * If you want to add your own rejcetion logic, pass `skipRejectionHandling: true`
       * while calling `createEntitySlice`
       * */
      if (!skipRejectionHandling) {
        builder.addMatcher(isRejectedAction, (state, action) => {
          state.deleting = false;
          state.entityLoading = false;
          state.loading = false;
          state.updating = false;
          state.updateSuccess = false;
          state.errorMessage = action.error.message;
          state.fieldErrors = action.error.response?.data?.fieldErrors?.reduce((acc, fieldError) => {
            const params =
              fieldError.parameters?.reduce(
                (acc, param, index) => ({
                  ...acc,
                  [`${index}`]: param,
                }),
                {}
              ) || {};
            acc.push({ ...fieldError, parameters: params });
            return acc;
          }, []);
        });
      }
    },
  });
};
