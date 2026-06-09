import { Trophy, Users, Package2, Users2 } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import type { DashboardRankingResponse } from '../types/dashboard';

interface RankingsSectionProps {
  isLoading: boolean;
  librosMasPrestados: DashboardRankingResponse[];
  lectoresMasActivos: DashboardRankingResponse[];
  categoriasConMasLibros: DashboardRankingResponse[];
  autoresConMasLibros: DashboardRankingResponse[];
}

export const RankingsSection = ({
  isLoading,
  librosMasPrestados,
  lectoresMasActivos,
  categoriasConMasLibros,
  autoresConMasLibros,
}: RankingsSectionProps) => {
  return (
    <>
      <div className="grid gap-6 lg:grid-cols-2">
        {/* Libros Más Prestados */}
        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Trophy className="h-5 w-5 text-violet-500" /> Libros Más Prestados
            </CardTitle>
            <CardDescription>Títulos con mayor movimiento histórico.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              [1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-11 w-full rounded-xl" />)
            ) : librosMasPrestados.length === 0 ? (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">
                Aún no hay préstamos registrados
              </p>
            ) : (
              librosMasPrestados.map((item, index) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between rounded-xl border border-border bg-muted/10 p-3"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-bold text-foreground">
                      {index + 1}. {item.nombre}
                    </p>
                    <p className="text-[11px] text-muted-foreground">Unidades prestadas</p>
                  </div>
                  <Badge className="bg-violet-500 text-white hover:bg-violet-600">
                    {item.total}
                  </Badge>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        {/* Lectores Más Activos */}
        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Users className="h-5 w-5 text-blue-500" /> Lectores Más Activos
            </CardTitle>
            <CardDescription>Lectores con más unidades solicitadas.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              [1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-11 w-full rounded-xl" />)
            ) : lectoresMasActivos.length === 0 ? (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">
                Aún no hay lectores con préstamos
              </p>
            ) : (
              lectoresMasActivos.map((item, index) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between rounded-xl border border-border bg-muted/10 p-3"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-bold text-foreground">
                      {index + 1}. {item.nombre}
                    </p>
                    <p className="text-[11px] text-muted-foreground">Unidades solicitadas</p>
                  </div>
                  <Badge className="bg-blue-500 text-white hover:bg-blue-600">
                    {item.total}
                  </Badge>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Categorías con más libros */}
        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Package2 className="h-5 w-5 text-indigo-500" /> Categorías con Más Libros
            </CardTitle>
            <CardDescription>Áreas más fuertes del catálogo.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              [1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-11 w-full rounded-xl" />)
            ) : categoriasConMasLibros.length === 0 ? (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">
                Aún no hay libros categorizados
              </p>
            ) : (
              categoriasConMasLibros.map((item, index) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between rounded-xl border border-border bg-muted/10 p-3"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-bold text-foreground">
                      {index + 1}. {item.nombre}
                    </p>
                    <p className="text-[11px] text-muted-foreground">Libros activos</p>
                  </div>
                  <Badge className="bg-indigo-500 text-white hover:bg-indigo-600">
                    {item.total}
                  </Badge>
                </div>
              ))
            )}
          </CardContent>
        </Card>

        {/* Autores con más libros */}
        <Card className="border border-border bg-card/40 backdrop-blur-md">
          <CardHeader className="pb-3">
            <CardTitle className="flex items-center gap-2 text-lg">
              <Users2 className="h-5 w-5 text-sky-500" /> Autores con Más Libros
            </CardTitle>
            <CardDescription>Autores con mayor presencia en el catálogo.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {isLoading ? (
              [1, 2, 3, 4, 5].map((i) => <Skeleton key={i} className="h-11 w-full rounded-xl" />)
            ) : autoresConMasLibros.length === 0 ? (
              <p className="py-6 text-center text-sm font-semibold text-muted-foreground">
                Aún no hay autores asociados a libros
              </p>
            ) : (
              autoresConMasLibros.map((item, index) => (
                <div
                  key={item.id}
                  className="flex items-center justify-between rounded-xl border border-border bg-muted/10 p-3"
                >
                  <div className="min-w-0">
                    <p className="truncate text-sm font-bold text-foreground">
                      {index + 1}. {item.nombre}
                    </p>
                    <p className="text-[11px] text-muted-foreground">Libros activos</p>
                  </div>
                  <Badge className="bg-sky-500 text-white hover:bg-sky-600">
                    {item.total}
                  </Badge>
                </div>
              ))
            )}
          </CardContent>
        </Card>
      </div>
    </>
  );
};

