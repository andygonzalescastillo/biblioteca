import { Link } from 'react-router-dom';
import { Package2, CheckCircle2, ArrowRight } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader } from '@/components/ui/card';
import { Skeleton } from '@/components/ui/skeleton';
import { Badge } from '@/components/ui/badge';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

interface InventarioAlerta {
  id: number;
  titulo: string;
  stock: number;
  categoriaNombre: string;
}

interface AlertsSectionProps {
  isLoading: boolean;
  alertasInventario: InventarioAlerta[];
}

export const AlertsSection = ({ isLoading, alertasInventario }: AlertsSectionProps) => {
  return (
    <Card className="border border-border bg-card/40 backdrop-blur-md h-full flex flex-col justify-between">
      <div>
        <CardHeader className="pb-3">
          <h3 className="flex items-center gap-2 text-lg font-bold">
            <Package2 className="h-5 w-5 text-violet-500" /> Alertas de Inventario
          </h3>
          <CardDescription>Libros con stock crítico o agotados.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          {isLoading ? (
            <div className="space-y-3">
              {[1, 2, 3, 4].map((i) => (
                <Skeleton key={i} className="h-14 w-full rounded-xl" />
              ))}
            </div>
          ) : alertasInventario.length === 0 ? (
            <div className="py-12 flex flex-col items-center justify-center text-center">
              <div className="h-12 w-12 rounded-full bg-emerald-500/10 text-emerald-500 flex items-center justify-center mb-3">
                <CheckCircle2 className="h-6 w-6" />
              </div>
              <p className="text-sm font-bold text-foreground">Inventario al día</p>
              <p className="text-[11px] text-muted-foreground mt-1 max-w-50">
                Todos los libros cuentan con unidades suficientes.
              </p>
            </div>
          ) : (
            <div className="space-y-3">
              {alertasInventario.map((libro) => {
                const isAgotado = libro.stock === 0;
                return (
                  <div
                    key={libro.id}
                    className="flex items-center justify-between p-3 rounded-xl border border-border bg-muted/10 hover:border-violet-500/20 hover:bg-muted/20 transition-colors"
                  >
                    <div className="min-w-0 mr-2">
                      <h4 className="font-bold text-xs truncate text-foreground">
                        {libro.titulo}
                      </h4>
                      <p className="text-[10px] text-muted-foreground truncate">
                        {libro.categoriaNombre}
                      </p>
                    </div>
                    <Badge
                      className={`shrink-0 font-bold text-[9px] ${isAgotado
                          ? 'bg-rose-500 hover:bg-rose-600 text-white'
                          : 'bg-amber-500 hover:bg-amber-600 text-white'
                        }`}
                    >
                      {isAgotado ? 'Agotado' : `${libro.stock} unid.`}
                    </Badge>
                  </div>
                );
              })}
            </div>
          )}
        </CardContent>
      </div>

      <CardContent className="pt-0 pb-4 border-t border-border/50 bg-muted/10 py-3 rounded-b-2xl">
        <Link
          to={APP_ROUTES.LIBROS}
          className="text-xs text-violet-500 hover:underline font-bold flex items-center justify-center gap-1"
        >
          Gestionar inventario de libros <ArrowRight className="h-3 w-3" />
        </Link>
      </CardContent>
    </Card>
  );
};

