import { User, CheckCircle2 } from 'lucide-react';
import { Button } from '@/components/ui/button';
import { Badge } from '@/components/ui/badge';
import { Combobox, ComboboxContent, ComboboxInput, ComboboxItem, ComboboxList, } from '@/components/ui/combobox';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import { AvatarEntidad } from '@/components/shared/AvatarEntidad';
import type { UsuarioResponse } from '@/features/usuarios/types/usuario';

interface LectorSelectorProps {
  selectedUsuario: UsuarioResponse | undefined;
  usuariosFiltrados: UsuarioResponse[];
  buscarUsuario: string;
  setBuscarUsuario: (val: string) => void;
  onSelectUsuario: (id: number) => void;
  onClearLector: () => void;
  error?: string;
}

export const LectorSelector = ({
  selectedUsuario,
  usuariosFiltrados,
  buscarUsuario,
  setBuscarUsuario,
  onSelectUsuario,
  onClearLector,
  error,
}: LectorSelectorProps) => {
  return (
    <div className="rounded-2xl border border-border/80 bg-card/30 p-5 shadow-sm">
      <div className="mb-4 flex items-start justify-between gap-3">
        <div>
          <p className="text-[11px] font-bold uppercase tracking-wider text-muted-foreground">Paso 1</p>
          <h3 className="mt-1 text-lg font-bold">Selecciona el lector</h3>
        </div>
        {selectedUsuario ? (
          <Badge variant="outline" className="gap-1.5 text-[10px] font-bold text-emerald-600">
            <CheckCircle2 className="h-3 w-3" />
            Listo
          </Badge>
        ) : null}
      </div>
      <CampoFormulario label="Lector" required error={error}>
        {selectedUsuario ? (
          <div className="flex items-center gap-3 rounded-2xl border border-primary/20 bg-primary/10 p-4">
            <AvatarEntidad
              alt={selectedUsuario.nombre}
              icon={User}
              className="h-10 w-10 border-primary/20"
              fallbackClassName="bg-primary/10 text-primary"
            />
            <div className="min-w-0 flex-1">
              <p className="truncate text-sm font-bold">{selectedUsuario.nombre}</p>
              <p className="truncate text-xs text-muted-foreground">{selectedUsuario.email}</p>
            </div>
            <Button type="button" variant="ghost" size="sm" onClick={onClearLector}>
              Cambiar
            </Button>
          </div>
        ) : (
          <Combobox
            value={null}
            inputValue={buscarUsuario}
            onInputValueChange={setBuscarUsuario}
            onValueChange={(value) => {
              if (value != null) {
                onSelectUsuario(value as number);
              }
            }}
            itemToStringLabel={(id) => {
              const usuario = usuariosFiltrados.find((item) => item.id === id);
              return usuario ? `${usuario.nombre} - ${usuario.email}` : '';
            }}
          >
            <ComboboxInput placeholder="Buscar lector por nombre o email..." showClear />
            <ComboboxContent>
              <ComboboxList>
                {usuariosFiltrados.map((usuario) => (
                  <ComboboxItem key={usuario.id} value={usuario.id}>
                    <div className="flex flex-col min-w-0">
                      <span className="truncate text-xs font-semibold">{usuario.nombre}</span>
                      <span className="truncate text-[10px] text-muted-foreground">{usuario.email}</span>
                    </div>
                  </ComboboxItem>
                ))}
              </ComboboxList>
            </ComboboxContent>
          </Combobox>
        )}
      </CampoFormulario>
    </div>
  );
};

