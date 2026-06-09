import { AlertTriangle } from 'lucide-react';
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogMedia, AlertDialogTitle, } from '@/components/ui/alert-dialog';

interface DialogoConfirmarEliminarProps {
  abierto: boolean;
  alCerrar: () => void;
  alConfirmar: () => void;
  pendiente: boolean;
  titulo: string;
  descripcion: string;
}

export const DialogoConfirmarEliminar = ({
  abierto,
  alCerrar,
  alConfirmar,
  pendiente,
  titulo,
  descripcion,
}: DialogoConfirmarEliminarProps) => {
  return (
    <AlertDialog open={abierto} onOpenChange={(open) => !open && alCerrar()}>
      <AlertDialogContent className="sm:max-w-md rounded-2xl">
        <AlertDialogHeader>
          <AlertDialogMedia className="bg-destructive/10 text-destructive">
            <AlertTriangle className="h-6 w-6" />
          </AlertDialogMedia>
          <AlertDialogTitle className="text-lg font-bold">{titulo}</AlertDialogTitle>
          <AlertDialogDescription className="text-xs max-w-sm">
            {descripcion}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel className="rounded-xl font-bold" onClick={alCerrar}>
            Cancelar
          </AlertDialogCancel>
          <AlertDialogAction
            variant="destructive"
            className="rounded-xl font-bold"
            onClick={alConfirmar}
            disabled={pendiente}
          >
            {pendiente ? 'Eliminando...' : 'Confirmar'}
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

