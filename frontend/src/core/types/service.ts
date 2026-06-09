import type { Page } from './common';

export interface ServicioCrud<TResponse, TRequest> {
  obtenerTodos: (
    buscar?: string,
    estado?: boolean,
    page?: number,
    size?: number,
    sort?: string,
  ) => Promise<Page<TResponse>>;
  obtenerPorId: (id: number) => Promise<TResponse>;
  crear: (request: TRequest) => Promise<TResponse>;
  actualizar: (id: number, request: TRequest) => Promise<TResponse>;
  eliminar: (id: number) => Promise<void>;
  activar: (id: number) => Promise<TResponse>;
}

export type ServicioMutacionesCrud<TResponse, TRequest> = Pick<
  ServicioCrud<TResponse, TRequest>,
  'crear' | 'actualizar' | 'eliminar' | 'activar'
>;
