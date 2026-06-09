import { api } from '@/core/api/api';
import { API_ENDPOINTS } from '@/core/api/apiEndpoints';
import { API_ORIGIN } from '@/core/api/apiConfig';
import type { ImagenResponse } from '@/shared/types/imagen';

export const imagenService = {
  subir: async (archivo: File) => {
    const formData = new FormData();
    formData.append('archivo', archivo);

    const response = await api.post<ImagenResponse>(API_ENDPOINTS.IMAGENES.BASE, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },

  eliminar: async (id: string) => {
    const response = await api.delete<void>(API_ENDPOINTS.IMAGENES.BY_ID(id));
    return response.data;
  },

  obtenerUrl: (urlAlmacenamiento?: string) => {
    if (!urlAlmacenamiento) return '';
    return `${API_ORIGIN}${urlAlmacenamiento}`;
  },
};
