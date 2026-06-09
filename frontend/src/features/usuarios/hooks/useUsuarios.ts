import { useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { useEstadoListadoCrud, type ParametrosListadoCrud } from '@/shared/hooks/useEstadoListadoCrud';
import { mapearFiltroEstado } from '@/lib/mapearFiltroEstado';
import { usuarioService } from '../services/usuarioService';
import type { UsuarioResponse, UsuarioRequest } from '../types/usuario';
import { useUsuarioMutations } from './useUsuarioMutations';

export interface ParametrosUsuarios extends Omit<ParametrosListadoCrud, 'size'> {
  pageSize: number;
}

export const useUsuarios = ({ buscar, estadoFilter, page, pageSize }: ParametrosUsuarios) => {
  const estado = useEstadoListadoCrud<UsuarioResponse>();
  const estadoParam = mapearFiltroEstado(estadoFilter);

  const { data, isLoading, isError, error } = useQuery({
    queryKey: ['usuarios', buscar, estadoFilter, page, pageSize],
    queryFn: () => usuarioService.obtenerTodos(buscar || undefined, estadoParam, page, pageSize),
    placeholderData: (prev) => prev,
  });

  const { mutacionCrear, mutacionActualizar, mutacionEliminar, mutacionActivar } =
    useUsuarioMutations({
      alGuardarExito: () => {
        toast.success(
          estado.elementoEditando
            ? 'Lector actualizado correctamente'
            : 'Lector registrado correctamente',
        );
        estado.setDialogoAbierto(false);
        estado.setElementoEditando(null);
      },
      alEliminarExito: () => {
        toast.success('Lector desactivado. Puede reactivarlo desde la tarjeta.');
        estado.setIdEliminando(null);
      },
      alActivarExito: () => toast.success('Lector reactivado correctamente'),
    });

  return {
    items: data?.content ?? [],
    usuarios: data?.content ?? [],
    totalPages: data?.page?.totalPages ?? 0,
    totalElements: data?.page?.totalElements ?? 0,
    isLoading,
    isError,
    error,
    isDialogOpen: estado.dialogoAbierto,
    dialogOpen: estado.dialogoAbierto,
    editingUsuario: estado.elementoEditando,
    deleteId: estado.idEliminando,
    setDeleteId: estado.setIdEliminando,
    handleOpenCreate: estado.abrirCrear,
    handleOpenEdit: estado.abrirEditar,
    handleCloseDialog: estado.cerrarDialogo,
    handleSave: (payload: UsuarioRequest) => {
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
}
