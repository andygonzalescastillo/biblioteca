import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BookMarked } from 'lucide-react';
import { usePrestamos } from './hooks/usePrestamos';
import type { EstadoPrestamo } from './types/prestamo';
import { PrestamoCard } from './components/PrestamoCard';
import { PrestamoFilters } from './components/PrestamoFilters';
import { PaginaListado } from '@/components/shared/PaginaListado';
import { PrestamosSkeleton } from '@/components/shared/EstadoPagina';
import { usePaginacion } from '@/shared/hooks/usePaginacion';
import { APP_ROUTES } from '@/shared/constants/appRoutes';
import { OPCIONES_TAMANIO_PAGINA_PRESTAMOS, TAMANIO_PAGINA_PRESTAMOS_POR_DEFECTO, } from '@/features/prestamos/constants/paginacionPrestamos';

export const PrestamosPage = () => {
  const navigate = useNavigate();
  const [buscarUsuario, setBuscarUsuario] = useState('');
  const [estadoFilter, setEstadoFilter] = useState<EstadoPrestamo | 'TODOS'>('TODOS');
  const { pagina, setPagina, tamanioPagina, setTamanioPagina, manejarCambioFiltro } = usePaginacion(
    TAMANIO_PAGINA_PRESTAMOS_POR_DEFECTO,
  );

  const {
    filteredPrestamos,
    totalPages,
    totalElements,
    isLoading,
    isError,
    handleDevolver,
    isDevolviendo,
  } = usePrestamos({ buscarUsuario, estadoFilter, page: pagina, pageSize: tamanioPagina });

  const descripcion = 'Registra nuevos préstamos de libros, realiza el seguimiento y procesa las devoluciones de manera eficiente.';

  return (
    <PaginaListado
      titulo="Registro de Préstamos"
      descripcion={descripcion}
      etiquetaCrear="Nuevo Préstamo"
      filtros={
        <PrestamoFilters
          buscarUsuario={buscarUsuario}
          onBuscarUsuarioChange={manejarCambioFiltro(setBuscarUsuario)}
          estadoFilter={estadoFilter}
          onEstadoFilterChange={manejarCambioFiltro(setEstadoFilter)}
        />
      }
      items={filteredPrestamos}
      totalPaginas={totalPages}
      totalElementos={totalElements}
      cargando={isLoading}
      hayError={isError}
      pagina={pagina}
      tamanioPagina={tamanioPagina}
      alCambiarPagina={setPagina}
      alCambiarTamanioPagina={setTamanioPagina}
      opcionesTamanioPagina={OPCIONES_TAMANIO_PAGINA_PRESTAMOS}
      alCrear={() => navigate(APP_ROUTES.PRESTAMOS_NUEVO)}
      renderizarTarjeta={(prestamo) => (
        <PrestamoCard
          key={prestamo.id}
          prestamo={prestamo}
          onDevolver={handleDevolver}
          isDevolviendo={isDevolviendo(prestamo.id)}
        />
      )}
      esqueleto={<PrestamosSkeleton />}
      iconoVacio={<BookMarked className="h-8 w-8" />}
      tituloVacio="No se encontraron préstamos"
      descripcionVacio="Ajusta los filtros o registra el primer préstamo del sistema."
      etiquetaAccionVacio="Nuevo Préstamo"
      mensajeError="Verifica que el backend esté activo e intenta nuevamente."
      claseGrilla="grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
    />
  );
};

