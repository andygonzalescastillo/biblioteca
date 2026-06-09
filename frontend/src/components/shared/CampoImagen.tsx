import { useState, type ReactNode } from 'react';
import { Upload, X, Loader2 } from 'lucide-react';
import { toast } from 'sonner';
import { Button } from '@/components/ui/button';
import { imagenService } from '@/shared/services/imagenService';
import { cn } from '@/lib/utils';

type ImageUploadVariant = 'avatar' | 'cover';

interface CampoImagenProps {
  previewUrl: string;
  onChange: (imageId: string | null, previewUrl: string) => void;
  label: string;
  variant?: ImageUploadVariant;
  inputId?: string;
  placeholder?: ReactNode;
  successMessage?: string;
  selectButtonLabel?: string;
  onUploadingChange?: (uploading: boolean) => void;
}

const previewStyles: Record<ImageUploadVariant, string> = {
  avatar: 'h-20 w-20 rounded-full',
  cover: 'h-24 w-16 rounded-lg',
};

export const CampoImagen = ({
  previewUrl,
  onChange,
  label,
  variant = 'avatar',
  inputId = 'image-upload',
  placeholder,
  successMessage = 'Imagen cargada correctamente',
  selectButtonLabel = 'Seleccionar Foto',
  onUploadingChange,
}: CampoImagenProps) => {
  const [isUploading, setIsUploading] = useState(false);

  const setUploading = (value: boolean) => {
    setIsUploading(value);
    onUploadingChange?.(value);
  };

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      toast.error('Por favor, selecciona un archivo de tipo imagen.');
      return;
    }
    try {
      setUploading(true);
      const res = await imagenService.subir(file);
      const url = imagenService.obtenerUrl(res.urlAlmacenamiento);
      onChange(res.id, url);
      toast.success(successMessage);
    } catch (err: unknown) {
      const message = err && typeof err === 'object' && 'message' in err
        ? String((err as { message: string }).message)
        : 'Error al subir la imagen';
      toast.error(message);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleRemove = () => onChange(null, '');

  return (
    <div className="flex items-center gap-5 p-3 border border-dashed border-border/80 rounded-xl bg-muted/10">
      <div
        className={cn(
          'shrink-0 overflow-hidden border-2 border-border/60 bg-linear-to-br from-blue-950 via-slate-900 to-indigo-950 flex items-center justify-center relative group',
          previewStyles[variant],
        )}
      >
        {previewUrl ? (
          <>
            <img src={previewUrl} alt="Vista previa" className="h-full w-full object-cover" />
            <button
              type="button"
              onClick={handleRemove}
              className="absolute inset-0 bg-black/50 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-opacity duration-200"
            >
              <X className="h-5 w-5 text-rose-400" />
            </button>
          </>
        ) : (
          placeholder
        )}
      </div>

      <div className="flex-1 space-y-1.5">
        <p className="text-[10px] text-muted-foreground font-semibold uppercase tracking-wider">
          {label}
        </p>
        <div className="relative inline-block">
          <input
            type="file"
            id={inputId}
            accept="image/*"
            onChange={handleFileChange}
            className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
            disabled={isUploading}
          />
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="rounded-xl h-8 text-xs font-semibold"
            disabled={isUploading}
          >
            {isUploading ? (
              <>
                <Loader2 className="h-3 w-3 mr-1 animate-spin" /> Subiendo...
              </>
            ) : (
              <>
                <Upload className="h-3 w-3 mr-1" /> {selectButtonLabel}
              </>
            )}
          </Button>
        </div>
      </div>
    </div>
  );
};

