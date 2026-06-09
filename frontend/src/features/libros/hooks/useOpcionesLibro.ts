import { useQuery } from '@tanstack/react-query';
import { autorService } from '@/features/autores/services/autorService';
import { categoriaService } from '@/features/categorias/services/categoriaService';

const TIEMPO_CACHE_OPCIONES = 1000 * 60 * 5;

export const useCategoriasActivas = () => {
  return useQuery({
    queryKey: ['categorias-all'],
    queryFn: () => categoriaService.obtenerTodos(undefined, true, 0, 200),
    staleTime: TIEMPO_CACHE_OPCIONES,
  });
};

export const useAutoresActivos = () => {
  return useQuery({
    queryKey: ['autores-all'],
    queryFn: () => autorService.obtenerTodos(undefined, true, 0, 200),
    staleTime: TIEMPO_CACHE_OPCIONES,
  });
};
