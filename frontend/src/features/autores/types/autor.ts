import type { ImagenResponse } from '@/shared/types/imagen';

export interface AutorResponse {
  id: number;
  nombre: string;
  biografia: string;
  fechaNacimiento: string;
  foto?: ImagenResponse;
  estado: boolean;
}

export interface AutorRequest {
  nombre: string;
  biografia: string;
  fechaNacimiento?: string;
  fotoId?: string;
  estado?: boolean;
}
