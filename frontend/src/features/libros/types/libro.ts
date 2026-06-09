import type { CategoriaResponse } from '@/features/categorias/types/categoria';
import type { ImagenResponse } from '@/shared/types/imagen';
import type { AutorResponse } from '@/features/autores/types/autor';

export interface LibroResponse {
  id: number;
  titulo: string;
  isbn: string;
  stock: number;
  categoria: CategoriaResponse;
  portada?: ImagenResponse;
  autores: AutorResponse[];
  estado: boolean;
}

export interface LibroRequest {
  titulo: string;
  isbn: string;
  stock: number;
  categoriaId: number;
  autoresIds: number[];
  portadaId?: string;
  estado?: boolean;
}
