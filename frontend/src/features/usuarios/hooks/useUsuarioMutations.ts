import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import type { ApiError } from '@/core/types/common';
import { usuarioService } from '../services/usuarioService';
import type { UsuarioRequest } from '../types/usuario';

interface OpcionesUsuarioMutations {
  alGuardarExito?: () => void;
  alEliminarExito?: () => void;
  alActivarExito?: () => void;
}

export const useUsuarioMutations = ({
  alGuardarExito,
  alEliminarExito,
  alActivarExito,
}: OpcionesUsuarioMutations = {}) => {
  const queryClient = useQueryClient();
  const invalidarUsuarios = () => queryClient.invalidateQueries({ queryKey: ['usuarios'] });

  const mutacionCrear = useMutation({
    mutationFn: (data: UsuarioRequest) => usuarioService.crear(data),
    onSuccess: () => {
      invalidarUsuarios();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al registrar el lector'),
  });

  const mutacionActualizar = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UsuarioRequest }) =>
      usuarioService.actualizar(id, data),
    onSuccess: () => {
      invalidarUsuarios();
      alGuardarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al actualizar el lector'),
  });

  const mutacionEliminar = useMutation({
    mutationFn: (id: number) => usuarioService.eliminar(id),
    onSuccess: () => {
      invalidarUsuarios();
      alEliminarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al desactivar el lector'),
  });

  const mutacionActivar = useMutation({
    mutationFn: (id: number) => usuarioService.activar(id),
    onSuccess: () => {
      invalidarUsuarios();
      alActivarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al reactivar el lector'),
  });

  return {
    mutacionCrear,
    mutacionActualizar,
    mutacionEliminar,
    mutacionActivar,
  };
}
