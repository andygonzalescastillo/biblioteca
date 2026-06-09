import { useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useEstadoListadoCrud, type ParametrosListadoCrud } from '@/shared/hooks/useEstadoListadoCrud';
import { mapearFiltroEstado } from '@/lib/mapearFiltroEstado';
import { categoriaService } from '../services/categoriaService';
import type { CategoriaResponse, CategoriaRequest } from '../types/categoria';
import { useCategoriaMutations } from './useCategoriaMutations';

export const useCategorias = (params: ParametrosListadoCrud) => {
  const estado = useEstadoListadoCrud<CategoriaResponse>();
  const estadoParam = mapearFiltroEstado(params.estadoFilter);

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['categorias', params.buscar, params.estadoFilter, params.page, params.size],
    queryFn: () =>
      categoriaService.obtenerTodos(params.buscar, estadoParam, params.page, params.size),
    placeholderData: (prev) => prev,
  });

  const { mutacionCrear, mutacionActualizar, mutacionEliminar, mutacionActivar } =
    useCategoriaMutations({
      alGuardarExito: () => {
        toast.success(estado.elementoEditando ? 'Categoría actualizada' : 'Categoría creada con éxito');
        estado.setDialogoAbierto(false);
        estado.setElementoEditando(null);
      },
      alEliminarExito: () => {
        toast.success('Categoría desactivada. Puede reactivarla desde la tarjeta.');
        estado.setIdEliminando(null);
      },
      alActivarExito: () => toast.success('Categoría reactivada con éxito'),
    });

  return {
    items: data?.content ?? [],
    categorias: data?.content ?? [],
    totalPages: data?.page?.totalPages ?? 0,
    totalElements: data?.page?.totalElements ?? 0,
    isLoading,
    isError,
    error,
    isDialogOpen: estado.dialogoAbierto,
    editingItem: estado.elementoEditando,
    editingCategoria: estado.elementoEditando,
    deletingId: estado.idEliminando,
    setDeletingId: estado.setIdEliminando,
    handleOpenCreate: estado.abrirCrear,
    handleOpenEdit: estado.abrirEditar,
    handleCloseDialog: estado.cerrarDialogo,
    handleSave: (payload: CategoriaRequest) => {
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
