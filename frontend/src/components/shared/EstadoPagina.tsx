import { ServerCrash } from 'lucide-react';
import { Button } from '../ui/button';
import { Skeleton } from '../ui/skeleton';
import { cn } from '@/lib/utils';


export type CardGridSkeletonVariant = 'simple' | 'avatar' | 'cover' | 'usuario' | 'prestamo';

const SKELETON_GRID: Record<CardGridSkeletonVariant, string> = {
  simple: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-4',
  avatar: 'grid gap-6 sm:grid-cols-2 lg:grid-cols-4',
  cover: 'grid gap-5 grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4',
  usuario: 'grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4',
  prestamo: 'grid gap-5 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3',
};

const SKELETON_COUNT: Record<CardGridSkeletonVariant, number> = {
  simple: 8,
  avatar: 8,
  cover: 8,
  usuario: 8,
  prestamo: 6,
};

interface CardGridSkeletonProps {
  variant: CardGridSkeletonVariant;
  count?: number;
}

export const CardGridSkeleton = ({ variant, count }: CardGridSkeletonProps) => {
  const total = count ?? SKELETON_COUNT[variant];

  return (
    <div className={SKELETON_GRID[variant]}>
      {Array.from({ length: total }).map((_, i) => (
        <SkeletonCard key={i} variant={variant} />
      ))}
    </div>
  );
}

const SkeletonCard = ({ variant }: { variant: CardGridSkeletonVariant }) => {
  if (variant === 'cover') {
    return (
      <div className="border border-border/80 bg-card/40 rounded-xl flex flex-col justify-between overflow-hidden animate-pulse">
        <Skeleton className="aspect-3/4 w-full rounded-none" />
        <div className="p-4 pb-2 space-y-3">
          <div className="flex items-center gap-1.5">
            <Skeleton className="h-4 w-4 rounded-full" />
            <Skeleton className="h-4 w-20 rounded" />
          </div>
          <Skeleton className="h-5 w-3/4 rounded mt-2" />
          <Skeleton className="h-3.5 w-1/2 rounded" />
        </div>
        <div className="px-4 pb-4 mt-auto">
          <Skeleton className="h-3 w-2/3 rounded" />
        </div>
        <SkeletonActions />
      </div>
    );
  }

  if (variant === 'usuario') {
    return (
      <div className="border border-border/80 bg-card/40 rounded-xl overflow-hidden p-5 flex flex-col justify-between space-y-4 animate-pulse">
        <div className="flex items-center gap-4">
          <Skeleton className="h-14 w-14 shrink-0 rounded-full" />
          <div className="min-w-0 flex-1 space-y-2">
            <Skeleton className="h-5 w-3/4 rounded" />
            <Skeleton className="h-4 w-16 rounded" />
          </div>
        </div>
        <div className="space-y-2.5">
          {Array.from({ length: 4 }).map((_, j) => (
            <div key={j} className="flex items-center gap-2">
              <Skeleton className="h-4 w-4 rounded-full shrink-0" />
              <Skeleton className="h-3.5 w-3/4 rounded" />
            </div>
          ))}
        </div>
        <SkeletonActions />
      </div>
    );
  }

  if (variant === 'prestamo') {
    return (
      <div className="border border-border/80 bg-card/40 rounded-xl p-5 flex flex-col justify-between space-y-4 animate-pulse">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-3">
            <Skeleton className="h-10 w-10 shrink-0 rounded-full" />
            <div className="space-y-1.5">
              <Skeleton className="h-4 w-28 rounded" />
              <Skeleton className="h-3 w-36 rounded" />
            </div>
          </div>
          <Skeleton className="h-5 w-16 rounded shrink-0" />
        </div>
        <div className="space-y-3">
          <div className="space-y-1.5">
            <Skeleton className="h-3 w-20 rounded" />
            <Skeleton className="h-7 w-full rounded-lg" />
            <Skeleton className="h-7 w-full rounded-lg" />
          </div>
          <div className="grid grid-cols-2 gap-2">
            <Skeleton className="h-12 w-full rounded-lg" />
            <Skeleton className="h-12 w-full rounded-lg" />
          </div>
          <Skeleton className="h-8 w-full rounded-xl mt-1" />
        </div>
      </div>
    );
  }

  const withAvatar = variant === 'avatar';

  return (
    <div className="border border-border/80 bg-card/40 rounded-xl p-5 flex flex-col justify-between space-y-4 animate-pulse">
      <div className="space-y-3">
        {withAvatar ? (
          <div className="flex items-start gap-3">
            <Skeleton className="h-10 w-10 shrink-0 rounded-xl" />
            <div className="min-w-0 flex-1 space-y-2">
              <div className="flex items-center justify-between gap-2">
                <Skeleton className="h-5 w-2/3 rounded" />
                <Skeleton className="h-5 w-12 shrink-0 rounded" />
              </div>
              <Skeleton className="h-3 w-1/2 rounded" />
            </div>
          </div>
        ) : (
          <div className="flex items-center justify-between gap-2">
            <Skeleton className="h-5 w-1/2 rounded" />
            <Skeleton className="h-5 w-16 rounded" />
          </div>
        )}
        <div className={cn('space-y-2', withAvatar && 'mt-3')}>
          <Skeleton className="h-3 w-full rounded" />
          <Skeleton className="h-3 w-5/6 rounded" />
          <Skeleton className="h-3 w-2/3 rounded" />
        </div>
      </div>
      <SkeletonActions />
    </div>
  );
};

const SkeletonActions = () => {
  return (
    <div className="flex justify-end gap-2 pt-2 border-t border-border/40">
      <Skeleton className="h-8 w-8 rounded-lg" />
      <Skeleton className="h-8 w-8 rounded-lg" />
    </div>
  );
};

export const CategoriasSkeleton = () => <CardGridSkeleton variant="simple" />;
export const AutoresSkeleton = () => <CardGridSkeleton variant="avatar" />;
export const LibrosSkeleton = () => <CardGridSkeleton variant="cover" />;
export const UsuariosSkeleton = () => <CardGridSkeleton variant="usuario" />;
export const PrestamosSkeleton = () => <CardGridSkeleton variant="prestamo" />;


export const EstadoError = ({
  mensaje = 'Verifica que el backend esté activo.',
}: {
  mensaje?: string;
}) => {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-24 text-muted-foreground animate-fadeIn">
      <div className="h-16 w-16 rounded-2xl bg-rose-500/10 text-rose-500 flex items-center justify-center">
        <ServerCrash className="h-8 w-8" />
      </div>
      <p className="font-bold text-foreground">Error al cargar los datos</p>
      <p className="text-sm text-center max-w-sm">{mensaje}</p>
    </div>
  );
}

interface EstadoVacioProps {
  icono: React.ReactNode;
  titulo: string;
  descripcion: string;
  etiquetaAccion?: string;
  alAccionar?: () => void;
}

export const EstadoVacio = ({ icono, titulo, descripcion, etiquetaAccion, alAccionar }: EstadoVacioProps) => {
  return (
    <div className="flex flex-col items-center justify-center gap-3 py-24 text-muted-foreground">
      <div className="h-16 w-16 rounded-2xl bg-primary/10 text-primary flex items-center justify-center">
        {icono}
      </div>
      <p className="font-bold text-foreground">{titulo}</p>
      <p className="text-sm text-center max-w-sm">{descripcion}</p>
      {etiquetaAccion && alAccionar && (
        <Button variant="outline" className="rounded-xl font-bold mt-2" onClick={alAccionar}>
          {etiquetaAccion}
        </Button>
      )}
    </div>
  );
}
