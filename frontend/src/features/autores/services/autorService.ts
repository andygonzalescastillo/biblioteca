import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { Page } from '@/core/types/common';
import type { ServicioCrud } from '@/core/types/service';
import type { AutorResponse, AutorRequest } from '../types/autor';

export const autorService: ServicioCrud<AutorResponse, AutorRequest> = {
  obtenerTodos: async (buscar, estado, page = 0, size = 10, sort = 'id,desc') => {
    const params: Record<string, string | number | boolean> = { page, size, sort };
    if (buscar !== undefined && buscar.trim() !== '') params.buscar = buscar.trim();
    if (estado !== undefined) params.estado = estado;

    const response = await api.get<Page<AutorResponse>>(API_ENDPOINTS.AUTORES.BASE, { params });
    return response.data;
  },

  obtenerPorId: async (id) => {
    const response = await api.get<AutorResponse>(API_ENDPOINTS.AUTORES.BY_ID(id));
    return response.data;
  },

  crear: async (request) => {
    const response = await api.post<AutorResponse>(API_ENDPOINTS.AUTORES.BASE, request);
    return response.data;
  },

  actualizar: async (id, request) => {
    const response = await api.put<AutorResponse>(API_ENDPOINTS.AUTORES.BY_ID(id), request);
    return response.data;
  },

  eliminar: async (id) => {
    const response = await api.delete<void>(API_ENDPOINTS.AUTORES.BY_ID(id));
    return response.data;
  },

  activar: async (id) => {
    const response = await api.put<AutorResponse>(API_ENDPOINTS.AUTORES.ACTIVAR(id));
    return response.data;
  },
};
