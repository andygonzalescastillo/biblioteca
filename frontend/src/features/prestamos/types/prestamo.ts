import type { UsuarioResponse } from '@/features/usuarios/types/usuario';
import type { LibroResponse } from '@/features/libros/types/libro';

export type EstadoPrestamo = 'ACTIVO' | 'DEVUELTO' | 'ATRASADO';

export interface DetallePrestamoResponse {
  id: number;
  libro: LibroResponse;
  cantidad: number;
}

export interface PrestamoResponse {
  id: number;
  usuario: UsuarioResponse;
  fechaPrestamo: string;
  fechaDevolucionLimite: string;
  fechaDevolucionReal?: string;
  estado: EstadoPrestamo;
  detalles: DetallePrestamoResponse[];
}

export interface DetallePrestamoRequest {
  libroId: number;
  cantidad: number;
}

export interface PrestamoRequest {
  usuarioId: number;
  detalles: DetallePrestamoRequest[];
  diasLimite: number;
}
