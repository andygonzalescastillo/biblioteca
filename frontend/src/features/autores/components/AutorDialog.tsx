import { useEffect, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { es } from 'date-fns/locale';
import { CalendarIcon, UserCircle2 } from 'lucide-react';
import type { AutorResponse, AutorRequest } from '../types/autor';
import { autorSchema, type AutorFormValues } from '../schemas/autorSchema';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { Button } from '@/components/ui/button';
import { Calendar } from '@/components/ui/calendar';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { CampoImagen } from '@/components/shared/CampoImagen';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import { CampoSwitchEstado } from '@/components/shared/CampoSwitchEstado';
import { obtenerUrlPreviewImagen } from '@/lib/previewImagen';
import { formatearFecha, fechaAFormatoISO } from '@/lib/formatearFecha';

interface AutorDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  editingAutor: AutorResponse | null;
  onSave: (data: AutorRequest) => void;
  isSaving: boolean;
}

interface AutorDialogFormProps {
  editingAutor: AutorResponse | null;
  onSave: AutorDialogProps['onSave'];
  onCancel: () => void;
  isSaving: boolean;
}

const FECHA_MINIMA = new Date(1800, 0, 1);
const FECHA_MAXIMA = new Date();

const parseFechaLocal = (fecha?: string) => {
  if (!fecha) return undefined;
  const [year, month, day] = fecha.split('-').map(Number);
  if (!year || !month || !day) return undefined;
  return new Date(year, month - 1, day);
};



const AutorDialogForm = ({ editingAutor, onSave, onCancel, isSaving }: AutorDialogFormProps) => {
  const [previewUrl, setPreviewUrl] = useState(() => obtenerUrlPreviewImagen(editingAutor?.foto));
  const [isUploading, setIsUploading] = useState(false);
  const [calendarOpen, setCalendarOpen] = useState(false);

  const {
    handleSubmit,
    setValue,
    reset,
    control,
    formState: { errors },
  } = useForm<AutorFormValues>({
    resolver: zodResolver(autorSchema),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: { nombre: '', biografia: '', fechaNacimiento: '', fotoId: null, estado: true },
  });

  const estado = useWatch({ control, name: 'estado', defaultValue: true }) ?? true;
  const fechaNacimiento = useWatch({ control, name: 'fechaNacimiento', defaultValue: '' });
  const fechaSeleccionada = parseFechaLocal(fechaNacimiento);

  useEffect(() => {
    if (editingAutor) {
      reset({
        nombre: editingAutor.nombre,
        biografia: editingAutor.biografia || '',
        fechaNacimiento: editingAutor.fechaNacimiento || '',
        fotoId: editingAutor.foto?.id ?? null,
        estado: editingAutor.estado,
      });
    } else {
      reset({ nombre: '', biografia: '', fechaNacimiento: '', fotoId: null, estado: true });
    }
  }, [editingAutor, reset]);

  const handleImageChange = (imageId: string | null, url: string) => {
    setValue('fotoId', imageId, { shouldValidate: true });
    setPreviewUrl(url);
  };

  const onSubmit = (data: AutorFormValues) => {
    onSave({
      nombre: data.nombre,
      biografia: data.biografia || '',
      fechaNacimiento: data.fechaNacimiento || undefined,
      fotoId: data.fotoId ?? undefined,
      estado: data.estado,
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
      <CampoImagen
        previewUrl={previewUrl}
        onChange={handleImageChange}
        label="Foto del Autor (JPG, PNG · máx 5 MB)"
        inputId="autor-foto-file"
        placeholder={<UserCircle2 className="h-9 w-9 text-blue-400/50" />}
        successMessage="Foto de autor cargada correctamente"
        onUploadingChange={setIsUploading}
      />

      <CampoFormulario label="Nombre" htmlFor="nombre" required error={errors.nombre?.message}>
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
              placeholder="Ej. Gabriel García Márquez..."
              className="rounded-xl h-10"
              aria-invalid={!!errors.nombre}
            />
          )}
        />
      </CampoFormulario>

      <CampoFormulario
        label="Fecha de Nacimiento"
        htmlFor="fechaNacimiento"
        error={errors.fechaNacimiento?.message}
      >
        <Popover open={calendarOpen} onOpenChange={setCalendarOpen}>
          <PopoverTrigger
            id="fechaNacimiento"
            type="button"
            className="flex h-10 w-full items-center justify-start gap-2 rounded-xl border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none transition-[color,box-shadow] hover:bg-accent hover:text-accent-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 aria-invalid:border-destructive aria-invalid:ring-destructive/20 dark:aria-invalid:ring-destructive/40"
            aria-invalid={!!errors.fechaNacimiento}
          >
            <CalendarIcon className="h-4 w-4 text-muted-foreground" />
            <span className={fechaNacimiento ? 'text-foreground' : 'text-muted-foreground'}>
              {fechaNacimiento ? formatearFecha(fechaNacimiento, { day: '2-digit', month: 'long', year: 'numeric' }) : 'Selecciona una fecha'}
            </span>
          </PopoverTrigger>
          <PopoverContent align="start" className="w-auto p-0">
            <Calendar
              mode="single"
              selected={fechaSeleccionada}
              defaultMonth={fechaSeleccionada ?? new Date(1990, 0, 1)}
              startMonth={FECHA_MINIMA}
              endMonth={FECHA_MAXIMA}
              captionLayout="dropdown"
              locale={es}
              className="p-1 [--cell-size:--spacing(6)]"
              disabled={{ after: FECHA_MAXIMA, before: FECHA_MINIMA }}
              onSelect={(date) => {
                setValue('fechaNacimiento', date ? fechaAFormatoISO(date) : '', {
                  shouldDirty: true,
                  shouldValidate: true,
                });
                setCalendarOpen(false);
              }}
            />
          </PopoverContent>
        </Popover>
      </CampoFormulario>

      <CampoFormulario label="Biografía" htmlFor="biografia" error={errors.biografia?.message}>
        <Controller
          control={control}
          name="biografia"
          render={({ field }) => (
            <Textarea
              id="biografia"
              name={field.name}
              value={field.value ?? ''}
              onChange={(event) => {
                setValue('biografia', event.target.value, {
                  shouldDirty: true,
                  shouldValidate: true,
                  shouldTouch: true,
                });
              }}
              onBlur={field.onBlur}
              ref={field.ref}
              placeholder="Breve biografía, trayectoria y datos de interés del autor..."
              rows={4}
              className="rounded-xl"
              aria-invalid={!!errors.biografia}
            />
          )}
        />
      </CampoFormulario>

      <CampoSwitchEstado
        id="estado-autor"
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

export const AutorDialog = ({
  isOpen,
  onOpenChange,
  editingAutor,
  onSave,
  isSaving,
}: AutorDialogProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange} dismissible={false}>
      <DialogContent className="sm:max-w-md rounded-2xl border border-border max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold">
            {editingAutor ? 'Editar Autor' : 'Nuevo Autor'}
          </DialogTitle>
          <DialogDescription className="text-xs">
            Introduce la información del autor literario.
          </DialogDescription>
        </DialogHeader>
        {isOpen ? (
          <AutorDialogForm
            key={editingAutor?.id ?? 'new'}
            editingAutor={editingAutor}
            onSave={onSave}
            onCancel={() => onOpenChange(false)}
            isSaving={isSaving}
          />
        ) : null}
      </DialogContent>
    </Dialog>
  );
};

