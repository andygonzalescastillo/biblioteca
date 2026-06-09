import { Search } from 'lucide-react';
import type { EstadoPrestamo } from '../types/prestamo';
import { Input } from '@/components/ui/input';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';

interface PrestamoFiltersProps {
  buscarUsuario: string;
  onBuscarUsuarioChange: (value: string) => void;
  estadoFilter: EstadoPrestamo | 'TODOS';
  onEstadoFilterChange: (value: EstadoPrestamo | 'TODOS') => void;
}

const ESTADOS: { value: EstadoPrestamo | 'TODOS'; label: string }[] = [
  { value: 'TODOS', label: 'Todos' },
  { value: 'ACTIVO', label: 'Activos' },
  { value: 'DEVUELTO', label: 'Devueltos' },
  { value: 'ATRASADO', label: 'Atrasados' },
];

export const PrestamoFilters = ({
  buscarUsuario,
  onBuscarUsuarioChange,
  estadoFilter,
  onEstadoFilterChange,
}: PrestamoFiltersProps) => {
  return (
    <div className="flex flex-col gap-4 bg-card/25 border border-border/80 p-4 rounded-2xl backdrop-blur-md">
      <div className="relative">
        <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
        <Input
          placeholder="Buscar por nombre de lector..."
          value={buscarUsuario}
          onChange={(e) => onBuscarUsuarioChange(e.target.value)}
          className="pl-10 rounded-xl"
        />
      </div>

      <Tabs
        value={estadoFilter}
        onValueChange={(v) => onEstadoFilterChange(v as EstadoPrestamo | 'TODOS')}
      >
        <TabsList variant="line" className="w-full flex-wrap h-auto gap-1 bg-transparent p-0">
          {ESTADOS.map((e) => (
            <TabsTrigger
              key={e.value}
              value={e.value}
              className="rounded-full px-3 py-1 text-xs font-bold data-active:bg-primary/15 data-active:text-primary"
            >
              {e.label}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>
    </div>
  );
};

