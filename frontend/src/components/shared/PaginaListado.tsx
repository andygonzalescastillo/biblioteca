import type { ReactNode } from 'react';
import { Plus } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Paginacion } from '@/components/shared/Paginacion';
import { EstadoError, EstadoVacio } from '@/components/shared/EstadoPagina';
import { OPCIONES_TAMANIO_PAGINA } from '@/shared/constants/paginacion';

interface PaginaListadoProps<T extends { id: number }> {
  titulo: string;
  descripcion: string;
  etiquetaCrear: string;
  filtros: ReactNode;
  items: T[];
  totalPaginas: number;
  totalElementos: number;
  cargando: boolean;
  hayError: boolean;
  error?: unknown;
  pagina: number;
  tamanioPagina: number;
  alCambiarPagina: (pagina: number) => void;
  alCambiarTamanioPagina: (tamanio: number) => void;
  alCrear: () => void;
  renderizarTarjeta: (item: T) => ReactNode;
  esqueleto: ReactNode;
  iconoVacio: ReactNode;
  tituloVacio: string;
  descripcionVacio: string;
  etiquetaAccionVacio: string;
  mensajeError?: string;
  claseGrilla?: string;
  opcionesTamanioPagina?: readonly number[];
  modales?: ReactNode;
}

export const PaginaListado = <T extends { id: number }>({
  titulo,
  descripcion,
  etiquetaCrear,
  filtros,
  items,
  totalPaginas,
  totalElementos,
  cargando,
  hayError,
  error,
  pagina,
  tamanioPagina,
  alCambiarPagina,
  alCambiarTamanioPagina,
  alCrear,
  renderizarTarjeta,
  esqueleto,
  iconoVacio,
  tituloVacio,
  descripcionVacio,
  etiquetaAccionVacio,
  mensajeError,
  claseGrilla = 'grid gap-6 sm:grid-cols-2 lg:grid-cols-4',
  opcionesTamanioPagina = OPCIONES_TAMANIO_PAGINA,
  modales,
}: PaginaListadoProps<T>) => {
  const mensajeErrorResuelto =
    mensajeError ??
    (error as { message?: string })?.message ??
    'Error al cargar los datos.';

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight">{titulo}</h2>
          <p className="text-muted-foreground text-sm">{descripcion}</p>
        </div>
        <Button onClick={alCrear} className="rounded-xl shadow-md shadow-primary/10 shrink-0">
          <Plus className="h-4 w-4 mr-2" /> {etiquetaCrear}
        </Button>
      </div>

      {filtros}

      {cargando ? (
        esqueleto
      ) : hayError ? (
        <EstadoError mensaje={mensajeErrorResuelto} />
      ) : items.length === 0 ? (
        <EstadoVacio
          icono={iconoVacio}
          titulo={tituloVacio}
          descripcion={descripcionVacio}
          etiquetaAccion={etiquetaAccionVacio}
          alAccionar={alCrear}
        />
      ) : (
        <div className="space-y-6">
          <div className={claseGrilla}>{items.map(renderizarTarjeta)}</div>
          <Paginacion
            pagina={pagina}
            totalPaginas={totalPaginas}
            totalElementos={totalElementos}
            alCambiarPagina={alCambiarPagina}
            tamanioPagina={tamanioPagina}
            alCambiarTamanioPagina={alCambiarTamanioPagina}
            opcionesTamanioPagina={[...opcionesTamanioPagina]}
          />
        </div>
      )}

      {modales}
    </div>
  );
};

