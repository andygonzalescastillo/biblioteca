import { useState } from 'react';
import { Users } from 'lucide-react';
import { useUsuarios } from './hooks/useUsuarios';
import { usePaginacion } from '@/shared/hooks/usePaginacion';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';
import { UsuarioCard } from './components/UsuarioCard';
import { UsuarioDialog } from './components/UsuarioDialog';
import { DialogoConfirmarEliminar } from '@/components/shared/DialogoConfirmarEliminar';
import { FiltrosBusquedaEstado } from '@/components/shared/FiltrosBusquedaEstado';
import { PaginaListado } from '@/components/shared/PaginaListado';
import { UsuariosSkeleton } from '@/components/shared/EstadoPagina';

export const UsuariosPage = () => {
  const [buscar, setBuscar] = useState('');
  const [filtroEstado, setFiltroEstado] = useState<FiltroEstado>('todos');
  const { pagina, setPagina, tamanioPagina, setTamanioPagina, manejarCambioFiltro } = usePaginacion();

  const {
    usuarios,
    totalPages,
    totalElements,
    isLoading,
    isError,
    dialogOpen,
    editingUsuario,
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
  } = useUsuarios({ buscar, estadoFilter: filtroEstado, page: pagina, pageSize: tamanioPagina });

  const descripcion = 'Registra, edita, desactiva y administra la información detallada de los lectores.';

  return (
    <PaginaListado
      titulo="Directorio de Lectores"
      descripcion={descripcion}
      etiquetaCrear="Nuevo Lector"
      filtros={
        <FiltrosBusquedaEstado
          buscar={buscar}
          alCambiarBuscar={manejarCambioFiltro(setBuscar)}
          filtroEstado={filtroEstado}
          alCambiarFiltroEstado={manejarCambioFiltro(setFiltroEstado)}
          placeholder="Buscar por nombre, email..."
        />
      }
      items={usuarios}
      totalPaginas={totalPages}
      totalElementos={totalElements}
      cargando={isLoading}
      hayError={isError}
      pagina={pagina}
      tamanioPagina={tamanioPagina}
      alCambiarPagina={setPagina}
      alCambiarTamanioPagina={setTamanioPagina}
      alCrear={handleOpenCreate}
      renderizarTarjeta={(usuario) => (
        <UsuarioCard
          key={usuario.id}
          usuario={usuario}
          onEdit={handleOpenEdit}
          onDelete={(id) => setDeleteId(id)}
          onActivate={handleActivate}
        />
      )}
      esqueleto={<UsuariosSkeleton />}
      iconoVacio={<Users className="h-8 w-8" />}
      tituloVacio="No se encontraron lectores"
      descripcionVacio="Ajusta los filtros o registra el primer lector del sistema."
      etiquetaAccionVacio="Registrar Lector"
      mensajeError="No se pudo conectar con el servidor. Verifica que el backend esté activo."
      claseGrilla="grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
      modales={
        <>
          <UsuarioDialog
            isOpen={dialogOpen}
            onOpenChange={handleCloseDialog}
            editingUsuario={editingUsuario}
            onSave={handleSave}
            isSaving={isSaving}
          />
          <DialogoConfirmarEliminar
            abierto={deleteId !== null}
            alCerrar={() => setDeleteId(null)}
            alConfirmar={() => deleteId !== null && handleDelete(deleteId)}
            pendiente={isDeleting}
            titulo="¿Desactivar Lector?"
            descripcion="Esta acción desactivará al usuario del sistema. No podrá realizar nuevos préstamos mientras esté inactivo. Podrá reactivarlo luego."
          />
        </>
      }
    />
  );
};

