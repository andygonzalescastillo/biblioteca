import { useState } from 'react';
import { User } from 'lucide-react';
import { useAutores } from './hooks/useAutores';
import { usePaginacion } from '@/shared/hooks/usePaginacion';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';
import { AutorCard } from './components/AutorCard';
import { AutorDialog } from './components/AutorDialog';
import { DialogoConfirmarEliminar } from '@/components/shared/DialogoConfirmarEliminar';
import { FiltrosBusquedaEstado } from '@/components/shared/FiltrosBusquedaEstado';
import { PaginaListado } from '@/components/shared/PaginaListado';
import { AutoresSkeleton } from '@/components/shared/EstadoPagina';

export const AutoresPage = () => {
  const [buscar, setBuscar] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>('todos');
  const { pagina, setPagina, tamanioPagina, setTamanioPagina, manejarCambioFiltro } = usePaginacion();

  const {
    autores,
    totalPages,
    totalElements,
    isLoading,
    isError,
    error,
    isDialogOpen,
    editingAutor,
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
  } = useAutores({ buscar, estadoFilter: filtroEstado, page: pagina, size: tamanioPagina });

  const descripcion = 'Registra, edita, desactiva y gestiona las biografías y datos de los autores literarios.';

  return (
    <PaginaListado
      titulo="Directorio de Autores"
      descripcion={descripcion}
      etiquetaCrear="Nuevo Autor"
      filtros={
        <FiltrosBusquedaEstado
          buscar={buscar}
          alCambiarBuscar={manejarCambioFiltro(setBuscar)}
          filtroEstado={filtroEstado}
          alCambiarFiltroEstado={manejarCambioFiltro(setFiltroEstado)}
          placeholder="Buscar por nombre o biografía..."
        />
      }
      items={autores}
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
      renderizarTarjeta={(autor) => (
        <AutorCard
          key={autor.id}
          autor={autor}
          onEdit={handleOpenEdit}
          onDelete={(id) => setDeletingId(id)}
          onActivate={handleActivate}
        />
      )}
      esqueleto={<AutoresSkeleton />}
      iconoVacio={<User className="h-8 w-8" />}
      tituloVacio="No se encontraron autores"
      descripcionVacio="Intenta cambiar los términos de búsqueda o registra un nuevo autor literario."
      etiquetaAccionVacio="Registrar Autor"
      mensajeError={(error as { message?: string })?.message}
      modales={
        <>
          <AutorDialog
            isOpen={isDialogOpen}
            onOpenChange={handleCloseDialog}
            editingAutor={editingAutor}
            onSave={handleSave}
            isSaving={isSaving}
          />
          <DialogoConfirmarEliminar
            abierto={deletingId !== null}
            alCerrar={() => setDeletingId(null)}
            alConfirmar={() => deletingId !== null && handleDelete(deletingId)}
            pendiente={isDeleting}
            titulo="¿Desactivar Autor?"
            descripcion="El autor quedará inactivo pero podrá reactivarlo luego. Si tiene libros asociados, la operación será impedida."
          />
        </>
      }
    />
  );
};

