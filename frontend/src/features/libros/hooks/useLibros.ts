import { useQuery } from '@tanstack/react-query';
import { toast } from 'sonner';
import { libroService } from '../services/libroService';
import { useEstadoListadoCrud } from '@/shared/hooks/useEstadoListadoCrud';
import { mapearFiltroEstado, type FiltroEstado } from '@/lib/mapearFiltroEstado';
import type { LibroResponse, LibroRequest } from '../types/libro';
import { useLibroMutations } from './useLibroMutations';
import { useAutoresActivos, useCategoriasActivas } from './useOpcionesLibro';

export interface ParametrosLibros {
  buscar: string;
  estadoFilter: FiltroEstado;
  categoriaId: number | '';
  autorId: number | '';
  page: number;
  pageSize: number;
}

const configLibros = {
  avisos: {
    creado: 'Libro creado correctamente',
    actualizado: 'Libro actualizado correctamente',
    eliminado: 'Libro desactivado. Puede reactivarlo desde la tarjeta.',
    activado: 'Libro reactivado en el catálogo',
  },
} as const;

export const useLibros = ({
  buscar,
  estadoFilter,
  categoriaId,
  autorId,
  page,
  pageSize,
}: ParametrosLibros) => {
  const estado = useEstadoListadoCrud<LibroResponse>();
  const estadoParam = mapearFiltroEstado(estadoFilter);

  const { data: librosPage, isLoading, isError, error } = useQuery({
    queryKey: ['libros', buscar, estadoParam, categoriaId, autorId, page, pageSize],
    queryFn: () =>
      libroService.obtenerTodos(
        buscar || undefined,
        estadoParam,
        categoriaId !== '' ? categoriaId : undefined,
        autorId !== '' ? autorId : undefined,
        page,
        pageSize,
      ),
    placeholderData: (prev) => prev,
  });

  const { data: categoriasPage } = useCategoriasActivas();
  const { data: autoresPage } = useAutoresActivos();

  const { mutacionCrear, mutacionActualizar, mutacionEliminar, mutacionActivar } =
    useLibroMutations({
      alGuardarExito: () => {
        toast.success(
          estado.elementoEditando ? configLibros.avisos.actualizado : configLibros.avisos.creado,
        );
        estado.setDialogoAbierto(false);
        estado.setElementoEditando(null);
      },
      alEliminarExito: () => {
        toast.success(configLibros.avisos.eliminado);
        estado.setIdEliminando(null);
      },
      alActivarExito: () => toast.success(configLibros.avisos.activado),
    });

  return {
    libros: librosPage?.content ?? [],
    totalPages: librosPage?.page?.totalPages ?? 0,
    totalElements: librosPage?.page?.totalElements ?? 0,
    categorias: categoriasPage?.content ?? [],
    autores: autoresPage?.content ?? [],
    isLoading,
    isError,
    error,
    dialogOpen: estado.dialogoAbierto,
    editingLibro: estado.elementoEditando,
    deleteId: estado.idEliminando,
    setDeleteId: estado.setIdEliminando,
    handleOpenCreate: estado.abrirCrear,
    handleOpenEdit: estado.abrirEditar,
    handleCloseDialog: estado.cerrarDialogo,
    handleSave: (data: LibroRequest) => {
      if (estado.elementoEditando) {
        mutacionActualizar.mutate({ id: estado.elementoEditando.id, data });
      } else {
        mutacionCrear.mutate(data);
      }
    },
    handleDelete: (id: number) => mutacionEliminar.mutate(id),
    handleActivate: (id: number) => mutacionActivar.mutate(id),
    isSaving: mutacionCrear.isPending || mutacionActualizar.isPending,
    isDeleting: mutacionEliminar.isPending,
  };
}
