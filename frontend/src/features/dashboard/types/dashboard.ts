import type { PrestamoResponse } from '@/features/prestamos/types/prestamo';

export interface DashboardLibroInventarioResponse {
  id: number;
  titulo: string;
  stock: number;
  categoriaNombre: string;
}

export interface DashboardRankingResponse {
  id: number;
  nombre: string;
  total: number;
}

export interface DashboardResumenResponse {
  totalLibros: number;
  totalLibrosActivos: number;
  totalEjemplares: number;
  totalUsuarios: number;
  usuariosActivos: number;
  totalAutores: number;
  totalCategorias: number;
  prestamosActivos: number;
  prestamosAtrasados: number;
  prestamosPorVencer: number;
  prestamosDevueltosEsteMes: number;
  librosAgotados: number;
  prestamosRecientes: PrestamoResponse[];
  prestamosVencidos: PrestamoResponse[];
  prestamosProximosAVencer: PrestamoResponse[];
  librosMasPrestados: DashboardRankingResponse[];
  lectoresMasActivos: DashboardRankingResponse[];
  categoriasConMasLibros: DashboardRankingResponse[];
  autoresConMasLibros: DashboardRankingResponse[];
  alertasInventario: DashboardLibroInventarioResponse[];
}
