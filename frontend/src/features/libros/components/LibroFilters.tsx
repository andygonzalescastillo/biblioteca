import type { CategoriaResponse } from '@/features/categorias/types/categoria';
import type { AutorResponse } from '@/features/autores/types/autor';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';
import { FiltrosBusquedaEstado } from '@/components/shared/FiltrosBusquedaEstado';
import { CampoSelect } from '@/components/shared/CampoSelect';
import { Separator } from '@/components/ui/separator';

interface LibroFiltersProps {
  buscar: string;
  onBuscarChange: (value: string) => void;
  estadoFilter: FiltroEstado;
  onEstadoFilterChange: (value: FiltroEstado) => void;
  categoriaId: number | '';
  onCategoriaIdChange: (value: number | '') => void;
  autorId: number | '';
  onAutorIdChange: (value: number | '') => void;
  categorias: CategoriaResponse[];
  autores: AutorResponse[];
}

export const LibroFilters = ({
  buscar,
  onBuscarChange,
  estadoFilter,
  onEstadoFilterChange,
  categoriaId,
  onCategoriaIdChange,
  autorId,
  onAutorIdChange,
  categorias,
  autores,
}: LibroFiltersProps) => {
  return (
    <FiltrosBusquedaEstado
      buscar={buscar}
      alCambiarBuscar={onBuscarChange}
      filtroEstado={estadoFilter}
      alCambiarFiltroEstado={onEstadoFilterChange}
      placeholder="Buscar por título o ISBN..."
    >
      <Separator className="mt-2" />
      <div className="grid gap-3 grid-cols-1 sm:grid-cols-2 pt-2">
        <CampoSelect
          label="Categoría"
          value={categoriaId === '' ? '' : String(categoriaId)}
          onValueChange={(v) => onCategoriaIdChange(v === '' ? '' : Number(v))}
          opciones={[
            { value: '', label: 'Todas las Categorías' },
            ...categorias.map((cat) => ({ value: String(cat.id), label: cat.nombre })),
          ]}
          placeholder="Todas las Categorías"
        />
        <CampoSelect
          label="Autor"
          value={autorId === '' ? '' : String(autorId)}
          onValueChange={(v) => onAutorIdChange(v === '' ? '' : Number(v))}
          opciones={[
            { value: '', label: 'Todos los Autores' },
            ...autores.map((aut) => ({ value: String(aut.id), label: aut.nombre })),
          ]}
          placeholder="Todos los Autores"
        />
      </div>
    </FiltrosBusquedaEstado>
  );
};

