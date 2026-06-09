export const API_ENDPOINTS = {
  AUTORES: {
    BASE: '/autores',
    BY_ID: (id: number) => `/autores/${id}`,
    ACTIVAR: (id: number) => `/autores/${id}/activar`,
  },
  CATEGORIAS: {
    BASE: '/categorias',
    BY_ID: (id: number) => `/categorias/${id}`,
    ACTIVAR: (id: number) => `/categorias/${id}/activar`,
  },
  CONFIGURACION: {
    PUBLICA: '/configuracion/publica',
  },
  DASHBOARD: {
    RESUMEN: '/dashboard/resumen',
  },
  IMAGENES: {
    BASE: '/imagenes',
    BY_ID: (id: string) => `/imagenes/${id}`,
  },
  LIBROS: {
    BASE: '/libros',
    BY_ID: (id: number) => `/libros/${id}`,
    ACTIVAR: (id: number) => `/libros/${id}/activar`,
    BY_ISBN: (isbn: string) => `/libros/isbn/${isbn}`,
  },
  PRESTAMOS: {
    BASE: '/prestamos',
    BY_ID: (id: number) => `/prestamos/${id}`,
    DEVOLUCION: (id: number) => `/prestamos/${id}/devolucion`,
  },
  USUARIOS: {
    BASE: '/usuarios',
    BY_ID: (id: number) => `/usuarios/${id}`,
    ACTIVAR: (id: number) => `/usuarios/${id}/activar`,
    BY_EMAIL: (email: string) => `/usuarios/email/${email}`,
    CUPO_PRESTAMO: (id: number) => `/usuarios/${id}/cupo-prestamo`,
  },
} as const;
