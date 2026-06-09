import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { ConfiguracionPublicaResponse } from '../types/configuracion';

export const configuracionService = {
  obtenerPublica: async () => {
    const response = await api.get<ConfiguracionPublicaResponse>(API_ENDPOINTS.CONFIGURACION.PUBLICA);
    return response.data;
  },
};
