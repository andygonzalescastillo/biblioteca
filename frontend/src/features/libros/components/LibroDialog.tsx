import { useEffect, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { BookOpen } from 'lucide-react';
import type { CategoriaResponse } from '@/features/categorias/types/categoria';
import type { AutorResponse } from '@/features/autores/types/autor';
import type { LibroResponse, LibroRequest } from '../types/libro';
import { libroSchema, type LibroFormValues } from '../schemas/libroSchema';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { Label } from '@/components/ui/label';
import { ScrollArea } from '@/components/ui/scroll-area';
import { toast } from 'sonner';
import { imagenService } from '@/shared/services/imagenService';
import { CampoImagen } from '@/components/shared/CampoImagen';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import { CampoSelect } from '@/components/shared/CampoSelect';
import { CampoSwitchEstado } from '@/components/shared/CampoSwitchEstado';
import { Field, FieldError, FieldLabel } from '@/components/ui/field';
import { obtenerUrlPreviewImagen } from '@/lib/previewImagen';

interface LibroDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  editingLibro: LibroResponse | null;
  categorias: CategoriaResponse[];
  autores: AutorResponse[];
  onSave: (data: LibroRequest) => void;
  isSaving: boolean;
}

interface LibroDialogFormProps {
  editingLibro: LibroResponse | null;
  categorias: CategoriaResponse[];
  autores: AutorResponse[];
  onSave: LibroDialogProps['onSave'];
  onCancel: () => void;
  isSaving: boolean;
}

const LibroDialogForm = ({
  editingLibro,
  categorias,
  autores,
  onSave,
  onCancel,
  isSaving,
}: LibroDialogFormProps) => {
  const [previewUrl, setPreviewUrl] = useState(() =>
    obtenerUrlPreviewImagen(editingLibro?.portada),
  );
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const {
    handleSubmit,
    setValue,
    reset,
    control,
    formState: { errors },
  } = useForm<LibroFormValues>({
    resolver: zodResolver(libroSchema),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: {
      titulo: '',
      isbn: '',
      stock: 0,
      categoriaId: 0,
      autoresIds: [],
      portadaId: null,
      estado: true,
    },
  });

  const categoriaId = useWatch({ control, name: 'categoriaId', defaultValue: 0 });
  const autoresIds = useWatch({ control, name: 'autoresIds', defaultValue: [] });
  const estado = useWatch({ control, name: 'estado', defaultValue: true }) ?? true;

  useEffect(() => {
    if (editingLibro) {
      reset({
        titulo: editingLibro.titulo,
        isbn: editingLibro.isbn,
        stock: editingLibro.stock,
        categoriaId: editingLibro.categoria.id,
        autoresIds: editingLibro.autores.map((a) => a.id),
        portadaId: editingLibro.portada?.id ?? null,
        estado: editingLibro.estado,
      });
    } else {
      reset({
        titulo: '',
        isbn: '',
        stock: 0,
        categoriaId: 0,
        autoresIds: [],
        portadaId: null,
        estado: true,
      });
    }
  }, [editingLibro, reset]);

  const handleCoverChange = (file: File | null, url: string) => {
    setImageFile(file);
    setPreviewUrl(url);
    if (!file) {
      setValue('portadaId', null, { shouldValidate: true });
    }
  };

  const onSubmit = async (data: LibroFormValues) => {
    let finalPortadaId = data.portadaId;

    if (imageFile) {
      try {
        setIsUploading(true);
        const res = await imagenService.subir(imageFile);
        finalPortadaId = res.id;
      } catch (err: unknown) {
        const message = err && typeof err === 'object' && 'message' in err
          ? String((err as { message: string }).message)
          : 'Error al subir la imagen';
        toast.error(message);
        setIsUploading(false);
        return;
      } finally {
        setIsUploading(false);
      }
    }

    onSave({
      titulo: data.titulo,
      isbn: data.isbn,
      stock: data.stock,
      categoriaId: data.categoriaId,
      autoresIds: data.autoresIds,
      portadaId: finalPortadaId ?? undefined,
      estado: data.estado,
    });
  };

  const toggleAutor = (id: number, checked: boolean) => {
    const siguiente = checked
      ? [...autoresIds, id]
      : autoresIds.filter((a) => a !== id);
    setValue('autoresIds', siguiente, { shouldValidate: true });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2">
        <CampoFormulario label="Título" htmlFor="titulo" required error={errors.titulo?.message}>
          <Controller
            control={control}
            name="titulo"
            render={({ field }) => (
              <Input
                id="titulo"
                name={field.name}
                value={field.value ?? ''}
                onValueChange={(value) => {
                  setValue('titulo', value, {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                placeholder="Ej. Cien años de soledad"
                className="rounded-xl h-10"
                aria-invalid={!!errors.titulo}
              />
            )}
          />
        </CampoFormulario>

        <CampoFormulario label="ISBN" htmlFor="isbn" required error={errors.isbn?.message}>
          <Controller
            control={control}
            name="isbn"
            render={({ field }) => (
              <Input
                id="isbn"
                name={field.name}
                value={field.value ?? ''}
                onValueChange={(value) => {
                  setValue('isbn', value, {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                placeholder="Ej. 9780307474728"
                className="rounded-xl h-10 font-mono"
                aria-invalid={!!errors.isbn}
              />
            )}
          />
        </CampoFormulario>
      </div>

      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2">
        <CampoFormulario label="Stock" htmlFor="stock" required error={errors.stock?.message}>
          <Controller
            control={control}
            name="stock"
            render={({ field }) => (
              <Input
                id="stock"
                name={field.name}
                value={String(field.value ?? 0)}
                onValueChange={(value) => {
                  setValue('stock', value === '' ? 0 : Number(value), {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                type="number"
                min="0"
                className="rounded-xl h-10"
                aria-invalid={!!errors.stock}
              />
            )}
          />
        </CampoFormulario>

        <CampoSelect
          label="Categoría"
          id="categoriaId"
          required
          size="lg"
          value={categoriaId > 0 ? String(categoriaId) : '0'}
          onValueChange={(v) => setValue('categoriaId', Number(v), { shouldValidate: true })}
          opciones={[
            { value: '0', label: 'Selecciona Categoría' },
            ...categorias.map((cat) => ({
              value: String(cat.id),
              label: cat.nombre,
            })),
          ]}
          placeholder="Selecciona Categoría"
          error={errors.categoriaId?.message}
        />
      </div>

      <Field data-invalid={!!errors.autoresIds}>
        <FieldLabel className="text-xs font-bold text-muted-foreground uppercase">
          Seleccionar Autores <span className="text-destructive ml-0.5">*</span>
        </FieldLabel>
        <ScrollArea className="h-32 rounded-xl border border-border/80 bg-muted/10">
          <div className="p-3 space-y-2">
            {autores.length === 0 ? (
              <p className="text-xs text-muted-foreground text-center py-4">
                No hay autores disponibles
              </p>
            ) : (
              autores.map((aut) => (
                <div key={aut.id} className="flex items-center gap-2.5">
                  <Checkbox
                    id={`autor-${aut.id}`}
                    checked={autoresIds.includes(aut.id)}
                    onCheckedChange={(checked) => toggleAutor(aut.id, checked === true)}
                  />
                  <Label htmlFor={`autor-${aut.id}`} className="text-xs font-semibold cursor-pointer">
                    {aut.nombre}
                  </Label>
                </div>
              ))
            )}
          </div>
        </ScrollArea>
        <FieldError className="text-xs font-bold">{errors.autoresIds?.message}</FieldError>
      </Field>

      <CampoImagen
        variant="cover"
        previewUrl={previewUrl}
        onChange={handleCoverChange}
        label="Portada del Libro (JPG, PNG · máx 5 MB)"
        inputId="libro-portada-file"
        placeholder={<BookOpen className="h-6 w-6 text-muted-foreground/40" />}
        selectButtonLabel="Seleccionar Portada"
      />

      <CampoSwitchEstado
        id="estado-libro"
        checked={estado}
        onCheckedChange={(v) => setValue('estado', v, { shouldValidate: true })}
      />

      <DialogFooter className="pt-4 border-t border-border/40">
        <Button
          type="button"
          variant="outline"
          className="rounded-xl font-bold h-9"
          onClick={onCancel}
        >
          Cancelar
        </Button>
        <Button type="submit" disabled={isSaving || isUploading} className="rounded-xl font-bold h-9">
          {isSaving ? 'Guardando...' : 'Guardar'}
        </Button>
      </DialogFooter>
    </form>
  );
};

export const LibroDialog = ({
  isOpen,
  onOpenChange,
  editingLibro,
  categorias,
  autores,
  onSave,
  isSaving,
}: LibroDialogProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange} dismissible={false}>
      <DialogContent className="sm:max-w-lg rounded-2xl border border-border max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold">
            {editingLibro ? 'Editar Libro' : 'Nuevo Libro'}
          </DialogTitle>
          <DialogDescription className="text-xs">
            Introduce los detalles y asocia autores y portada al catálogo.
          </DialogDescription>
        </DialogHeader>
        {isOpen ? (
          <LibroDialogForm
            key={editingLibro?.id ?? 'new'}
            editingLibro={editingLibro}
            categorias={categorias}
            autores={autores}
            onSave={onSave}
            onCancel={() => onOpenChange(false)}
            isSaving={isSaving}
          />
        ) : null}
      </DialogContent>
    </Dialog>
  );
};

