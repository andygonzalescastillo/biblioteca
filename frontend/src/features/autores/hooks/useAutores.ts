import { useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useEstadoListadoCrud, type ParametrosListadoCrud } from '@/shared/hooks/useEstadoListadoCrud';
import { mapearFiltroEstado } from '@/lib/mapearFiltroEstado';
import { autorService } from '../services/autorService';
import type { AutorResponse, AutorRequest } from '../types/autor';
import { useAutorMutations } from './useAutorMutations';

export const useAutores = (params: ParametrosListadoCrud) => {
  const estado = useEstadoListadoCrud<AutorResponse>();
  const estadoParam = mapearFiltroEstado(params.estadoFilter);

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['autores', params.buscar, params.estadoFilter, params.page, params.size],
    queryFn: () => autorService.obtenerTodos(params.buscar, estadoParam, params.page, params.size),
    placeholderData: (prev) => prev,
  });

  const { mutacionCrear, mutacionActualizar, mutacionEliminar, mutacionActivar } =
    useAutorMutations({
      alGuardarExito: () => {
        toast.success(estado.elementoEditando ? 'Autor actualizado con éxito' : 'Autor creado con éxito');
        estado.setDialogoAbierto(false);
        estado.setElementoEditando(null);
      },
      alEliminarExito: () => {
        toast.success('Autor desactivado. Puede reactivarlo desde la tarjeta.');
        estado.setIdEliminando(null);
      },
      alActivarExito: () => toast.success('Autor reactivado con éxito'),
    });

  return {
    items: data?.content ?? [],
    autores: data?.content ?? [],
    totalPages: data?.page?.totalPages ?? 0,
    totalElements: data?.page?.totalElements ?? 0,
    isLoading,
    isError,
    error,
    isDialogOpen: estado.dialogoAbierto,
    editingItem: estado.elementoEditando,
    editingAutor: estado.elementoEditando,
    deletingId: estado.idEliminando,
    setDeletingId: estado.setIdEliminando,
    handleOpenCreate: estado.abrirCrear,
    handleOpenEdit: estado.abrirEditar,
    handleCloseDialog: estado.cerrarDialogo,
    handleSave: (payload: AutorRequest) => {
      if (estado.elementoEditando) {
        mutacionActualizar.mutate({ id: estado.elementoEditando.id, data: payload });
      } else {
        mutacionCrear.mutate(payload);
      }
    },
    handleDelete: (id: number) => mutacionEliminar.mutate(id),
    handleActivate: (id: number) => mutacionActivar.mutate(id),
    isSaving: mutacionCrear.isPending || mutacionActualizar.isPending,
    isDeleting: mutacionEliminar.isPending,
  };
};
