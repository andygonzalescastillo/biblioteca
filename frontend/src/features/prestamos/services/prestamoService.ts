import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { Page } from '@/core/types/common';
import type { PrestamoResponse, PrestamoRequest, EstadoPrestamo } from '../types/prestamo';

export const prestamoService = {
  obtenerTodos: async (
    usuarioId?: number,
    estado?: EstadoPrestamo,
    page = 0,
    size = 10,
    sort = 'id,desc'
  ) => {
    const params: Record<string, string | number | boolean> = { page, size, sort };
    if (usuarioId !== undefined && usuarioId !== null) params.usuarioId = usuarioId;
    if (estado !== undefined) params.estado = estado;

    const response = await api.get<Page<PrestamoResponse>>(API_ENDPOINTS.PRESTAMOS.BASE, { params });
    return response.data;
  },

  obtenerPorId: async (id: number) => {
    const response = await api.get<PrestamoResponse>(API_ENDPOINTS.PRESTAMOS.BY_ID(id));
    return response.data;
  },

  registrar: async (request: PrestamoRequest) => {
    const response = await api.post<PrestamoResponse>(API_ENDPOINTS.PRESTAMOS.BASE, request);
    return response.data;
  },

  devolver: async (id: number) => {
    const response = await api.put<PrestamoResponse>(API_ENDPOINTS.PRESTAMOS.DEVOLUCION(id));
    return response.data;
  },
};
