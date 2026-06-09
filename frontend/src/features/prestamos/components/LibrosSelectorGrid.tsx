import { useState } from 'react';
import { Search, Plus, ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import type { LibroResponse } from '@/features/libros/types/libro';

const PAGE_SIZE = 8;

interface LibrosSelectorGridProps {
  buscarLibro: string;
  setBuscarLibro: (val: string) => void;
  librosFiltrados: LibroResponse[];
  totalLibrosCarrito: number;
  cupoRestante: number;
  selectedUsuario: unknown;
  cupoPrestamoLoading: boolean;
  librosYaPrestadosIds: Set<number>;
  cart: { libro: LibroResponse; cantidad: number }[];
  onAgregarAlCarrito: (libro: LibroResponse) => void;
  renderPortada: (libro: LibroResponse) => React.ReactNode;
  maxReservaMaxima: number;
}

export const LibrosSelectorGrid = ({
  buscarLibro,
  setBuscarLibro,
  librosFiltrados,
  totalLibrosCarrito,
  cupoRestante,
  selectedUsuario,
  cupoPrestamoLoading,
  librosYaPrestadosIds,
  cart,
  onAgregarAlCarrito,
  renderPortada,
  maxReservaMaxima,
}: LibrosSelectorGridProps) => {
  const [paginaActual, setPaginaActual] = useState(0);

  // Al cambiar la búsqueda, resetear página
  const handleBuscar = (val: string) => {
    setBuscarLibro(val);
    setPaginaActual(0);
  };

  const totalPaginas = Math.ceil(librosFiltrados.length / PAGE_SIZE);
  const librosEnPagina = librosFiltrados.slice(
    paginaActual * PAGE_SIZE,
    (paginaActual + 1) * PAGE_SIZE,
  );

  return (
    <div className="rounded-2xl border border-border/80 bg-card/30 p-5 shadow-sm">
      <div className="mb-4 flex flex-col gap-3 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground">Paso 2</p>
          <h3 className="mt-1 text-lg font-bold">Agrega libros al préstamo</h3>
          <p className="mt-1 text-xs text-muted-foreground">
            Revisa portada, autor, categoría y stock antes de agregar.
          </p>
        </div>
        <div className="grid grid-cols-2 gap-2 text-right sm:flex">
          <div className="rounded-xl border border-border/70 bg-background/70 px-3 py-2 text-left">
            <p className="text-[10px] font-bold uppercase text-muted-foreground">En carrito</p>
            <p className="text-sm font-bold">{totalLibrosCarrito} libro(s)</p>
          </div>
          <div className={`rounded-xl border px-3 py-2 text-left ${!selectedUsuario
            ? 'border-border/70 bg-background/70'
            : cupoRestante === 0
              ? 'border-rose-300/60 bg-rose-50/40'
              : cupoRestante <= 2
                ? 'border-amber-300/60 bg-amber-50/40'
                : 'border-emerald-300/60 bg-emerald-50/40'
            }`}>
            <p className="text-[10px] font-bold uppercase text-muted-foreground">Cupo disponible</p>
            <p className={`text-sm font-bold ${!selectedUsuario ? 'text-muted-foreground'
              : cupoRestante === 0 ? 'text-rose-600'
                : cupoRestante <= 2 ? 'text-amber-600'
                  : 'text-emerald-600'
              }`}>
              {!selectedUsuario
                ? 'Sin lector'
                : selectedUsuario && cupoPrestamoLoading
                  ? '...'
                  : `${cupoRestante} lugar(es)`}
            </p>
          </div>
        </div>
      </div>

      <CampoFormulario label="Libros disponibles" required>
        <div className="overflow-hidden rounded-2xl border border-border/80 bg-background/50">
          {/* Buscador */}
          <div className="relative border-b border-border/60 p-2">
            <Search className="absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="Buscar por título o ISBN..."
              value={buscarLibro}
              onChange={(event) => handleBuscar(event.target.value)}
              className="h-10 border-0 bg-transparent pl-9 text-sm shadow-none"
            />
          </div>

          {/* Lista con scroll fijo */}
          <ScrollArea className="h-128">
            <div className="divide-y divide-border/40">
              {librosEnPagina.map((libro) => {
                const enCarrito = cart.find((item) => item.libro.id === libro.id);
                const yaLoPosee = librosYaPrestadosIds.has(libro.id);

                const autoresNames = libro.autores.length
                  ? libro.autores.map((autor) => autor.nombre).join(', ')
                  : 'Sin autor registrado';

                return (
                  <div key={libro.id} className="flex items-center gap-4 px-4 py-3 hover:bg-muted/20">
                    {renderPortada(libro)}
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-bold">{libro.titulo}</p>
                      <p className="mt-0.5 truncate text-xs text-muted-foreground">
                        {libro.categoria.nombre} · ISBN {libro.isbn}
                      </p>
                      <p className="mt-1 truncate text-xs font-medium text-foreground/80">
                        {autoresNames}
                      </p>
                      <div className="mt-2 flex flex-wrap gap-1.5">
                        <Badge variant="secondary" className="text-[10px] font-bold">
                          {libro.stock} disponibles
                        </Badge>
                        {enCarrito ? (
                          <Badge variant="outline" className="text-[10px] font-bold text-primary">
                            En carrito x{enCarrito.cantidad}
                          </Badge>
                        ) : null}
                        {yaLoPosee ? (
                          <Badge variant="outline" className="text-[10px] font-bold text-amber-600">
                            Ya lo posee
                          </Badge>
                        ) : null}
                      </div>
                    </div>
                    <Button
                      type="button"
                      size="sm"
                      className="rounded-xl text-xs font-bold"
                      variant={enCarrito || yaLoPosee ? 'outline' : 'default'}
                      onClick={() => onAgregarAlCarrito(libro)}
                      disabled={
                        !selectedUsuario ||
                        cupoPrestamoLoading ||
                        yaLoPosee ||
                        totalLibrosCarrito >= (totalLibrosCarrito + cupoRestante) ||
                        Boolean(enCarrito && enCarrito.cantidad >= maxReservaMaxima)
                      }
                    >
                      {yaLoPosee ? (
                        'Ya lo posee'
                      ) : (
                        <>
                          <Plus className="h-3.5 w-3.5" />
                          Agregar
                        </>
                      )}
                    </Button>
                  </div>
                );
              })}
              {librosFiltrados.length === 0 ? (
                <p className="py-10 text-center text-sm text-muted-foreground">
                  No se encontraron libros disponibles.
                </p>
              ) : null}
            </div>
          </ScrollArea>

          {/* Paginación */}
          {totalPaginas > 1 && (
            <div className="flex items-center justify-between border-t border-border/60 px-4 py-2.5">
              <p className="text-[11px] text-muted-foreground">
                Mostrando {paginaActual * PAGE_SIZE + 1}–{Math.min((paginaActual + 1) * PAGE_SIZE, librosFiltrados.length)} de {librosFiltrados.length}
              </p>
              <div className="flex items-center gap-1">
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 rounded-lg"
                  onClick={() => setPaginaActual((p) => Math.max(0, p - 1))}
                  disabled={paginaActual === 0}
                >
                  <ChevronLeft className="h-4 w-4" />
                </Button>
                <span className="min-w-16 text-center text-xs font-semibold text-muted-foreground">
                  {paginaActual + 1} / {totalPaginas}
                </span>
                <Button
                  type="button"
                  variant="ghost"
                  size="icon"
                  className="h-7 w-7 rounded-lg"
                  onClick={() => setPaginaActual((p) => Math.min(totalPaginas - 1, p + 1))}
                  disabled={paginaActual >= totalPaginas - 1}
                >
                  <ChevronRight className="h-4 w-4" />
                </Button>
              </div>
            </div>
          )}
        </div>
      </CampoFormulario>
    </div>
  );
};

