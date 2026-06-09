import { Link } from 'react-router-dom';
import { BookMarked, PlusCircle, UserPlus, ArrowRight, Sparkles } from 'lucide-react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { APP_ROUTES } from '@/shared/constants/appRoutes';

export const QuickActions = () => {
  return (
    <Card className="border border-border bg-card/45 backdrop-blur-md overflow-hidden">
      <CardHeader className="pb-3">
        <CardTitle className="flex items-center gap-2 text-lg">
          <Sparkles className="h-5 w-5 text-amber-500 animate-pulse" /> Acciones Rápidas
        </CardTitle>
        <CardDescription>Accesos directos para la administración diaria.</CardDescription>
      </CardHeader>
      <CardContent className="grid gap-4 sm:grid-cols-3">
        <Link
          to={APP_ROUTES.PRESTAMOS}
          className="group relative flex flex-col justify-between p-4 rounded-xl border border-border/80 bg-muted/20 hover:bg-primary/5 hover:border-primary/40 hover:shadow-md transition-all duration-300"
        >
          <div className="h-9 w-9 rounded-lg bg-primary/10 text-primary flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <BookMarked className="h-5 w-5" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-foreground flex items-center gap-1">
              Registrar Préstamo <ArrowRight className="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
            </h4>
            <p className="text-[11px] text-muted-foreground mt-1">Crear préstamo a lector</p>
          </div>
        </Link>

        <Link
          to={APP_ROUTES.LIBROS}
          className="group relative flex flex-col justify-between p-4 rounded-xl border border-border/80 bg-muted/20 hover:bg-violet-500/5 hover:border-violet-500/40 hover:shadow-md transition-all duration-300"
        >
          <div className="h-9 w-9 rounded-lg bg-violet-500/10 text-violet-500 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <PlusCircle className="h-5 w-5" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-foreground flex items-center gap-1">
              Agregar Libro <ArrowRight className="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
            </h4>
            <p className="text-[11px] text-muted-foreground mt-1">Registrar nuevo título</p>
          </div>
        </Link>

        <Link
          to={APP_ROUTES.USUARIOS}
          className="group relative flex flex-col justify-between p-4 rounded-xl border border-border/80 bg-muted/20 hover:bg-blue-500/5 hover:border-blue-500/40 hover:shadow-md transition-all duration-300"
        >
          <div className="h-9 w-9 rounded-lg bg-blue-500/10 text-blue-500 flex items-center justify-center mb-4 group-hover:scale-110 transition-transform">
            <UserPlus className="h-5 w-5" />
          </div>
          <div>
            <h4 className="font-bold text-sm text-foreground flex items-center gap-1">
              Registrar Lector <ArrowRight className="h-3 w-3 opacity-0 group-hover:opacity-100 transition-opacity" />
            </h4>
            <p className="text-[11px] text-muted-foreground mt-1">Dar de alta nuevo usuario</p>
          </div>
        </Link>
      </CardContent>
    </Card>
  );
};

