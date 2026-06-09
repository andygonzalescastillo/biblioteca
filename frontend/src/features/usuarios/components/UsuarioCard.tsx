import { Mail, Phone, MapPin, Calendar, User } from 'lucide-react';
import type { UsuarioResponse } from '../types/usuario';
import { Card, CardHeader, CardTitle } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { AccionesTarjeta } from '@/components/shared/AccionesTarjeta';
import { AvatarEntidad } from '@/components/shared/AvatarEntidad';
import { imagenService } from '@/shared/services/imagenService';
import { formatearFecha } from '@/lib/formatearFecha';

interface UsuarioCardProps {
  usuario: UsuarioResponse;
  onEdit: (usuario: UsuarioResponse) => void;
  onDelete: (id: number) => void;
  onActivate: (id: number) => void;
}

export const UsuarioCard = ({ usuario, onEdit, onDelete, onActivate }: UsuarioCardProps) => {
  const avatarUrl = usuario.foto
    ? imagenService.obtenerUrl(usuario.foto.urlAlmacenamiento)
    : undefined;

  return (
    <Card
      className={`relative group border border-border/80 bg-card/40 backdrop-blur-md flex flex-col justify-between hover:scale-[1.02] hover:shadow-md transition-all duration-300 ${!usuario.estado ? 'opacity-70 bg-muted/20' : ''
        }`}
    >
      <CardHeader className="pb-2">
        <div className="flex items-start gap-3 min-w-0">
          <AvatarEntidad
            src={avatarUrl}
            alt={usuario.nombre}
            icon={User}
          />

          <div className="min-w-0 flex-1">
            <div className="flex items-center justify-between gap-2 min-w-0">
              <CardTitle
                className="font-bold text-base truncate group-hover:text-primary transition-colors duration-200 flex-1 min-w-0"
                title={usuario.nombre}
              >
                {usuario.nombre}
              </CardTitle>
              <Badge
                className={
                  usuario.estado
                    ? 'bg-emerald-500 text-white font-semibold shrink-0'
                    : 'bg-zinc-500 text-white font-semibold shrink-0'
                }
              >
                {usuario.estado ? 'Activo' : 'Inactivo'}
              </Badge>
            </div>
            <p className="text-[10px] font-semibold text-muted-foreground flex items-center gap-1 mt-1 uppercase tracking-wide">
              <Calendar className="h-3 w-3 text-blue-500" />
              Registrado: {formatearFecha(usuario.fechaRegistro)}
            </p>
          </div>
        </div>

        <div className="mt-3.5 space-y-1.5 text-xs text-muted-foreground font-medium">
          <div className="flex items-center gap-2">
            <Mail className="h-3.5 w-3.5 shrink-0 text-blue-500/70" />
            <span className="truncate">{usuario.email}</span>
          </div>

          {usuario.telefono && (
            <div className="flex items-center gap-2">
              <Phone className="h-3.5 w-3.5 shrink-0 text-blue-500/70" />
              <span className="truncate">{usuario.telefono}</span>
            </div>
          )}

          {usuario.direccion && (
            <div className="flex items-center gap-2">
              <MapPin className="h-3.5 w-3.5 shrink-0 text-blue-500/70" />
              <span className="truncate">{usuario.direccion}</span>
            </div>
          )}
        </div>
      </CardHeader>

      <AccionesTarjeta
        estado={usuario.estado}
        idEntidad={usuario.id}
        alEditar={() => onEdit(usuario)}
        alEliminar={onDelete}
        alActivar={onActivate}
        tituloEditar="Editar Usuario"
        tituloEliminar="Eliminar Usuario"
        tituloActivar="Activar Usuario"
      />
    </Card>
  );
};

