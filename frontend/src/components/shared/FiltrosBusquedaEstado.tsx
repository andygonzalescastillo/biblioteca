import type { ReactNode } from 'react';
import { Search } from 'lucide-react';
import { Input } from '@/components/ui/input';
import { CampoSelect } from '@/components/shared/CampoSelect';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';

const OPCIONES_ESTADO = [
  { value: 'todos', label: 'Todos' },
  { value: 'activos', label: 'Activos' },
  { value: 'inactivos', label: 'Inactivos' },
] as const;

interface FiltrosBusquedaEstadoProps {
  buscar: string;
  alCambiarBuscar: (valor: string) => void;
  filtroEstado: FiltroEstado;
  alCambiarFiltroEstado: (valor: FiltroEstado) => void;
  placeholder?: string;
  children?: ReactNode;
}

export const FiltrosBusquedaEstado = ({
  buscar,
  alCambiarBuscar,
  filtroEstado,
  alCambiarFiltroEstado,
  placeholder = 'Buscar...',
  children,
}: FiltrosBusquedaEstadoProps) => {
  return (
    <div className="flex flex-col gap-4 bg-card/25 border border-border/80 p-4 rounded-2xl backdrop-blur-md">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end">
        <div className="relative flex-1">
          <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder={placeholder}
            value={buscar}
            onChange={(e) => alCambiarBuscar(e.target.value)}
            className="pl-10 rounded-xl"
          />
        </div>

        <CampoSelect
          label="Estado"
          value={filtroEstado}
          onValueChange={(v) => alCambiarFiltroEstado(v as FiltroEstado)}
          opciones={[...OPCIONES_ESTADO]}
          placeholder="Estado"
          className="sm:w-44"
        />
      </div>

      {children}
    </div>
  );
};

