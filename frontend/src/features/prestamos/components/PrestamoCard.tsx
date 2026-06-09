import { User, BookOpen, Calendar, Clock, CheckCircle2, AlertTriangle, RotateCcw } from 'lucide-react';
import type { PrestamoResponse, EstadoPrestamo } from '../types/prestamo';
import { Card, CardContent, CardHeader } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { formatearFecha } from '@/lib/formatearFecha';
import { AvatarEntidad } from '@/components/shared/AvatarEntidad';
import { imagenService } from '@/shared/services/imagenService';

interface PrestamoCardProps {
  prestamo: PrestamoResponse;
  onDevolver: (id: number) => void;
  isDevolviendo: boolean;
}

const ESTADO_CONFIG: Record<
  EstadoPrestamo,
  { label: string; icon: React.ReactNode; className: string }
> = {
  ACTIVO: {
    label: 'Activo',
    icon: <Clock className="h-3 w-3" />,
    className: 'bg-blue-500/15 text-blue-500 border-blue-500/20',
  },
  DEVUELTO: {
    label: 'Devuelto',
    icon: <CheckCircle2 className="h-3 w-3" />,
    className: 'bg-emerald-500/15 text-emerald-500 border-emerald-500/20',
  },
  ATRASADO: {
    label: 'Atrasado',
    icon: <AlertTriangle className="h-3 w-3" />,
    className: 'bg-rose-500/15 text-rose-500 border-rose-500/20',
  },
};

export const PrestamoCard = ({ prestamo, onDevolver, isDevolviendo }: PrestamoCardProps) => {
  const config = ESTADO_CONFIG[prestamo.estado];
  const totalLibros = prestamo.detalles.reduce((sum, d) => sum + d.cantidad, 0);
  const esAtrasado = prestamo.estado === 'ATRASADO';
  const esActivo = prestamo.estado === 'ACTIVO' || esAtrasado;

  const avatarUrl = prestamo.usuario.foto
    ? imagenService.obtenerUrl(prestamo.usuario.foto.urlAlmacenamiento)
    : undefined;

  return (
    <Card
      className={`relative group border border-border/80 bg-card/40 backdrop-blur-md overflow-hidden hover:shadow-lg transition-all duration-300 flex flex-col h-95 justify-between ${esAtrasado ? 'border-rose-500/30' : ''
        }`}
    >
      <CardHeader className="pt-4 pb-2 px-5 shrink-0">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3 min-w-0">
            <AvatarEntidad
              src={avatarUrl}
              alt={prestamo.usuario.nombre}
              icon={User}
            />
            <div className="min-w-0">
              <p className="font-bold text-sm truncate">{prestamo.usuario.nombre}</p>
              <p className="text-[11px] text-muted-foreground truncate">{prestamo.usuario.email}</p>
            </div>
          </div>

          <Badge className={`shrink-0 border text-[10px] font-bold flex items-center gap-1 ${config.className}`}>
            {config.icon}
            {config.label}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="px-5 pb-4 pt-1 flex-1 flex flex-col justify-between gap-3">
        <div className="space-y-1.5">
          <p className="text-[10px] font-bold text-muted-foreground uppercase tracking-wider flex items-center gap-1.5">
            <BookOpen className="h-3 w-3" />
            {totalLibros} libro{totalLibros !== 1 ? 's' : ''} prestado{totalLibros !== 1 ? 's' : ''}
          </p>
          <div className="space-y-1.5 h-27.5 min-h-27.5 max-h-27.5 overflow-y-auto pr-1">
            {prestamo.detalles.map((detalle) => {
              const bookCover = detalle.libro.portada
                ? imagenService.obtenerUrl(detalle.libro.portada.urlAlmacenamiento)
                : undefined;
              return (
                <div
                  key={detalle.id}
                  className="flex items-center justify-between gap-2.5 text-xs bg-muted/20 border border-border/30 rounded-lg p-1.5"
                >
                  <div className="flex items-center gap-2 min-w-0 flex-1">
                    {bookCover ? (
                      <img
                        src={bookCover}
                        alt={detalle.libro.titulo}
                        className="h-10 w-7 shrink-0 object-cover rounded-xs border border-border/40 shadow-xs"
                      />
                    ) : (
                      <div className="h-10 w-7 shrink-0 rounded-xs bg-linear-to-br from-indigo-950 to-slate-900 flex items-center justify-center border border-border/40">
                        <BookOpen className="h-3.5 w-3.5 text-slate-500" />
                      </div>
                    )}
                    <span className="font-semibold truncate text-foreground/90">{detalle.libro.titulo}</span>
                  </div>
                  <Badge variant="outline" className="text-[9px] shrink-0 font-bold bg-background/50">
                    ×{detalle.cantidad}
                  </Badge>
                </div>
              );
            })}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2 text-[11px] shrink-0">
          <div className="bg-muted/20 rounded-lg px-2.5 py-1.5">
            <p className="text-muted-foreground font-semibold mb-0.5 flex items-center gap-1">
              <Calendar className="h-3 w-3" /> Préstamo
            </p>
            <p className="font-bold text-foreground">{formatearFecha(prestamo.fechaPrestamo)}</p>
          </div>
          <div className={`rounded-lg px-2.5 py-1.5 ${esAtrasado ? 'bg-rose-500/10' : 'bg-muted/20'}`}>
            <p className={`font-semibold mb-0.5 flex items-center gap-1 ${esAtrasado ? 'text-rose-400' : 'text-muted-foreground'}`}>
              <Clock className="h-3 w-3" /> Límite
            </p>
            <p className={`font-bold ${esAtrasado ? 'text-rose-500' : 'text-foreground'}`}>
              {formatearFecha(prestamo.fechaDevolucionLimite)}
            </p>
          </div>
        </div>

        <div className="h-9 shrink-0 flex items-end">
          {esActivo ? (
            <Button
              variant="outline"
              size="sm"
              className={`w-full rounded-xl font-bold h-8 text-xs gap-1.5 transition-all ${esAtrasado
                ? 'border-rose-500/40 text-rose-500 hover:bg-rose-500/10'
                : 'hover:bg-primary/10 hover:text-primary hover:border-primary/30'
                }`}
              onClick={() => onDevolver(prestamo.id)}
              disabled={isDevolviendo}
            >
              <RotateCcw className="h-3 w-3" />
              {isDevolviendo ? 'Procesando...' : 'Registrar Devolución'}
            </Button>
          ) : (
            prestamo.fechaDevolucionReal && (
              <div className="w-full bg-emerald-500/10 border border-emerald-500/20 text-emerald-500 rounded-lg px-2.5 py-1.5 text-[11px] font-bold flex items-center gap-1.5">
                <CheckCircle2 className="h-3.5 w-3.5" /> Devuelto el {formatearFecha(prestamo.fechaDevolucionReal)}
              </div>
            )
          )}
        </div>
      </CardContent>
    </Card>
  );
};

