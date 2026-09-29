export interface IResponse<T> {
  error: any;
  message: {
    data: T;
  };
  path: string;
  status: number;
  success: boolean;
  timestamp: string;
}

export interface IGenericResponse<T> {
  error: any;
  message: T;
  path: string;
  status: number;
  success: boolean;
  timestamp: string;
}

export interface IResponsePage<T> {
  error: any;
  message: {
    data: {
      content: T[];
      totalElements: number;
    };
  };
  path: string;
  status: number;
  success: boolean;
  timestamp: string;
}

export interface IResponseList<T> {
  error: any;
  message: {
    data: T[];
  };
  path: string;
  status: number;
  success: boolean;
  timestamp: string;
}

export type IReadOnlyResponseList<T> = IResponseList<T>;
