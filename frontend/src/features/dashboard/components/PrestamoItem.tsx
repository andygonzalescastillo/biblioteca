import type { PrestamoResponse } from '@/features/prestamos/types/prestamo';
import { Badge } from '@/components/ui/badge';
import { formatearFecha } from '@/lib/formatearFecha';

const STATUS_CONFIG: Record<string, { bg: string; label: string }> = {
  ACTIVO: { bg: 'bg-amber-500 hover:bg-amber-600', label: 'Activo' },
  DEVUELTO: { bg: 'bg-emerald-500 hover:bg-emerald-600', label: 'Devuelto' },
  ATRASADO: { bg: 'bg-rose-500 hover:bg-rose-600', label: 'Atrasado' },
};

interface PrestamoItemProps {
  prestamo: PrestamoResponse;
}

export const PrestamoItem = ({ prestamo }: PrestamoItemProps) => {
  const cfg = STATUS_CONFIG[prestamo.estado] ?? { bg: '', label: prestamo.estado };
  return (
    <div className="py-3 flex items-center justify-between gap-4">
      <div className="min-w-0">
        <p className="text-sm font-semibold truncate text-foreground">
          {prestamo.usuario.nombre}
        </p>
        <p className="text-xs text-muted-foreground truncate max-w-xs sm:max-w-md">
          {prestamo.detalles.map((d) => `${d.libro.titulo} (x${d.cantidad})`).join(', ')}
        </p>
      </div>
      <div className="flex items-center gap-3 shrink-0">
        <div className="text-right hidden sm:block">
          <span className="block text-[10px] font-bold text-muted-foreground uppercase">
            Préstamo
          </span>
          <span className="block text-xs font-semibold text-foreground">
            {prestamo.fechaPrestamo ? formatearFecha(prestamo.fechaPrestamo) : '-'}
          </span>
        </div>
        <Badge className={`${cfg.bg} text-white font-semibold`}>{cfg.label}</Badge>
      </div>
    </div>
  );
};

