import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { Page } from '@/core/types/common';
import type { ServicioCrud } from '@/core/types/service';
import type { CategoriaResponse, CategoriaRequest } from '../types/categoria';

export const categoriaService: ServicioCrud<CategoriaResponse, CategoriaRequest> = {
  obtenerTodos: async (buscar, estado, page = 0, size = 10, sort = 'id,desc') => {
    const params: Record<string, string | number | boolean> = { page, size, sort };
    if (buscar !== undefined && buscar.trim() !== '') params.buscar = buscar.trim();
    if (estado !== undefined) params.estado = estado;

    const response = await api.get<Page<CategoriaResponse>>(API_ENDPOINTS.CATEGORIAS.BASE, { params });
    return response.data;
  },

  obtenerPorId: async (id) => {
    const response = await api.get<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.BY_ID(id));
    return response.data;
  },

  crear: async (request) => {
    const response = await api.post<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.BASE, request);
    return response.data;
  },

  actualizar: async (id, request) => {
    const response = await api.put<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.BY_ID(id), request);
    return response.data;
  },

  eliminar: async (id) => {
    const response = await api.delete<void>(API_ENDPOINTS.CATEGORIAS.BY_ID(id));
    return response.data;
  },

  activar: async (id) => {
    const response = await api.put<CategoriaResponse>(API_ENDPOINTS.CATEGORIAS.ACTIVAR(id));
    return response.data;
  },
};
