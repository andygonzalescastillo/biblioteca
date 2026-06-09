import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { BookOpen, Users, CalendarDays, Plus, Clock, AlertTriangle, CheckCircle2, Package2, Timer, Trophy, Boxes } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Skeleton } from '@/components/ui/skeleton';
import { MetricCard } from './components/MetricCard';
import { PrestamoItem } from './components/PrestamoItem';
import { EstadoError } from '@/components/shared/EstadoPagina';
import { QuickActions } from './components/QuickActions';
import { AlertsSection } from './components/AlertsSection';
import { RankingsSection } from './components/RankingsSection';
import { dashboardService } from './services/dashboardService';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

export const DashboardPage = () => {
  const resumen = useQuery({
    queryKey: ['dashboard', 'resumen'],
    queryFn: dashboardService.obtenerResumen,
    retry: 1,
  });

  const isOffline = resumen.isError;

  if (isOffline) {
    return (
      <EstadoError
        mensaje="No se pudo establecer conexión con el backend de la biblioteca. Verifica que esté en ejecución."
      />
    );
  }

  const data = resumen.data;
  const prestamosRecientes = data?.prestamosRecientes ?? [];
  const alertasInventario = data?.alertasInventario ?? [];
  const prestamosVencidos = data?.prestamosVencidos ?? [];
  const prestamosProximosAVencer = data?.prestamosProximosAVencer ?? [];
  const librosMasPrestados = data?.librosMasPrestados ?? [];
  const lectoresMasActivos = data?.lectoresMasActivos ?? [];
  const categoriasConMasLibros = data?.categoriasConMasLibros ?? [];
  const autoresConMasLibros = data?.autoresConMasLibros ?? [];

  return (
    <div className="space-y-8">
      <div className="flex flex-col gap-2 md:flex-row md:items-center md:justify-between">
        <div>
          <h2 className="text-3xl font-extrabold tracking-tight bg-linear-to-r from-foreground to-muted-foreground bg-clip-text text-transparent">
            Resumen General
          </h2>
          <p className="text-muted-foreground text-sm">
            Control de inventario, préstamos y usuarios en tiempo real.
          </p>
        </div>
        <Button render={<Link to={APP_ROUTES.PRESTAMOS} />} className="rounded-xl shadow-md shadow-primary/10">
          <Plus className="h-4 w-4 mr-2" /> Nuevo Préstamo
        </Button>
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          icon={BookOpen}
          iconColor="text-primary"
          label="Libros Catalogados"
          value={data?.totalLibros}
          loading={resumen.isLoading}
          subtitle="Libros en la base de datos"
          linkTo={APP_ROUTES.LIBROS}
          linkLabel="Ver catálogo"
        />
        <MetricCard
          icon={Users}
          iconColor="text-blue-500"
          label="Lectores Activos"
          value={data?.usuariosActivos}
          loading={resumen.isLoading}
          subtitle={`${data?.totalUsuarios ?? 0} registrados en total`}
          linkTo={APP_ROUTES.USUARIOS}
          linkLabel="Ver directorio"
        />
        <MetricCard
          icon={CalendarDays}
          iconColor="text-amber-500"
          label="Préstamos Activos"
          value={data?.prestamosActivos}
          loading={resumen.isLoading}
          subtitle="Libros fuera de almacén"
          linkTo={APP_ROUTES.PRESTAMOS}
          linkLabel="Ver préstamos"
        />
        <MetricCard
          icon={AlertTriangle}
          iconColor="text-rose-500"
          label="Préstamos Atrasados"
          value={data?.prestamosAtrasados}
          loading={resumen.isLoading}
          subtitle="Devoluciones pendientes"
          linkTo={APP_ROUTES.PRESTAMOS}
          linkLabel="Ver atrasos"
        />
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          icon={BookOpen}
          iconColor="text-cyan-500"
          label="Libros Activos"
          value={data?.totalLibrosActivos}
          loading={resumen.isLoading}
          subtitle={`${data?.totalLibros ?? 0} títulos registrados`}
          linkTo={APP_ROUTES.LIBROS}
          linkLabel="Ver catálogo"
        />
        <MetricCard
          icon={Boxes}
          iconColor="text-emerald-500"
          label="Ejemplares Disponibles"
          value={data?.totalEjemplares}
          loading={resumen.isLoading}
          subtitle="Unidades activas en inventario"
          linkTo={APP_ROUTES.LIBROS}
          linkLabel="Ver inventario"
        />
        <MetricCard
          icon={BookOpen}
          iconColor="text-rose-500"
          label="Libros Agotados"
          value={data?.librosAgotados}
          loading={resumen.isLoading}
          subtitle="Títulos activos sin stock"
          linkTo={APP_ROUTES.LIBROS}
          linkLabel="Revisar stock"
        />
        <MetricCard
          icon={Timer}
          iconColor="text-orange-500"
          label="Vencen Pronto"
          value={data?.prestamosPorVencer}
          loading={resumen.isLoading}
          subtitle="Préstamos que vencen en 3 días"
          linkTo={APP_ROUTES.PRESTAMOS}
          linkLabel="Ver préstamos"
        />
      </div>

      <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <MetricCard
          icon={CheckCircle2}
          iconColor="text-emerald-500"
          label="Devueltos Este Mes"
          value={data?.prestamosDevueltosEsteMes}
          loading={resumen.isLoading}
          subtitle="Préstamos cerrados en el mes actual"
          linkTo={APP_ROUTES.PRESTAMOS}
          linkLabel="Ver historial"
        />
        <MetricCard
          icon={Users}
          iconColor="text-sky-500"
          label="Autores"
          value={data?.totalAutores}
          loading={resumen.isLoading}
          subtitle="Autores registrados"
          linkTo={APP_ROUTES.AUTORES}
          linkLabel="Ver autores"
        />
        <MetricCard
          icon={Package2}
          iconColor="text-indigo-500"
          label="Categorías"
          value={data?.totalCategorias}
          loading={resumen.isLoading}
          subtitle="Clasificaciones del catálogo"
          linkTo={APP_ROUTES.CATEGORIAS}
          linkLabel="Ver categorías"
        />
        <MetricCard
          icon={Trophy}
          iconColor="text-violet-500"
          label="Top Préstamos"
          value={librosMasPrestados[0]?.total}
          loading={resumen.isLoading}
          subtitle={librosMasPrestados[0]?.nombre ?? 'Sin actividad registrada'}
          linkTo={APP_ROUTES.PRESTAMOS}
          linkLabel="Ver actividad"
        />
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <AlertTriangle className="h-5 w-5 text-rose-500" /> Préstamos Vencidos
            </CardTitle>
            <CardDescription>Casos que requieren seguimiento inmediato.</CardDescription>
          </CardHeader>
          <CardContent>
            {resumen.isLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-12 w-full rounded-xl" />
                ))}
              </div>
            ) : prestamosVencidos.length === 0 ? (
              <div className="py-6 flex items-center justify-center gap-2 text-sm font-semibold text-muted-foreground">
                <CheckCircle2 className="h-5 w-5 text-emerald-500" />
                No hay préstamos vencidos
              </div>
            ) : (
              <div className="divide-y divide-border/60">
                {prestamosVencidos.map((p) => (
                  <PrestamoItem key={p.id} prestamo={p} />
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Timer className="h-5 w-5 text-orange-500" /> Próximos a Vencer
            </CardTitle>
            <CardDescription>Préstamos que vencen durante los próximos 3 días.</CardDescription>
          </CardHeader>
          <CardContent>
            {resumen.isLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map((i) => (
                  <Skeleton key={i} className="h-12 w-full rounded-xl" />
                ))}
              </div>
            ) : prestamosProximosAVencer.length === 0 ? (
              <div className="py-6 flex items-center justify-center gap-2 text-sm font-semibold text-muted-foreground">
                <CheckCircle2 className="h-5 w-5 text-emerald-500" />
                Sin vencimientos próximos
              </div>
            ) : (
              <div className="divide-y divide-border/60">
                {prestamosProximosAVencer.map((p) => (
                  <PrestamoItem key={p.id} prestamo={p} />
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 md:grid-cols-3">
        <div className="md:col-span-2 space-y-6">
          <QuickActions />
          <Card className="border border-border bg-card/40 backdrop-blur-md">
            <CardHeader className="flex flex-row items-center justify-between pb-3">
              <div>
                <CardTitle className="flex items-center gap-2 text-lg">
                  <Clock className="h-5 w-5 text-primary" /> Préstamos Recientes
                </CardTitle>
                <CardDescription>Últimos registros de préstamo en el sistema.</CardDescription>
              </div>
              <Button
                variant="outline"
                size="sm"
                className="rounded-xl font-bold text-xs"
                render={<Link to={APP_ROUTES.PRESTAMOS} />}
              >
                Ver todos
              </Button>
            </CardHeader>
            <CardContent>
              {resumen.isLoading ? (
                <div className="space-y-3">
                  {[1, 2, 3].map((i) => (
                    <Skeleton key={i} className="h-12 w-full rounded-xl" />
                  ))}
                </div>
              ) : prestamosRecientes.length === 0 ? (
                <div className="py-8 flex flex-col items-center text-center">
                  <Clock className="h-8 w-8 text-muted-foreground/60 mb-2" />
                  <p className="text-sm font-semibold text-muted-foreground">
                    No hay préstamos recientes
                  </p>
                </div>
              ) : (
                <div className="divide-y divide-border/60">
                  {prestamosRecientes.map((p) => (
                    <PrestamoItem key={p.id} prestamo={p} />
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <AlertsSection isLoading={resumen.isLoading} alertasInventario={alertasInventario} />
      </div>

      <RankingsSection
        isLoading={resumen.isLoading}
        librosMasPrestados={librosMasPrestados}
        lectoresMasActivos={lectoresMasActivos}
        categoriasConMasLibros={categoriasConMasLibros}
        autoresConMasLibros={autoresConMasLibros}
      />
    </div>
  );
};

