import { useMutation, useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { prestamoService } from '../services/prestamoService';
import type { ApiError } from '@/core/types/common';

interface ParametrosMutacionesPrestamo {
  alRegistrarExito?: () => void;
  alDevolverExito?: () => void;
  alIniciarDevolucion?: (id: number) => void;
  alFinalizarDevolucion?: () => void;
}

export const useMutacionesPrestamo = ({
  alRegistrarExito,
  alDevolverExito,
  alIniciarDevolucion,
  alFinalizarDevolucion,
}: ParametrosMutacionesPrestamo = {}) => {
  const queryClient = useQueryClient();

  const invalidar = () => {
    queryClient.invalidateQueries({ queryKey: ['prestamos'] });
    queryClient.invalidateQueries({ queryKey: ['libros-disponibles'] });
    queryClient.invalidateQueries({ queryKey: ['libros'] });
    queryClient.invalidateQueries({ queryKey: ['usuario-cupo-prestamo'] });
  };

  const mutacionRegistrar = useMutation({
    mutationFn: prestamoService.registrar,
    onSuccess: () => {
      invalidar();
      toast.success('Préstamo registrado correctamente');
      alRegistrarExito?.();
    },
    onError: (err: unknown) =>
      toast.error((err as ApiError).message || 'Error al registrar el préstamo'),
  });

  const mutacionDevolver = useMutation({
    mutationFn: (id: number) => {
      alIniciarDevolucion?.(id);
      return prestamoService.devolver(id);
    },
    onSuccess: () => {
      invalidar();
      toast.success('Devolución registrada correctamente');
      alDevolverExito?.();
      alFinalizarDevolucion?.();
    },
    onError: (err: unknown) => {
      alFinalizarDevolucion?.();
      toast.error((err as ApiError).message || 'Error al registrar la devolución');
    },
  });

  return {
    mutacionRegistrar,
    mutacionDevolver,
    registrarMutation: mutacionRegistrar,
    devolverMutation: mutacionDevolver,
  };
};
