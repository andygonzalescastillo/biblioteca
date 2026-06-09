import { useState } from 'react';
import type { FiltroEstado } from '@/lib/mapearFiltroEstado';

export interface ParametrosListadoCrud {
  buscar: string;
  estadoFilter: FiltroEstado;
  page: number;
  size: number;
}

export const useEstadoListadoCrud = <TResponse extends { id: number }>() => {
  const [elementoEditando, setElementoEditando] = useState<TResponse | null>(null);
  const [dialogoAbierto, setDialogoAbierto] = useState(false);
  const [idEliminando, setIdEliminando] = useState<number | null>(null);

  const abrirCrear = () => {
    setElementoEditando(null);
    setDialogoAbierto(true);
  };

  const abrirEditar = (item: TResponse) => {
    setElementoEditando(item);
    setDialogoAbierto(true);
  };

  const cerrarDialogo = (abierto: boolean) => {
    setDialogoAbierto(abierto);
    if (!abierto) setElementoEditando(null);
  };

  return {
    elementoEditando,
    setElementoEditando,
    dialogoAbierto,
    setDialogoAbierto,
    idEliminando,
    setIdEliminando,
    abrirCrear,
    abrirEditar,
    cerrarDialogo,
    editingItem: elementoEditando,
    setEditingItem: setElementoEditando,
    isDialogOpen: dialogoAbierto,
    setIsDialogOpen: setDialogoAbierto,
    deletingId: idEliminando,
    setDeletingId: setIdEliminando,
    handleOpenCreate: abrirCrear,
    handleOpenEdit: abrirEditar,
    handleCloseDialog: cerrarDialogo,
  };
}
