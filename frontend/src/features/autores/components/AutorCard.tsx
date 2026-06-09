import { User, Calendar } from 'lucide-react';
import type { AutorResponse } from '../types/autor';
import { Card, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AccionesTarjeta } from '@/components/shared/AccionesTarjeta';
import { AvatarEntidad } from '@/components/shared/AvatarEntidad';
import { imagenService } from '@/shared/services/imagenService';
import { formatearFecha } from '@/lib/formatearFecha';

interface AutorCardProps {
  autor: AutorResponse;
  onEdit: (autor: AutorResponse) => void;
  onDelete: (id: number) => void;
  onActivate: (id: number) => void;
}

export const AutorCard = ({ autor, onEdit, onDelete, onActivate }: AutorCardProps) => {
  const fotoUrl = autor.foto ? imagenService.obtenerUrl(autor.foto.urlAlmacenamiento) : undefined;

  return (
    <Card
      className={`relative group border border-border/80 bg-card/40 backdrop-blur-md flex flex-col justify-between hover:scale-[1.02] hover:shadow-md transition-all duration-300 ${!autor.estado ? 'opacity-70 bg-muted/20' : ''
        }`}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start gap-3 min-w-0">
          <AvatarEntidad
            src={fotoUrl}
            alt={autor.nombre}
            icon={User}
          />
          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between gap-2 min-w-0">
              <CardTitle
                className="font-bold text-base truncate group-hover:text-primary transition-colors duration-200 flex-1 min-w-0"
                title={autor.nombre}
              >
                {autor.nombre}
              </CardTitle>
              <Badge
                className={
                  autor.estado ? 'bg-emerald-500 text-white font-semibold shrink-0' : 'bg-zinc-500 text-white font-semibold shrink-0'
                }
              >
                {autor.estado ? 'Activo' : 'Inactivo'}
              </Badge>
            </div>
            <p className="text-[10px] font-semibold text-muted-foreground flex items-center gap-1 mt-1 uppercase tracking-wide">
              <Calendar className="h-3 w-3 text-blue-500" />
              {formatearFecha(autor.fechaNacimiento)}
            </p>
          </div>
        </div>
        <CardDescription
          className="text-xs line-clamp-3 min-h-18 mt-3 font-medium text-muted-foreground"
          title={autor.biografia || undefined}
        >
          {autor.biografia || 'Sin biografía disponible en este momento.'}
        </CardDescription>
      </CardHeader>
      <AccionesTarjeta
        estado={autor.estado}
        idEntidad={autor.id}
        alEditar={() => onEdit(autor)}
        alEliminar={onDelete}
        alActivar={onActivate}
        tituloEditar="Editar Autor"
        tituloEliminar="Eliminar Autor"
        tituloActivar="Activar Autor"
      />
    </Card>
  );
};

