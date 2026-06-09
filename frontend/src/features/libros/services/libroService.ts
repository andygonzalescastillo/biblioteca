import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { Page } from '@/core/types/common';
import type { ServicioMutacionesCrud } from '@/core/types/service';
import type { LibroResponse, LibroRequest } from '../types/libro';

export const libroService = {
  obtenerTodos: async (
    buscar?: string,
    estado?: boolean,
    categoriaId?: number,
    autorId?: number,
    page = 0,
    size = 10,
    sort = 'id,desc',
  ) => {
    const params: Record<string, string | number | boolean> = { page, size, sort };
    if (buscar !== undefined && buscar.trim() !== '') params.buscar = buscar.trim();
    if (estado !== undefined) params.estado = estado;
    if (categoriaId !== undefined && categoriaId !== null) params.categoriaId = categoriaId;
    if (autorId !== undefined && autorId !== null) params.autorId = autorId;

    const response = await api.get<Page<LibroResponse>>(API_ENDPOINTS.LIBROS.BASE, { params });
    return response.data;
  },

  obtenerPorId: async (id: number) => {
    const response = await api.get<LibroResponse>(API_ENDPOINTS.LIBROS.BY_ID(id));
    return response.data;
  },

  crear: async (request: LibroRequest) => {
    const response = await api.post<LibroResponse>(API_ENDPOINTS.LIBROS.BASE, request);
    return response.data;
  },

  actualizar: async (id: number, request: LibroRequest) => {
    const response = await api.put<LibroResponse>(API_ENDPOINTS.LIBROS.BY_ID(id), request);
    return response.data;
  },

  eliminar: async (id: number) => {
    const response = await api.delete<void>(API_ENDPOINTS.LIBROS.BY_ID(id));
    return response.data;
  },

  activar: async (id: number) => {
    const response = await api.put<LibroResponse>(API_ENDPOINTS.LIBROS.ACTIVAR(id));
    return response.data;
  },

  obtenerPorIsbn: async (isbn: string) => {
    const response = await api.get<LibroResponse>(API_ENDPOINTS.LIBROS.BY_ISBN(isbn));
    return response.data;
  },
} satisfies ServicioMutacionesCrud<LibroResponse, LibroRequest> & {
  obtenerTodos: (
    buscar?: string,
    estado?: boolean,
    categoriaId?: number,
    autorId?: number,
    page?: number,
    size?: number,
    sort?: string,
  ) => Promise<Page<LibroResponse>>;
  obtenerPorId: (id: number) => Promise<LibroResponse>;
  obtenerPorIsbn: (isbn: string) => Promise<LibroResponse>;
};
