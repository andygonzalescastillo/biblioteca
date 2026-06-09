import { ChevronLeft, ChevronRight } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group';

interface PaginacionProps {
  pagina: number;
  totalPaginas: number;
  totalElementos?: number;
  alCambiarPagina: (pagina: number) => void;
  tamanioPagina?: number;
  alCambiarTamanioPagina?: (tamanio: number) => void;
  opcionesTamanioPagina?: number[];
}

export const Paginacion = ({
  pagina,
  totalPaginas,
  totalElementos,
  alCambiarPagina,
  tamanioPagina,
  alCambiarTamanioPagina,
  opcionesTamanioPagina,
}: PaginacionProps) => {
  const mostrarSelector =
    tamanioPagina !== undefined &&
    alCambiarTamanioPagina !== undefined &&
    opcionesTamanioPagina !== undefined;

  const mostrarBarra = mostrarSelector || totalPaginas > 1;

  if (!mostrarBarra) return null;

  return (
    <div className="flex flex-col gap-3 pt-2">
      <div className="flex items-center justify-between gap-3 flex-wrap">
        {mostrarSelector ? (
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-muted-foreground whitespace-nowrap">
              Ver:
            </span>
            <ToggleGroup
              value={[String(tamanioPagina)]}
              onValueChange={(values) => {
                const next = values[0];
                if (next) {
                  alCambiarTamanioPagina!(Number(next));
                  alCambiarPagina(0);
                }
              }}
              variant="outline"
              size="sm"
            >
              {opcionesTamanioPagina!.map((opcion) => (
                <ToggleGroupItem
                  key={opcion}
                  value={String(opcion)}
                  className="min-w-9 text-xs font-bold rounded-lg"
                >
                  {opcion}
                </ToggleGroupItem>
              ))}
            </ToggleGroup>
            {totalElementos !== undefined && (
              <span className="text-[11px] text-muted-foreground font-medium">
                de {totalElementos} resultado{totalElementos !== 1 ? 's' : ''}
              </span>
            )}
          </div>
        ) : (
          <div />
        )}

        {totalPaginas > 1 ? (
          <div className="flex items-center gap-2">
            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8 rounded-lg"
              disabled={pagina === 0}
              onClick={() => alCambiarPagina(pagina - 1)}
            >
              <ChevronLeft className="h-4 w-4" />
            </Button>

            <span className="text-sm font-semibold text-muted-foreground px-1 whitespace-nowrap">
              Pág. {pagina + 1} / {totalPaginas}
            </span>

            <Button
              variant="outline"
              size="icon"
              className="h-8 w-8 rounded-lg"
              disabled={pagina >= totalPaginas - 1}
              onClick={() => alCambiarPagina(pagina + 1)}
            >
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        ) : (
          <div />
        )}
      </div>
    </div>
  );
};

