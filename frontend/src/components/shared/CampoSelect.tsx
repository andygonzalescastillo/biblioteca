import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue, } from '@/components/ui/select';
import { Field, FieldError, FieldLabel } from '@/components/ui/field';
import { cn } from '@/lib/utils';

export interface OpcionSelect {
  value: string;
  label: string;
}

interface CampoSelectProps {
  label?: string;
  value: string;
  onValueChange: (value: string) => void;
  opciones: OpcionSelect[];
  placeholder?: string;
  className?: string;
  triggerClassName?: string;
  required?: boolean;
  disabled?: boolean;
  error?: string;
  id?: string;
  size?: 'default' | 'lg';
}

export const CampoSelect = ({
  label,
  value,
  onValueChange,
  opciones,
  placeholder = 'Seleccionar...',
  className,
  triggerClassName,
  required,
  disabled,
  error,
  id,
  size = 'default',
}: CampoSelectProps) => {
  const isLg = size === 'lg';

  const select = (
    <div className="w-full">
      <Select
        value={value}
        items={opciones}
        onValueChange={(v) => v != null && onValueChange(v)}
        disabled={disabled}
      >
        <SelectTrigger
          id={id}
          className={cn(
            'w-full rounded-xl text-xs font-semibold',
            isLg ? 'data-[size=default]:h-10' : '',
            triggerClassName
          )}
          aria-invalid={!!error}
        >
          <SelectValue placeholder={placeholder} />
        </SelectTrigger>
        <SelectContent>
          {opciones.map((opcion) => (
            <SelectItem key={opcion.value} value={opcion.value}>
              {opcion.label}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );

  if (!label) {
    return <div className={cn('w-full', className)}>{select}</div>;
  }

  return (
    <Field
      className={cn(isLg ? 'gap-1.5' : 'gap-1', 'w-full', className)}
      data-invalid={!!error}
    >
      <FieldLabel
        htmlFor={id}
        className={cn(
          'font-bold text-muted-foreground uppercase',
          isLg ? 'text-xs' : 'text-[10px] pl-1'
        )}
      >
        {label}
        {required && <span className="text-destructive ml-0.5">*</span>}
      </FieldLabel>
      {select}
      <FieldError className="text-xs font-bold">{error}</FieldError>
    </Field>
  );
};

