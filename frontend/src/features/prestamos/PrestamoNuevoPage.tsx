import { useEffect, useMemo, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { BookOpen, Loader2 } from 'lucide-react';
import { EstadoError } from '@/components/shared/EstadoPagina';
import { useConfiguracionPublica } from '@/features/configuracion/hooks/useConfiguracionPublica';
import { useCupoPrestamoUsuario, useLibrosDisponibles, useUsuariosActivos } from './hooks/useOpcionesPrestamo';
import { useMutacionesPrestamo } from './hooks/useMutacionesPrestamo';
import { prestamoFormSchema, type PrestamoFormValues } from './schemas/prestamoSchema';
import type { LibroResponse } from '@/features/libros/types/libro';
import { obtenerUrlPreviewImagen } from '@/lib/previewImagen';
import { APP_ROUTES } from '@/shared/constants/appRoutes';
import { toast } from 'sonner';
import { LectorSelector } from './components/LectorSelector';
import { LibrosSelectorGrid } from './components/LibrosSelectorGrid';
import { ResumenPrestamoSidebar } from './components/ResumenPrestamoSidebar';

interface CartItem {
  libro: LibroResponse;
  cantidad: number;
}

export const PrestamoNuevoPage = () => {
  const navigate = useNavigate();
  const [buscarLibro, setBuscarLibro] = useState('');
  const [buscarUsuario, setBuscarUsuario] = useState('');
  const [cart, setCart] = useState<CartItem[]>([]);

  const configuracion = useConfiguracionPublica();
  const usuarios = useUsuariosActivos();
  const libros = useLibrosDisponibles();
  const configuracionPrestamo = configuracion.data?.prestamo;

  const {
    handleSubmit,
    setValue,
    setError,
    control,
    reset,
    formState: { errors },
  } = useForm<PrestamoFormValues>({
    resolver: zodResolver(prestamoFormSchema),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: { usuarioId: 0, diasLimite: 0 },
  });

  useEffect(() => {
    if (configuracionPrestamo) {
      reset({ usuarioId: 0, diasLimite: configuracionPrestamo.diasDefault });
    }
  }, [configuracionPrestamo, reset]);

  const selectedUsuarioId = useWatch({ control, name: 'usuarioId', defaultValue: 0 });
  const selectedUsuario = usuarios.data?.content.find((u) => u.id === selectedUsuarioId);
  const cupoPrestamo = useCupoPrestamoUsuario(selectedUsuarioId || undefined);
  const diasLimite = useWatch({ control, name: 'diasLimite', defaultValue: configuracionPrestamo?.diasDefault ?? 0 });

  const totalLibrosCarrito = useMemo(() => cart.reduce((sum, item) => sum + item.cantidad, 0), [cart]);
  const cupoDisponibleLector = cupoPrestamo.data?.cupoDisponible ?? 0;
  const librosYaPrestadosIds = useMemo(
    () => new Set(cupoPrestamo.data?.librosPrestadosIds ?? []),
    [cupoPrestamo.data?.librosPrestadosIds],
  );
  const librosYaPrestadosEnCarrito = useMemo(
    () => cart.filter((item) => librosYaPrestadosIds.has(item.libro.id)),
    [cart, librosYaPrestadosIds],
  );
  const cupoRestante = selectedUsuario ? Math.max(0, cupoDisponibleLector - totalLibrosCarrito) : 0;

  const fechaDevolucion = useMemo(() => {
    if (!configuracionPrestamo || !diasLimite || diasLimite < configuracionPrestamo.diasMinimo || diasLimite > configuracionPrestamo.diasMaximo) return null;
    const date = new Date();
    date.setDate(date.getDate() + Number(diasLimite));
    return date.toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  }, [configuracionPrestamo, diasLimite]);

  const usuariosFiltrados = useMemo(() => {
    const query = buscarUsuario.trim().toLowerCase();
    const data = usuarios.data?.content ?? [];
    if (!query) return data;
    return data.filter((usuario) =>
      usuario.nombre.toLowerCase().includes(query) ||
      usuario.email.toLowerCase().includes(query),
    );
  }, [buscarUsuario, usuarios.data?.content]);

  const librosFiltrados = useMemo(() => {
    const query = buscarLibro.trim().toLowerCase();
    return (libros.data?.content ?? []).filter((libro) =>
      libro.estado &&
      libro.stock > 0 &&
      (!query || libro.titulo.toLowerCase().includes(query) || libro.isbn.includes(query)),
    );
  }, [buscarLibro, libros.data?.content]);

  const { mutacionRegistrar } = useMutacionesPrestamo({
    alRegistrarExito: () => navigate(APP_ROUTES.PRESTAMOS),
  });

  const agregarAlCarrito = (libro: LibroResponse) => {
    if (!configuracionPrestamo) {
      toast.error('No se pudo cargar la configuración de préstamos.');
      return;
    }
    if (!selectedUsuario) {
      toast.error('Selecciona un lector para registrar el préstamo.');
      return;
    }
    if (cupoPrestamo.isLoading || !cupoPrestamo.data) {
      toast.error('Espera a que se calcule el cupo disponible del lector.');
      return;
    }
    if (librosYaPrestadosIds.has(libro.id)) {
      toast.warning(`El lector ya posee "${libro.titulo}".`);
      return;
    }

    const existente = cart.find((item) => item.libro.id === libro.id);
    if (totalLibrosCarrito >= cupoDisponibleLector) {
      toast.warning(`Este lector solo tiene ${cupoDisponibleLector} cupo(s) disponible(s).`);
      return;
    }

    if (!existente) {
      setCart([...cart, { libro, cantidad: 1 }]);
      return;
    }

    if (existente.cantidad >= configuracionPrestamo.cantidadReservaMaxima) {
      toast.warning(`Cantidad máxima alcanzada para este libro: ${configuracionPrestamo.cantidadReservaMaxima} unidad(es).`);
      return;
    }
    if (existente.cantidad >= libro.stock) {
      toast.error(`Stock máximo disponible: ${libro.stock}.`);
      return;
    }

    setCart(cart.map((item) => item.libro.id === libro.id ? { ...item, cantidad: item.cantidad + 1 } : item));
  };

  const actualizarCantidad = (libroId: number, delta: number) => {
    if (!configuracionPrestamo) return;
    setCart((items) =>
      items.map((item) => {
        if (item.libro.id !== libroId) return item;
        const siguiente = item.cantidad + delta;
        if (siguiente < 1) return item;
        if (siguiente > item.libro.stock) {
          toast.error(`Stock máximo disponible: ${item.libro.stock}.`);
          return item;
        }
        if (siguiente > configuracionPrestamo.cantidadReservaMaxima) {
          toast.warning(`Cantidad máxima permitida: ${configuracionPrestamo.cantidadReservaMaxima} unidad(es).`);
          return item;
        }
        if (delta > 0 && totalLibrosCarrito >= cupoDisponibleLector) {
          toast.warning(`Este lector solo tiene ${cupoDisponibleLector} cupo(s) disponible(s).`);
          return item;
        }
        return { ...item, cantidad: siguiente };
      }),
    );
  };

  const quitarDelCarrito = (libroId: number) => {
    setCart((items) => items.filter((item) => item.libro.id !== libroId));
  };

  const ajustarDiasPrestamo = (delta: number) => {
    if (!configuracionPrestamo) return;
    const actual = Number(diasLimite || configuracionPrestamo.diasDefault);
    const siguiente = Math.min(
      configuracionPrestamo.diasMaximo,
      Math.max(configuracionPrestamo.diasMinimo, actual + delta),
    );
    setValue('diasLimite', siguiente, {
      shouldDirty: true,
      shouldValidate: true,
      shouldTouch: true,
    });
  };

  const cambiarLector = () => {
    setValue('usuarioId', 0, { shouldValidate: true });
    setBuscarUsuario('');
    setCart([]);
  };

  const renderPortada = (libro: LibroResponse, className = 'w-14') => {
    const portadaUrl = obtenerUrlPreviewImagen(libro.portada);
    return (
      <div className={`${className} shrink-0 aspect-2/3 overflow-hidden rounded-xl border border-border/70 bg-muted/40 relative flex items-center justify-center`}>
        {portadaUrl ? (
          <img src={portadaUrl} alt={libro.titulo} className="absolute inset-0 h-full w-full object-cover" />
        ) : (
          <BookOpen className="h-5 w-5 text-muted-foreground" />
        )}
      </div>
    );
  };

  const onSubmit = (data: PrestamoFormValues) => {
    if (!configuracionPrestamo) {
      toast.error('No se pudo cargar la configuración de préstamos.');
      return;
    }
    if (data.diasLimite < configuracionPrestamo.diasMinimo || data.diasLimite > configuracionPrestamo.diasMaximo) {
      setError('diasLimite', {
        message: `Debe estar entre ${configuracionPrestamo.diasMinimo} y ${configuracionPrestamo.diasMaximo} días`,
      });
      return;
    }
    if (cart.length === 0) {
      toast.error('Agrega al menos un libro al préstamo.');
      return;
    }
    if (librosYaPrestadosEnCarrito.length > 0) {
      toast.warning(`El lector ya posee "${librosYaPrestadosEnCarrito[0].libro.titulo}".`);
      return;
    }
    if (totalLibrosCarrito > cupoDisponibleLector) {
      toast.error(`Este lector solo tiene ${cupoDisponibleLector} cupo(s) disponible(s).`);
      return;
    }

    mutacionRegistrar.mutate({
      usuarioId: data.usuarioId,
      diasLimite: data.diasLimite,
      detalles: cart.map((item) => ({ libroId: item.libro.id, cantidad: item.cantidad })),
    });
  };

  if (configuracion.isError || usuarios.isError || libros.isError) {
    return <EstadoError mensaje="No se pudo cargar la información necesaria para registrar el préstamo." />;
  }

  const cargando = configuracion.isLoading || usuarios.isLoading || libros.isLoading;

  return (
    <div className="space-y-6">
      <header className="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <p className="text-xs font-bold uppercase tracking-wider text-primary">Registrar préstamo</p>
          <h2 className="mt-2 text-3xl font-extrabold tracking-tight">Nuevo préstamo</h2>
          <p className="mt-2 max-w-2xl text-sm text-muted-foreground">
            Selecciona un lector, agrega libros con sus cantidades y confirma el préstamo en una sola operación.
          </p>
        </div>
      </header>

      {cargando ? (
        <div className="flex min-h-105 items-center justify-center rounded-2xl border border-border/80 bg-card/30">
          <div className="flex items-center gap-3 text-sm font-semibold text-muted-foreground">
            <Loader2 className="h-5 w-5 animate-spin" />
            Cargando datos del préstamo...
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_380px]">
          <section className="flex flex-col gap-5">
            <LectorSelector
              selectedUsuario={selectedUsuario}
              usuariosFiltrados={usuariosFiltrados}
              buscarUsuario={buscarUsuario}
              setBuscarUsuario={setBuscarUsuario}
              onSelectUsuario={(id) => setValue('usuarioId', id, { shouldValidate: true })}
              onClearLector={cambiarLector}
              error={errors.usuarioId?.message}
            />

            <LibrosSelectorGrid
              buscarLibro={buscarLibro}
              setBuscarLibro={setBuscarLibro}
              librosFiltrados={librosFiltrados}
              totalLibrosCarrito={totalLibrosCarrito}
              cupoRestante={cupoRestante}
              selectedUsuario={selectedUsuario}
              cupoPrestamoLoading={cupoPrestamo.isLoading}
              librosYaPrestadosIds={librosYaPrestadosIds}
              cart={cart}
              onAgregarAlCarrito={agregarAlCarrito}
              renderPortada={renderPortada}
              maxReservaMaxima={configuracionPrestamo?.cantidadReservaMaxima ?? 1}
            />
          </section>

          <ResumenPrestamoSidebar
            totalLibrosCarrito={totalLibrosCarrito}
            cupoRestante={cupoRestante}
            selectedUsuario={selectedUsuario}
            cupoPrestamoLoading={cupoPrestamo.isLoading}
            cupoPrestamoData={cupoPrestamo.data ? {
              maximoPermitido: cupoPrestamo.data.maximoPermitido,
              librosEnPosesion: cupoPrestamo.data.librosEnPosesion
            } : undefined}
            diasLimite={diasLimite}
            ajustarDiasPrestamo={ajustarDiasPrestamo}
            configuracionPrestamo={configuracionPrestamo}
            fechaDevolucion={fechaDevolucion}
            cart={cart}
            quitarDelCarrito={quitarDelCarrito}
            actualizarCantidad={actualizarCantidad}
            isSaving={mutacionRegistrar.isPending}
            onRegistrar={handleSubmit(onSubmit)}
            onCancelar={() => navigate(APP_ROUTES.PRESTAMOS)}
            renderPortada={renderPortada}
            diasLimiteError={errors.diasLimite?.message}
            onChangeDiasLimite={(val) => setValue('diasLimite', val, { shouldValidate: true })}
          />
        </form>
      )}
    </div>
  );
};

