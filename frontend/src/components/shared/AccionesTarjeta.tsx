import { Edit2, MoreHorizontal, RotateCcw, Trash2 } from 'lucide-react';
import { CardContent } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger, } from '@/components/ui/dropdown-menu';
import { Tooltip, TooltipContent, TooltipTrigger } from '@/components/ui/tooltip';
import { cn } from '@/lib/utils';

interface AccionesTarjetaProps {
  estado: boolean;
  idEntidad: number;
  alEditar: () => void;
  alEliminar: (id: number) => void;
  alActivar: (id: number) => void;
  tituloEditar?: string;
  tituloEliminar?: string;
  tituloActivar?: string;
  className?: string;
}

export const AccionesTarjeta = ({
  estado,
  idEntidad,
  alEditar,
  alEliminar,
  alActivar,
  tituloEditar = 'Editar',
  tituloEliminar = 'Desactivar',
  tituloActivar = 'Reactivar',
  className,
}: AccionesTarjetaProps) => {
  return (
    <CardContent
      className={cn(
        'pt-2 pb-3 flex items-center justify-end gap-2 border-t border-border/40 bg-muted/10',
        className,
      )}
    >
      <Tooltip>
        <TooltipTrigger
          render={
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 rounded-lg hover:bg-primary/10 hover:text-primary"
              onClick={alEditar}
            />
          }
        >
          <Edit2 className="h-3.5 w-3.5" />
        </TooltipTrigger>
        <TooltipContent>{tituloEditar}</TooltipContent>
      </Tooltip>

      <DropdownMenu>
        <DropdownMenuTrigger
          render={
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 rounded-lg hover:bg-muted"
            />
          }
        >
          <MoreHorizontal className="h-3.5 w-3.5" />
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="w-40">
          <DropdownMenuItem onClick={alEditar}>
            <Edit2 className="h-3.5 w-3.5" />
            {tituloEditar}
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          {estado ? (
            <DropdownMenuItem
              variant="destructive"
              onClick={() => alEliminar(idEntidad)}
            >
              <Trash2 className="h-3.5 w-3.5" />
              {tituloEliminar}
            </DropdownMenuItem>
          ) : (
            <DropdownMenuItem onClick={() => alActivar(idEntidad)}>
              <RotateCcw className="h-3.5 w-3.5 text-emerald-500" />
              {tituloActivar}
            </DropdownMenuItem>
          )}
        </DropdownMenuContent>
      </DropdownMenu>
    </CardContent>
  );
};

