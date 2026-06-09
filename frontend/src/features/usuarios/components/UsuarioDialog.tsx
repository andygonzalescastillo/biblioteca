import { useEffect, useState } from 'react';
import { Controller, useForm, useWatch } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { UserCircle2 } from 'lucide-react';
import type { UsuarioResponse, UsuarioRequest } from '../types/usuario';
import { usuarioSchema, type UsuarioFormValues } from '../schemas/usuarioSchema';
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription, DialogFooter, } from '@/components/ui/dialog';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { CampoImagen } from '@/components/shared/CampoImagen';
import { CampoFormulario } from '@/components/shared/CampoFormulario';
import { CampoSwitchEstado } from '@/components/shared/CampoSwitchEstado';
import { obtenerUrlPreviewImagen } from '@/lib/previewImagen';

interface UsuarioDialogProps {
  isOpen: boolean;
  onOpenChange: (open: boolean) => void;
  editingUsuario: UsuarioResponse | null;
  onSave: (data: UsuarioRequest) => void;
  isSaving: boolean;
}

interface UsuarioDialogFormProps {
  editingUsuario: UsuarioResponse | null;
  onSave: UsuarioDialogProps['onSave'];
  onCancel: () => void;
  isSaving: boolean;
}

const UsuarioDialogForm = ({ editingUsuario, onSave, onCancel, isSaving }: UsuarioDialogFormProps) => {
  const [previewUrl, setPreviewUrl] = useState(() =>
    obtenerUrlPreviewImagen(editingUsuario?.foto),
  );
  const [isUploading, setIsUploading] = useState(false);

  const {
    handleSubmit,
    setValue,
    reset,
    control,
    formState: { errors },
  } = useForm<UsuarioFormValues>({
    resolver: zodResolver(usuarioSchema),
    mode: 'onChange',
    reValidateMode: 'onChange',
    defaultValues: { nombre: '', email: '', telefono: '', direccion: '', fotoId: null, estado: true },
  });

  const estado = useWatch({ control, name: 'estado', defaultValue: true }) ?? true;

  useEffect(() => {
    if (editingUsuario) {
      reset({
        nombre: editingUsuario.nombre,
        email: editingUsuario.email,
        telefono: editingUsuario.telefono ?? '',
        direccion: editingUsuario.direccion ?? '',
        fotoId: editingUsuario.foto?.id ?? null,
        estado: editingUsuario.estado,
      });
    } else {
      reset({ nombre: '', email: '', telefono: '', direccion: '', fotoId: null, estado: true });
    }
  }, [editingUsuario, reset]);

  const handleImageChange = (imageId: string | null, url: string) => {
    setValue('fotoId', imageId, { shouldValidate: true });
    setPreviewUrl(url);
  };

  const onSubmit = (data: UsuarioFormValues) => {
    onSave({
      nombre: data.nombre,
      email: data.email,
      telefono: data.telefono ?? '',
      direccion: data.direccion ?? '',
      fotoId: data.fotoId ?? undefined,
      estado: data.estado,
    });
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-4 py-2">
      <CampoImagen
        previewUrl={previewUrl}
        onChange={handleImageChange}
        label="Foto de perfil (JPG, PNG · máx 5 MB)"
        inputId="usuario-foto-file"
        placeholder={<UserCircle2 className="h-9 w-9 text-blue-400/50" />}
        successMessage="Foto de perfil cargada correctamente"
        onUploadingChange={setIsUploading}
      />

      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2">
        <CampoFormulario
          label="Nombre completo"
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
                placeholder="Ej. García López, Ana"
                className="rounded-xl h-10"
                aria-invalid={!!errors.nombre}
              />
            )}
          />
        </CampoFormulario>

        <CampoFormulario label="Email" htmlFor="email" required error={errors.email?.message}>
          <Controller
            control={control}
            name="email"
            render={({ field }) => (
              <Input
                id="email"
                name={field.name}
                value={field.value ?? ''}
                onValueChange={(value) => {
                  setValue('email', value, {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                type="email"
                placeholder="ana@correo.com"
                className="rounded-xl h-10"
                aria-invalid={!!errors.email}
              />
            )}
          />
        </CampoFormulario>
      </div>

      <div className="grid gap-4 grid-cols-1 sm:grid-cols-2">
        <CampoFormulario label="Teléfono" htmlFor="telefono" error={errors.telefono?.message}>
          <Controller
            control={control}
            name="telefono"
            render={({ field }) => (
              <Input
                id="telefono"
                name={field.name}
                value={field.value ?? ''}
                onValueChange={(value) => {
                  setValue('telefono', value, {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                placeholder="+51 999 888 777"
                className="rounded-xl h-10"
                aria-invalid={!!errors.telefono}
              />
            )}
          />
        </CampoFormulario>

        <CampoFormulario label="Dirección" htmlFor="direccion" error={errors.direccion?.message}>
          <Controller
            control={control}
            name="direccion"
            render={({ field }) => (
              <Input
                id="direccion"
                name={field.name}
                value={field.value ?? ''}
                onValueChange={(value) => {
                  setValue('direccion', value, {
                    shouldDirty: true,
                    shouldValidate: true,
                    shouldTouch: true,
                  });
                }}
                onBlur={field.onBlur}
                ref={field.ref}
                placeholder="Av. Principal 123"
                className="rounded-xl h-10"
                aria-invalid={!!errors.direccion}
              />
            )}
          />
        </CampoFormulario>
      </div>

      <CampoSwitchEstado
        id="estado-usuario"
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

export const UsuarioDialog = ({
  isOpen,
  onOpenChange,
  editingUsuario,
  onSave,
  isSaving,
}: UsuarioDialogProps) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOpenChange} dismissible={false}>
      <DialogContent className="sm:max-w-lg rounded-2xl border border-border max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold">
            {editingUsuario ? 'Editar Lector' : 'Nuevo Lector'}
          </DialogTitle>
          <DialogDescription className="text-xs">
            Completa los datos del usuario y opcionalmente sube una foto de perfil.
          </DialogDescription>
        </DialogHeader>
        {isOpen ? (
          <UsuarioDialogForm
            key={editingUsuario?.id ?? 'new'}
            editingUsuario={editingUsuario}
            onSave={onSave}
            onCancel={() => onOpenChange(false)}
            isSaving={isSaving}
          />
        ) : null}
      </DialogContent>
    </Dialog>
  );
};

