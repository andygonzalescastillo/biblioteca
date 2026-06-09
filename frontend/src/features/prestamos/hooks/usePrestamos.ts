import { useState, useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { prestamoService } from '../services/prestamoService';
import { useMutacionesPrestamo } from './useMutacionesPrestamo';
import type { EstadoPrestamo } from '../types/prestamo';

export interface ParametrosPrestamos {
  buscarUsuario: string;
  estadoFilter: EstadoPrestamo | 'TODOS';
  page: number;
  pageSize: number;
}

export const usePrestamos = ({ buscarUsuario, estadoFilter, page, pageSize }: ParametrosPrestamos) => {
  const [devolviendoId, setDevolviendoId] = useState<number | null>(null);

  const estadoParam = estadoFilter === 'TODOS' ? undefined : estadoFilter;

  const { data: prestamosPage, isLoading, isError } = useQuery({
    queryKey: ['prestamos', estadoParam, page, pageSize],
    queryFn: () => prestamoService.obtenerTodos(undefined, estadoParam, page, pageSize),
    placeholderData: (prev) => prev,
  });

  const filteredPrestamos = useMemo(() => {
    const prestamos = prestamosPage?.content ?? [];
    if (!buscarUsuario.trim()) return prestamos;
    const q = buscarUsuario.toLowerCase();
    return prestamos.filter(
      (p) =>
        p.usuario.nombre.toLowerCase().includes(q) ||
        p.usuario.email.toLowerCase().includes(q),
    );
  }, [prestamosPage?.content, buscarUsuario]);

  const { mutacionDevolver } = useMutacionesPrestamo({
    alIniciarDevolucion: (id) => setDevolviendoId(id),
    alFinalizarDevolucion: () => setDevolviendoId(null),
  });
  return {
    filteredPrestamos,
    totalPages: prestamosPage?.page?.totalPages ?? 0,
    totalElements: prestamosPage?.page?.totalElements ?? 0,
    isLoading,
    isError,
    handleDevolver: (id: number) => mutacionDevolver.mutate(id),
    isDevolviendo: (id: number) => devolviendoId === id && mutacionDevolver.isPending,
  };
}
