import type { ImagenResponse } from '@/shared/types/imagen';

export interface UsuarioResponse {
  id: number;
  nombre: string;
  email: string;
  telefono: string;
  direccion: string;
  fechaRegistro: string;
  foto?: ImagenResponse;
  estado: boolean;
}

export interface UsuarioRequest {
  nombre: string;
  email: string;
  telefono: string;
  direccion: string;
  fotoId?: string;
  estado?: boolean;
}

export interface UsuarioCupoPrestamoResponse {
  usuarioId: number;
  maximoPermitido: number;
  librosEnPosesion: number;
  cupoDisponible: number;
  librosPrestadosIds: number[];
}
