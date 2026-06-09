import type { ReactNode } from 'react';
import { Field, FieldError, FieldLabel } from '@/components/ui/field';
import { cn } from '@/lib/utils';

interface CampoFormularioProps {
  label: string;
  htmlFor?: string;
  required?: boolean;
  error?: string;
  children: ReactNode;
  className?: string;
}

export const CampoFormulario = ({
  label,
  htmlFor,
  required,
  error,
  children,
  className,
}: CampoFormularioProps) => {
  return (
    <Field className={cn('gap-1.5', className)} data-invalid={!!error}>
      <FieldLabel
        htmlFor={htmlFor}
        className="text-xs font-bold text-muted-foreground uppercase"
      >
        {label}
        {required && <span className="text-destructive ml-0.5">*</span>}
      </FieldLabel>
      {children}
      <FieldError className="text-xs font-bold">{error}</FieldError>
    </Field>
  );
};

