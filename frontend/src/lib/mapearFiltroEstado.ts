export type FiltroEstado = 'todos' | 'activos' | 'inactivos';

export const mapearFiltroEstado = (filtro: FiltroEstado): boolean | undefined => {
  if (filtro === 'activos') return true;
  if (filtro === 'inactivos') return false;
  return undefined;
};
