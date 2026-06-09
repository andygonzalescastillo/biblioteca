import { useQuery } from '@tanstack/react-query';
import { libroService } from '@/features/libros/services/libroService';
import { usuarioService } from '@/features/usuarios/services/usuarioService';

const TIEMPO_CACHE_OPCIONES = 1000 * 60 * 5;
const TIEMPO_CACHE_LIBROS = 1000 * 60 * 2;

export const useUsuariosActivos = () => {
  return useQuery({
    queryKey: ['usuarios-all'],
    queryFn: () => usuarioService.obtenerTodos(undefined, true, 0, 500),
    staleTime: TIEMPO_CACHE_OPCIONES,
  });
}

export const useLibrosDisponibles = () => {
  return useQuery({
    queryKey: ['libros-disponibles'],
    queryFn: () => libroService.obtenerTodos(undefined, true, undefined, undefined, 0, 500),
    staleTime: TIEMPO_CACHE_LIBROS,
  });
}

export const useCupoPrestamoUsuario = (usuarioId?: number) => {
  return useQuery({
    queryKey: ['usuario-cupo-prestamo', usuarioId],
    queryFn: () => usuarioService.obtenerCupoPrestamo(usuarioId!),
    enabled: Boolean(usuarioId),
    staleTime: 1000 * 30,
  });
}
