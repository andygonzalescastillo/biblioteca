import { Switch } from '@/components/ui/switch';
import { Field, FieldError, FieldLabel } from '@/components/ui/field';
import { cn } from '@/lib/utils';

interface CampoSwitchEstadoProps {
  label?: string;
  checked: boolean;
  onCheckedChange: (checked: boolean) => void;
  className?: string;
  error?: string;
  id?: string;
  descripcionActivo?: string;
  descripcionInactivo?: string;
}

export const CampoSwitchEstado = ({
  label = 'Estado',
  checked,
  onCheckedChange,
  className,
  error,
  id,
  descripcionActivo = 'Activo',
  descripcionInactivo = 'Inactivo',
}: CampoSwitchEstadoProps) => {
  return (
    <Field className={cn('gap-2', className)} data-invalid={!!error}>
      <div className="flex items-center justify-between gap-3">
        <FieldLabel
          htmlFor={id}
          className="text-xs font-bold text-muted-foreground uppercase"
        >
          {label}
        </FieldLabel>
        <div className="flex items-center gap-2">
          <span className="text-xs font-semibold text-muted-foreground">
            {checked ? descripcionActivo : descripcionInactivo}
          </span>
          <Switch id={id} checked={checked} onCheckedChange={onCheckedChange} />
        </div>
      </div>
      <FieldError className="text-xs font-bold">{error}</FieldError>
    </Field>
  );
};

