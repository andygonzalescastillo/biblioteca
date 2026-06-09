import type { ImagenResponse } from '@/shared/types/imagen';
import { imagenService } from '@/shared/services/imagenService';

export const obtenerUrlPreviewImagen = (imagen?: ImagenResponse | null): string => {
  if (!imagen?.urlAlmacenamiento) return '';
  return imagenService.obtenerUrl(imagen.urlAlmacenamiento);
};
