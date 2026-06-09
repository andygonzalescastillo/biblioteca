import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import type { DashboardResumenResponse } from '../types/dashboard';

export const dashboardService = {
  obtenerResumen: async () => {
    const response = await api.get<DashboardResumenResponse>(API_ENDPOINTS.DASHBOARD.RESUMEN);
    return response.data;
  },
};
