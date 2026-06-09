import axios from 'axios';
import type { ApiError } from '@/core/types/common';
import { API_BASE_URL } from './apiConfig';
import { traducirCodigoError } from '@/shared/utils/traducirError';

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    let apiError: ApiError = {
      status: 500,
      errorCode: 'INTERNAL_SERVER_ERROR',
      message: traducirCodigoError('INTERNAL_SERVER_ERROR', 'Ha ocurrido un error de conexión con el servidor.'),
      timestamp: new Date().toISOString(),
    };

    if (error.response && error.response.data) {
      const data = error.response.data;
      const errorCode = data.errorCode || 'INTERNAL_SERVER_ERROR';
      apiError = {
        status: error.response.status || data.status || 500,
        errorCode,
        message: traducirCodigoError(errorCode, data.message || 'Error de servidor.'),
        timestamp: data.timestamp || new Date().toISOString(),
        errors: data.errors || undefined,
      };
    } else if (error.request) {
      apiError = {
        status: 0,
        errorCode: 'NETWORK_ERROR',
        message: traducirCodigoError('NETWORK_ERROR', 'No se pudo establecer conexión con el backend de la biblioteca. Verifica que esté en ejecución.'),
        timestamp: new Date().toISOString(),
      };
    }

    return Promise.reject(apiError);
  }
);

