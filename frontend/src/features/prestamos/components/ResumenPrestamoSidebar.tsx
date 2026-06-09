import React from 'react';
import { ShoppingCart, CalendarDays, Minus, Plus, Trash2, Loader2 } from 'lucide-react';
import { Separator } from '@/components/ui/separator';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Badge } from '@/components/ui/badge';
import { ScrollArea } from '@/components/ui/scroll-area';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import type { LibroResponse } from '@/features/libros/types/libro';
import type { UsuarioResponse } from '@/features/usuarios/types/usuario';

interface ResumenPrestamoSidebarProps {
  totalLibrosCarrito: number;
  cupoRestante: number;
  selectedUsuario: UsuarioResponse | undefined;
  cupoPrestamoLoading: boolean;
  cupoPrestamoData?: {
    maximoPermitido: number;
    librosEnPosesion: number;
  };
  diasLimite: number;
  ajustarDiasPrestamo: (delta: number) => void;
  configuracionPrestamo?: {
    diasMinimo: number;
    diasMaximo: number;
    cantidadReservaMaxima: number;
  };
  fechaDevolucion: string | null;
  cart: { libro: LibroResponse; cantidad: number }[];
  quitarDelCarrito: (libroId: number) => void;
  actualizarCantidad: (libroId: number, delta: number) => void;
  isSaving: boolean;
  onRegistrar: () => void;
  onCancelar: () => void;
  renderPortada: (libro: LibroResponse) => React.ReactNode;
  diasLimiteError?: string;
  onChangeDiasLimite: (value: number) => void;
}

export const ResumenPrestamoSidebar = ({
  totalLibrosCarrito,
  cupoRestante,
  selectedUsuario,
  cupoPrestamoLoading,
  cupoPrestamoData,
  diasLimite,
  ajustarDiasPrestamo,
  configuracionPrestamo,
  fechaDevolucion,
  cart,
  quitarDelCarrito,
  actualizarCantidad,
  isSaving,
  onRegistrar,
  onCancelar,
  renderPortada,
  diasLimiteError,
  onChangeDiasLimite,
}: ResumenPrestamoSidebarProps) => {
  const autoresNames = (libro: LibroResponse) => {
    return libro.autores.length ? libro.autores.map((a) => a.nombre).join(', ') : 'Sin autor';
  };

  return (
    <aside className="rounded-2xl border border-border/80 bg-card/40 p-5 shadow-sm xl:sticky xl:top-6 xl:self-start">
      <div>
        <p className="flex items-center gap-1.5 text-xs font-bold uppercase text-muted-foreground">
          <ShoppingCart className="h-3.5 w-3.5" />
          Resumen
        </p>
        <h3 className="mt-2 text-xl font-extrabold">Préstamo en preparación</h3>
        <div className="mt-4 grid grid-cols-2 gap-2">
          <div className="rounded-2xl border border-border/70 bg-background/70 p-3">
            <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Libros</p>
            <p className="mt-1 text-2xl font-extrabold">{totalLibrosCarrito}</p>
          </div>
          <div className="rounded-2xl border border-border/70 bg-background/70 p-3">
            <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Cupo libre</p>
            <p className="mt-1 text-2xl font-extrabold">
              {selectedUsuario && cupoPrestamoLoading ? '...' : cupoRestante}
            </p>
          </div>
        </div>
      </div>

      {selectedUsuario ? (
        <div className="mt-4 rounded-xl border border-border/70 bg-background/70 p-3">
          <p className="text-[10px] font-bold uppercase tracking-wider text-muted-foreground">Lector seleccionado</p>
          <p className="mt-1 truncate text-sm font-bold">{selectedUsuario.nombre}</p>
          <p className="truncate text-[11px] text-muted-foreground">{selectedUsuario.email}</p>
          {cupoPrestamoData ? (
            <div className="mt-3 grid grid-cols-3 gap-2 text-center">
              <div className="rounded-lg bg-muted/40 px-2 py-1.5">
                <p className="text-[10px] text-muted-foreground">Máximo</p>
                <p className="text-xs font-bold">{cupoPrestamoData.maximoPermitido}</p>
              </div>
              <div className="rounded-lg bg-muted/40 px-2 py-1.5">
                <p className="text-[10px] text-muted-foreground">Tiene</p>
                <p className="text-xs font-bold">{cupoPrestamoData.librosEnPosesion}</p>
              </div>
              <div className="rounded-lg bg-muted/40 px-2 py-1.5">
                <p className="text-[10px] text-muted-foreground">Libre</p>
                <p className="text-xs font-bold">{cupoRestante}</p>
              </div>
            </div>
          ) : null}
        </div>
      ) : null}

      <Separator className="my-5" />

      <CampoFormulario label="Días de plazo" required error={diasLimiteError}>
        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="outline"
            size="icon"
            className="h-11 w-11 rounded-xl"
            onClick={() => ajustarDiasPrestamo(-1)}
            disabled={!configuracionPrestamo || Number(diasLimite) <= configuracionPrestamo.diasMinimo}
          >
            <Minus className="h-4 w-4" />
          </Button>
          <Input
            id="diasLimite"
            value={String(diasLimite)}
            onChange={(e) => onChangeDiasLimite(e.target.value === '' ? 0 : Number(e.target.value))}
            type="number"
            min={configuracionPrestamo?.diasMinimo}
            max={configuracionPrestamo?.diasMaximo}
            className="h-11 rounded-xl text-center font-mono text-base font-bold"
          />
          <Button
            type="button"
            variant="outline"
            size="icon"
            className="h-11 w-11 rounded-xl"
            onClick={() => ajustarDiasPrestamo(1)}
            disabled={!configuracionPrestamo || Number(diasLimite) >= configuracionPrestamo.diasMaximo}
          >
            <Plus className="h-4 w-4" />
          </Button>
        </div>
        {configuracionPrestamo ? (
          <div className="mt-3 rounded-xl border border-border/70 bg-background/70 p-3">
            <p className="text-[11px] text-muted-foreground">
              Mínimo {configuracionPrestamo.diasMinimo}, máximo {configuracionPrestamo.diasMaximo}. Hasta {configuracionPrestamo.cantidadReservaMaxima} unidad(es) por libro.
            </p>
            {fechaDevolucion ? (
              <p className="mt-2 flex items-center gap-1.5 text-xs font-semibold text-primary">
                <CalendarDays className="h-3.5 w-3.5" />
                Devuelve: <span className="capitalize">{fechaDevolucion}</span>
              </p>
            ) : null}
          </div>
        ) : null}
      </CampoFormulario>

      <Separator className="my-5" />

      <div className="mb-3 flex items-center justify-between gap-3">
        <p className="text-xs font-bold uppercase tracking-wider text-muted-foreground">Libros elegidos</p>
        {cart.length > 0 ? (
          <Badge variant="secondary" className="text-[10px] font-bold">
            {cart.length} título(s)
          </Badge>
        ) : null}
      </div>

      <ScrollArea className="h-90">
        {cart.length === 0 ? (
          <div className="flex h-90 flex-col items-center justify-center rounded-xl border border-dashed border-border/80 text-center">
            <ShoppingCart className="h-9 w-9 text-muted-foreground" />
            <p className="mt-3 text-sm font-bold">Sin libros agregados</p>
            <p className="mt-1 max-w-55 text-xs text-muted-foreground">
              Agrega libros desde el listado para confirmar el préstamo.
            </p>
          </div>
        ) : (
          <div className="space-y-3 pr-2">
            {cart.map(({ libro, cantidad }) => (
              <div key={libro.id} className="rounded-2xl border border-border/70 bg-background/70 p-3">
                <div className="flex items-start gap-3">
                  {renderPortada(libro)}
                  <div className="min-w-0 flex-1">
                    <p className="line-clamp-2 text-sm font-bold">{libro.titulo}</p>
                    <p className="mt-1 line-clamp-1 text-[11px] font-medium text-foreground/80">
                      {autoresNames(libro)}
                    </p>
                    <p className="mt-1 text-[11px] text-muted-foreground">
                      {libro.categoria.nombre} · Stock: {libro.stock}
                    </p>
                  </div>
                  <Button
                    type="button"
                    size="icon"
                    variant="ghost"
                    className="h-7 w-7 rounded-lg hover:bg-rose-500/10 hover:text-rose-500"
                    onClick={() => quitarDelCarrito(libro.id)}
                  >
                    <Trash2 className="h-3.5 w-3.5" />
                  </Button>
                </div>
                <div className="mt-3 flex items-center justify-between">
                  <div className="flex items-center gap-1 rounded-xl border border-border/70 bg-card/60 p-1">
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      className="h-7 w-7 rounded-lg"
                      onClick={() => actualizarCantidad(libro.id, -1)}
                      disabled={cantidad <= 1}
                    >
                      <Minus className="h-3.5 w-3.5" />
                    </Button>
                    <span className="w-7 text-center text-sm font-bold">{cantidad}</span>
                    <Button
                      type="button"
                      size="icon"
                      variant="ghost"
                      className="h-7 w-7 rounded-lg"
                      onClick={() => actualizarCantidad(libro.id, 1)}
                      disabled={
                        cupoPrestamoLoading ||
                        !configuracionPrestamo ||
                        totalLibrosCarrito >= (totalLibrosCarrito + cupoRestante) ||
                        cantidad >= configuracionPrestamo.cantidadReservaMaxima ||
                        cantidad >= libro.stock
                      }
                    >
                      <Plus className="h-3.5 w-3.5" />
                    </Button>
                  </div>
                  <div className="text-right">
                    <Badge variant="outline" className="text-[10px] font-bold">x{cantidad}</Badge>
                    <p className="mt-1 text-[10px] text-muted-foreground">unidad(es)</p>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </ScrollArea>

      <div className="mt-5 grid gap-2">
        <Button
          type="button"
          onClick={onRegistrar}
          className="h-11 rounded-xl font-bold"
          disabled={
            isSaving ||
            !configuracionPrestamo ||
            !selectedUsuario ||
            cupoPrestamoLoading ||
            cart.length === 0
          }
        >
          {isSaving ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Registrando...
            </>
          ) : (
            <>
              <ShoppingCart className="h-4 w-4" />
              Registrar préstamo
            </>
          )}
        </Button>
        <Button
          type="button"
          variant="outline"
          className="h-10 rounded-xl font-bold"
          onClick={onCancelar}
        >
          Cancelar
        </Button>
      </div>
    </aside>
  );
};

