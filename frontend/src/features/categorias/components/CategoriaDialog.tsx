import { useEffect } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import type { CategoriaResponse, CategoriaRequest } from '../types/categoria';
import { categoriaSchema, type CategoriaFormValues } from '../schemas/categoriaSchema';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import { CampoSwitchEstado } from '@/components/shared/CampoSwitchEstado';

interface CategoriaDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  editingCategoria: CategoriaResponse | null;
  onSave: (data: CategoriaRequest) => void;
  isSaving: boolean;
}

export const CategoriaDialog = ({
  isOpen,
  onOpenChange,
  editingCategoria,
  onSave,
  isSaving,
}: CategoriaDialogProps) => {
  const {
    register,
    handleSubmit,
    reset,
    control,
    setValue,
    formState: { errors },
  } = useForm<CategoriaFormValues>({
    resolver: zodResolver(categoriaSchema),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: { nombre: '', descripcion: '', estado: true },
  });

  const estado = useWatch({ control, name: 'estado', defaultValue: true }) ?? true;

  useEffect(() => {
    if (isOpen) {
      reset({
        nombre: editingCategoria?.nombre || '',
        descripcion: editingCategoria?.descripcion || '',
        estado: editingCategoria ? editingCategoria.estado : true,
      });
    }
  }, [editingCategoria, isOpen, reset]);

  const onSubmit = (data: CategoriaFormValues) => {
    onSave({ nombre: data.nombre, descripcion: data.descripcion || '', estado: data.estado });
  };

  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange} dismissible={false}>
      <DialogContent className="sm:max-w-md rounded-2xl border border-border">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold">
            {editingCategoria ? 'Editar Categoría' : 'Nueva Categoría'}
          </DialogTitle>
          <DialogDescription className="text-xs">
            Introduce la información de la clasificación de libros.
          </DialogDescription>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
          <CampoFormulario
            label="Nombre"
            htmlFor="nombre"
            required
            error={errors.nombre?.message}
          >
            <Controller
              control={control}
              name="nombre"
              render={({ field }) => (
                <Input
                  id="nombre"
                  name={field.name}
                  value={field.value ?? ''}
                  onValueChange={(value) => {
                    setValue('nombre', value, {
                      shouldDirty: true,
                      shouldValidate: true,
                      shouldTouch: true,
                    });
                  }}
                  onBlur={field.onBlur}
                  ref={field.ref}
                  placeholder="Ej. Programación, Novela..."
                  className="rounded-xl h-10"
                  aria-invalid={!!errors.nombre}
                />
              )}
            />
          </CampoFormulario>

          <CampoFormulario
            label="Descripción"
            htmlFor="descripcion"
            error={errors.descripcion?.message}
          >
            <Textarea
              id="descripcion"
              placeholder="Breve descripción del género o tema..."
              rows={3}
              className="rounded-xl"
              aria-invalid={!!errors.descripcion}
              {...register('descripcion')}
            />
          </CampoFormulario>

          <CampoSwitchEstado
            id="estado-categoria"
            checked={estado}
            onCheckedChange={(v) => setValue('estado', v, { shouldValidate: true })}
          />

          <DialogFooter className="pt-4 border-t border-border/40">
            <Button
              type="button"
              variant="outline"
              className="rounded-xl font-bold h-9"
              onClick={() => onOpenChange(false)}
            >
              Cancelar
            </Button>
            <Button type="submit" disabled={isSaving} className="rounded-xl font-bold h-9">
              {isSaving ? 'Guardando...' : 'Guardar'}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

