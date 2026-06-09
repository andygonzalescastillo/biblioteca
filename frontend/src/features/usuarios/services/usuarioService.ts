import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { Page } from '@/core/types/common';
import type { ServicioCrud } from '@/core/types/service';
import type { UsuarioResponse, UsuarioRequest, UsuarioCupoPrestamoResponse } from '../types/usuario';

export const usuarioService = {
  obtenerTodos: async (buscar, estado, page = 0, size = 10, sort = 'id,desc') => {
    const params: Record<string, string | number | boolean> = { page, size, sort };
    if (buscar !== undefined && buscar.trim() !== '') params.buscar = buscar.trim();
    if (estado !== undefined) params.estado = estado;

    const response = await api.get<Page<UsuarioResponse>>(API_ENDPOINTS.USUARIOS.BASE, { params });
    return response.data;
  },

  obtenerPorId: async (id) => {
    const response = await api.get<UsuarioResponse>(API_ENDPOINTS.USUARIOS.BY_ID(id));
    return response.data;
  },

  crear: async (request) => {
    const response = await api.post<UsuarioResponse>(API_ENDPOINTS.USUARIOS.BASE, request);
    return response.data;
  },

  actualizar: async (id, request) => {
    const response = await api.put<UsuarioResponse>(API_ENDPOINTS.USUARIOS.BY_ID(id), request);
    return response.data;
  },

  eliminar: async (id) => {
    const response = await api.delete<void>(API_ENDPOINTS.USUARIOS.BY_ID(id));
    return response.data;
  },

  activar: async (id) => {
    const response = await api.put<UsuarioResponse>(API_ENDPOINTS.USUARIOS.ACTIVAR(id));
    return response.data;
  },

  obtenerPorEmail: async (email: string) => {
    const response = await api.get<UsuarioResponse>(API_ENDPOINTS.USUARIOS.BY_EMAIL(email));
    return response.data;
  },

  obtenerCupoPrestamo: async (id: number) => {
    const response = await api.get<UsuarioCupoPrestamoResponse>(API_ENDPOINTS.USUARIOS.CUPO_PRESTAMO(id));
    return response.data;
  },
} satisfies ServicioCrud<UsuarioResponse, UsuarioRequest> & {
  obtenerPorEmail: (email: string) => Promise<UsuarioResponse>;
  obtenerCupoPrestamo: (id: number) => Promise<UsuarioCupoPrestamoResponse>;
};
