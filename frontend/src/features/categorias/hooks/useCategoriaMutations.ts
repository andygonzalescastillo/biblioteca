import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import type { ApiError } from '@/core/types/common';
import { categoriaService } from '../services/categoriaService';
import type { CategoriaRequest } from '../types/categoria';

interface OpcionesCategoriaMutations {
  alGuardarExito?: () => void;
  alEliminarExito?: () => void;
  alActivarExito?: () => void;
}

export const useCategoriaMutations = ({
  alGuardarExito,
  alEliminarExito,
  alActivarExito,
}: OpcionesCategoriaMutations = {}) => {
  const queryClient = useQueryClient();
  const invalidarCategorias = () => {
    queryClient.invalidateQueries({ queryKey: ['categorias'] });
    queryClient.invalidateQueries({ queryKey: ['categorias-all'] });
  };

  const mutacionCrear = useMutation({
    mutationFn: (data: CategoriaRequest) => categoriaService.crear(data),
    onSuccess: () => {
      invalidarCategorias();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al crear la categoría'),
  });

  const mutacionActualizar = useMutation({
    mutationFn: ({ id, data }: { id: number; data: CategoriaRequest }) => categoriaService.actualizar(id, data),
    onSuccess: () => {
      invalidarCategorias();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al actualizar la categoría'),
  });

  const mutacionEliminar = useMutation({
    mutationFn: (id: number) => categoriaService.eliminar(id),
    onSuccess: () => {
      invalidarCategorias();
      alEliminarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'No se pudo desactivar la categoría'),
  });

  const mutacionActivar = useMutation({
    mutationFn: (id: number) => categoriaService.activar(id),
    onSuccess: () => {
      invalidarCategorias();
      alActivarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'No se pudo reactivar la categoría'),
  });

  return {
    mutacionCrear,
    mutacionActualizar,
    mutacionEliminar,
    mutacionActivar,
  };
};
