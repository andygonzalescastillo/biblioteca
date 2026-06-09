import type { CategoriaResponse } from '../types/categoria';
import { Card, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AccionesTarjeta } from '@/components/shared/AccionesTarjeta';

interface CategoriaCardProps {
  cat: CategoriaResponse;
  onEdit: (cat: CategoriaResponse) => void;
  onDelete: (id: number) => void;
  onActivate: (id: number) => void;
}

export const CategoriaCard = ({ cat, onEdit, onDelete, onActivate }: CategoriaCardProps) => {
  return (
    <Card
      className={`relative group border border-border/80 bg-card/40 backdrop-blur-md flex flex-col justify-between hover:scale-[1.02] hover:shadow-md transition-all duration-300 ${!cat.estado ? 'opacity-70 bg-muted/20' : ''
        }`}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start justify-between gap-2 min-w-0">
          <CardTitle
            className="font-bold text-base truncate group-hover:text-primary transition-colors duration-200 flex-1 min-w-0"
            title={cat.nombre}
          >
            {cat.nombre}
          </CardTitle>
          <Badge
            className={cat.estado ? 'bg-emerald-500 text-white font-semibold shrink-0' : 'bg-zinc-500 text-white font-semibold shrink-0'}
          >
            {cat.estado ? 'Activo' : 'Inactivo'}
          </Badge>
        </div>
        <CardDescription
          className="text-xs line-clamp-3 min-h-18 mt-2 font-medium"
          title={cat.descripcion || undefined}
        >
          {cat.descripcion || 'Sin descripción disponible.'}
        </CardDescription>
      </CardHeader>
      <AccionesTarjeta
        estado={cat.estado}
        idEntidad={cat.id}
        alEditar={() => onEdit(cat)}
        alEliminar={onDelete}
        alActivar={onActivate}
      />
    </Card>
  );
};

