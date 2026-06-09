import { useState } from 'react';
import { BookOpen } from 'lucide-react';
import { LibroCard } from './components/LibroCard';
import { LibroFilters } from './components/LibroFilters';
import { LibroDialog } from './components/LibroDialog';
import { DialogoConfirmarEliminar } from '@/components/shared/DialogoConfirmarEliminar';
import { PaginaListado } from '@/components/shared/PaginaListado';
import { LibrosSkeleton } from '@/components/shared/EstadoPagina';
import { useLibros } from './hooks/useLibros';
import { usePaginacion } from '@/shared/hooks/usePaginacion';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';

export const LibrosPage = () => {
  const [buscar, setBuscar] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>('todos');
  const [categoriaId, setCategoriaId] = useState<number | ''>('');
  const [autorId, setAutorId] = useState<number | ''>('');
  const { pagina, setPagina, tamanioPagina, setTamanioPagina, manejarCambioFiltro } = usePaginacion();

  const {
    libros,
    totalPages,
    totalElements,
    categorias,
    autores,
    isLoading,
    isError,
    dialogOpen,
    editingLibro,
    deleteId,
    setDeleteId,
    handleOpenCreate,
    handleOpenEdit,
    handleCloseDialog,
    handleSave,
    handleDelete,
    handleActivate,
    isSaving,
    isDeleting,
  } = useLibros({ buscar, estadoFilter: filtroEstado, categoriaId, autorId, page: pagina, pageSize: tamanioPagina });

  const descripcion = 'Registra, edita, desactiva y gestiona el catálogo de libros, stock y categorías asociadas.';

  return (
    <PaginaListado
      titulo="Catálogo de Libros"
      descripcion={descripcion}
      etiquetaCrear="Nuevo Libro"
      filtros={
        <LibroFilters
          buscar={buscar}
          onBuscarChange={manejarCambioFiltro(setBuscar)}
          estadoFilter={filtroEstado}
          onEstadoFilterChange={manejarCambioFiltro(setFiltroEstado)}
          categoriaId={categoriaId}
          onCategoriaIdChange={manejarCambioFiltro(setCategoriaId)}
          autorId={autorId}
          onAutorIdChange={manejarCambioFiltro(setAutorId)}
          categorias={categorias}
          autores={autores}
        />
      }
      items={libros}
      totalPaginas={totalPages}
      totalElementos={totalElements}
      cargando={isLoading}
      hayError={isError}
      pagina={pagina}
      tamanioPagina={tamanioPagina}
      alCambiarPagina={setPagina}
      alCambiarTamanioPagina={setTamanioPagina}
      alCrear={handleOpenCreate}
      renderizarTarjeta={(libro) => (
        <LibroCard
          key={libro.id}
          libro={libro}
          onEdit={handleOpenEdit}
          onDelete={(id) => setDeleteId(id)}
          onActivate={handleActivate}
        />
      )}
      esqueleto={<LibrosSkeleton />}
      iconoVacio={<BookOpen className="h-8 w-8" />}
      tituloVacio="No se encontraron libros"
      descripcionVacio="Ajusta los filtros o agrega el primer libro al catálogo."
      etiquetaAccionVacio="Agregar Libro"
      mensajeError="No se pudo conectar con el servidor. Verifica que el backend esté activo."
      claseGrilla="grid gap-5 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4"
      modales={
        <>
          <LibroDialog
            isOpen={dialogOpen}
            onOpenChange={handleCloseDialog}
            editingLibro={editingLibro}
            categorias={categorias}
            autores={autores}
            onSave={handleSave}
            isSaving={isSaving}
          />
          <DialogoConfirmarEliminar
            abierto={deleteId !== null}
            alCerrar={() => setDeleteId(null)}
            alConfirmar={() => deleteId !== null && handleDelete(deleteId)}
            pendiente={isDeleting}
            titulo="¿Desactivar Libro?"
            descripcion="Esta acción desactivará el libro del catálogo general y no estará disponible para nuevos préstamos. Podrá reactivarlo luego."
          />
        </>
      }
    />
  );
};

