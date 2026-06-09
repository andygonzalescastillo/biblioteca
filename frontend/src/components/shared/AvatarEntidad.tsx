import type { LucideIcon } from 'lucide-react';
import { Avatar, AvatarFallback, AvatarImage } from '@/components/ui/avatar';
import { cn } from '@/lib/utils';

interface AvatarEntidadProps {
  src?: string;
  alt: string;
  icon: LucideIcon;
  className?: string;
  fallbackClassName?: string;
}

export const AvatarEntidad = ({
  src,
  alt,
  icon: Icon,
  className,
  fallbackClassName,
}: AvatarEntidadProps) => {
  return (
    <Avatar
      className={cn(
        'h-10 w-10 shrink-0 rounded-full',
        className,
      )}
    >
      {src ? <AvatarImage src={src} alt={alt} className="object-cover" /> : null}
      <AvatarFallback
        className={cn(
          'rounded-full bg-primary/10 text-primary',
          fallbackClassName,
        )}
      >
        <Icon className="h-5 w-5" />
      </AvatarFallback>
    </Avatar>
  );
};

