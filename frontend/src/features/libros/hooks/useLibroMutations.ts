import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import type { ApiError } from '@/core/types/common';
import { libroService } from '../services/libroService';
import type { LibroRequest } from '../types/libro';

interface OpcionesLibroMutations {
  alGuardarExito?: () => void;
  alEliminarExito?: () => void;
  alActivarExito?: () => void;
}

export const useLibroMutations = ({
  alGuardarExito,
  alEliminarExito,
  alActivarExito,
}: OpcionesLibroMutations = {}) => {
  const queryClient = useQueryClient();
  const invalidarLibros = () => {
    queryClient.invalidateQueries({ queryKey: ['libros'] });
    queryClient.invalidateQueries({ queryKey: ['libros-disponibles'] });
  };

  const mutacionCrear = useMutation({
    mutationFn: (data: LibroRequest) => libroService.crear(data),
    onSuccess: () => {
      invalidarLibros();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al crear el libro'),
  });

  const mutacionActualizar = useMutation({
    mutationFn: ({ id, data }: { id: number; data: LibroRequest }) =>
      libroService.actualizar(id, data),
    onSuccess: () => {
      invalidarLibros();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al actualizar el libro'),
  });

  const mutacionEliminar = useMutation({
    mutationFn: (id: number) => libroService.eliminar(id),
    onSuccess: () => {
      invalidarLibros();
      alEliminarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al desactivar el libro'),
  });

  const mutacionActivar = useMutation({
    mutationFn: (id: number) => libroService.activar(id),
    onSuccess: () => {
      invalidarLibros();
      alActivarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al reactivar el libro'),
  });

  return {
    mutacionCrear,
    mutacionActualizar,
    mutacionEliminar,
    mutacionActivar,
  };
}
