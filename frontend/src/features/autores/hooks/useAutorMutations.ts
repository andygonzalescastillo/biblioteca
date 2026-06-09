import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import type { ApiError } from '@/core/types/common';
import { autorService } from '../services/autorService';
import type { AutorRequest } from '../types/autor';

interface OpcionesAutorMutations {
  alGuardarExito?: () => void;
  alEliminarExito?: () => void;
  alActivarExito?: () => void;
}

export const useAutorMutations = ({
  alGuardarExito,
  alEliminarExito,
  alActivarExito,
}: OpcionesAutorMutations = {}) => {
  const queryClient = useQueryClient();
  const invalidarAutores = () => {
    queryClient.invalidateQueries({ queryKey: ['autores'] });
    queryClient.invalidateQueries({ queryKey: ['autores-all'] });
  };

  const mutacionCrear = useMutation({
    mutationFn: (data: AutorRequest) => autorService.crear(data),
    onSuccess: () => {
      invalidarAutores();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al crear el autor'),
  });

  const mutacionActualizar = useMutation({
    mutationFn: ({ id, data }: { id: number; data: AutorRequest }) =>
      autorService.actualizar(id, data),
    onSuccess: () => {
      invalidarAutores();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al actualizar el autor'),
  });

  const mutacionEliminar = useMutation({
    mutationFn: (id: number) => autorService.eliminar(id),
    onSuccess: () => {
      invalidarAutores();
      alEliminarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'No se pudo desactivar el autor'),
  });

  const mutacionActivar = useMutation({
    mutationFn: (id: number) => autorService.activar(id),
    onSuccess: () => {
      invalidarAutores();
      alActivarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'No se pudo reactivar el autor'),
  });

  return {
    mutacionCrear,
    mutacionActualizar,
    mutacionEliminar,
    mutacionActivar,
  };
};
