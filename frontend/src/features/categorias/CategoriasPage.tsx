import { useState } from 'react';
import { Tag } from 'lucide-react';
import { useCategorias } from './hooks/useCategorias';
import { usePaginacion } from '@/shared/hooks/usePaginacion';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';
import { CategoriaCard } from './components/CategoriaCard';
import { CategoriaDialog } from './components/CategoriaDialog';
import { DialogoConfirmarEliminar } from '@/components/shared/DialogoConfirmarEliminar';
import { FiltrosBusquedaEstado } from '@/components/shared/FiltrosBusquedaEstado';
import { PaginaListado } from '@/components/shared/PaginaListado';
import { CategoriasSkeleton } from '@/components/shared/EstadoPagina';

export const CategoriasPage = () => {
  const [buscar, setBuscar] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>('todos');
  const { pagina, setPagina, tamanioPagina, setTamanioPagina, manejarCambioFiltro } = usePaginacion();

  const {
    categorias,
    totalPages,
    totalElements,
    isLoading,
    isError,
    error,
    isDialogOpen,
    editingCategoria,
    deletingId,
    setDeletingId,
    handleOpenCreate,
    handleOpenEdit,
    handleCloseDialog,
    handleSave,
    handleDelete,
    handleActivate,
    isSaving,
    isDeleting,
  } = useCategorias({ buscar, estadoFilter: filtroEstado, page: pagina, size: tamanioPagina });

  const descripcion = 'Registra, edita, desactiva y organiza las clasificaciones y géneros de los libros.';

  return (
    <PaginaListado
      titulo="Gestión de Categorías"
      descripcion={descripcion}
      etiquetaCrear="Nueva Categoría"
      filtros={
        <FiltrosBusquedaEstado
          buscar={buscar}
          alCambiarBuscar={manejarCambioFiltro(setBuscar)}
          filtroEstado={filtroEstado}
          alCambiarFiltroEstado={manejarCambioFiltro(setFiltroEstado)}
          placeholder="Buscar por nombre o descripción..."
        />
      }
      items={categorias}
      totalPaginas={totalPages}
      totalElementos={totalElements}
      cargando={isLoading}
      hayError={isError}
      error={error}
      pagina={pagina}
      tamanioPagina={tamanioPagina}
      alCambiarPagina={setPagina}
      alCambiarTamanioPagina={setTamanioPagina}
      alCrear={handleOpenCreate}
      renderizarTarjeta={(cat) => (
        <CategoriaCard
          key={cat.id}
          cat={cat}
          onEdit={handleOpenEdit}
          onDelete={(id) => setDeletingId(id)}
          onActivate={handleActivate}
        />
      )}
      esqueleto={<CategoriasSkeleton />}
      iconoVacio={<Tag className="h-8 w-8" />}
      tituloVacio="No se encontraron categorías"
      descripcionVacio="Intenta cambiar los términos de búsqueda o registra una nueva categoría."
      etiquetaAccionVacio="Nueva Categoría"
      mensajeError={(error as { message?: string })?.message}
      modales={
        <>
          <CategoriaDialog
            isOpen={isDialogOpen}
            onOpenChange={handleCloseDialog}
            editingCategoria={editingCategoria}
            onSave={handleSave}
            isSaving={isSaving}
          />
          <DialogoConfirmarEliminar
            abierto={deletingId !== null}
            alCerrar={() => setDeletingId(null)}
            alConfirmar={() => deletingId !== null && handleDelete(deletingId)}
            pendiente={isDeleting}
            titulo="¿Desactivar Categoría?"
            descripcion="La categoría quedará inactiva pero podrá reactivarla luego. Si tiene libros asociados activos, la operación será impedida."
          />
        </>
      }
    />
  );
};

