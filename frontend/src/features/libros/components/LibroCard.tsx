import { BookOpen, Layers } from 'lucide-react';
import type { LibroResponse } from '../types/libro';
import { Card, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AccionesTarjeta } from '@/components/shared/AccionesTarjeta';
import { imagenService } from '@/shared/services/imagenService';

interface LibroCardProps {
  libro: LibroResponse;
  onEdit: (libro: LibroResponse) => void;
  onDelete: (id: number) => void;
  onActivate: (id: number) => void;
}

export const LibroCard = ({ libro, onEdit, onDelete, onActivate }: LibroCardProps) => {
  const coverUrl = libro.portada ? imagenService.obtenerUrl(libro.portada.urlAlmacenamiento) : '';
  const autoresNames = libro.autores.map((a) => a.nombre).join(', ') || 'Autor Desconocido';

  
  let stockVariant = 'bg-emerald-500 hover:bg-emerald-600';
  let stockLabel = `Disponible (${libro.stock})`;
  if (libro.stock === 0) {
    stockVariant = 'bg-rose-500 hover:bg-rose-600';
    stockLabel = 'Agotado';
  } else if (libro.stock <= 5) {
    stockVariant = 'bg-amber-500 hover:bg-amber-600';
    stockLabel = `¡Pocas unidades! (${libro.stock})`;
  }

  return (
    <Card
      className={`relative group border border-border/80 bg-card/40 backdrop-blur-md flex flex-col justify-between overflow-hidden hover:scale-[1.02] hover:shadow-lg transition-all duration-300 pt-0 ${!libro.estado ? 'opacity-70 bg-muted/20' : ''
        }`}
    >
      {}
      <div className="relative aspect-2/3 w-full overflow-hidden bg-zinc-900 border-b border-border/40 flex items-center justify-center">
        {coverUrl ? (
          <img
            src={coverUrl}
            alt={libro.titulo}
            className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
            loading="lazy"
          />
        ) : (
          
          <div className="absolute inset-0 bg-linear-to-br from-indigo-950 via-slate-900 to-violet-950 p-6 flex flex-col justify-between text-left select-none transition-transform duration-500 group-hover:scale-[1.03]">
            {}
            <div className="absolute left-0 top-0 bottom-0 w-2.5 bg-black/35 backdrop-blur-xs" />
            <div className="absolute left-3 top-0 bottom-0 w-0.5 bg-white/5" />

            <div className="space-y-2 pl-3">
              <span className="inline-flex items-center gap-1 text-[9px] font-black tracking-widest uppercase text-violet-400">
                <BookOpen className="h-3 w-3" /> Biblioteca
              </span>
              <h4 className="font-extrabold text-sm text-zinc-100 tracking-tight leading-snug line-clamp-4">
                {libro.titulo}
              </h4>
            </div>

            <div className="space-y-1.5 pl-3 border-t border-white/10 pt-4">
              <span className="block text-[8px] font-bold text-zinc-400 tracking-wider uppercase">Escrito por</span>
              <p className="font-bold text-[10px] text-violet-300 truncate">
                {autoresNames}
              </p>
            </div>
          </div>
        )}

        {}
        <div className="absolute top-3.5 right-3.5 flex flex-col gap-2 z-10">
          <Badge className={`${stockVariant} text-white font-semibold shadow-md`}>
            {stockLabel}
          </Badge>
          {!libro.estado && (
            <Badge className="bg-zinc-600 text-white font-semibold shadow-md">
              Inactivo
            </Badge>
          )}
        </div>
      </div>

      {}
      <CardHeader className="p-4 pb-2">
        <div className="flex items-center gap-1.5">
          <Layers className="h-3.5 w-3.5 text-violet-500 shrink-0" />
          <Badge variant="outline" className="text-[10px] border-violet-500/20 text-violet-500 bg-violet-500/5 font-semibold">
            {libro.categoria.nombre}
          </Badge>
        </div>
         <CardTitle
          className="font-bold text-base truncate group-hover:text-primary transition-colors duration-200 mt-2"
          title={libro.titulo}
        >
          {libro.titulo}
        </CardTitle>
        <CardDescription className="text-[10px] font-mono mt-1 text-muted-foreground">
          ISBN: {libro.isbn}
        </CardDescription>
      </CardHeader>

      {}
      <div className="px-4 pb-4 grow flex flex-col justify-end">
        <p
          className="text-xs text-muted-foreground font-medium line-clamp-1"
          title={autoresNames}
        >
          <span className="font-bold">Autores:</span> {autoresNames}
        </p>
      </div>

      <AccionesTarjeta
        className="px-4"
        estado={libro.estado}
        idEntidad={libro.id}
        alEditar={() => onEdit(libro)}
        alEliminar={onDelete}
        alActivar={onActivate}
        tituloEditar="Editar Libro"
        tituloEliminar="Eliminar Libro"
        tituloActivar="Activar Libro"
      />
    </Card>
  );
};

