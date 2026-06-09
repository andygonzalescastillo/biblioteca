import * as z from 'zod';

export const categoriaSchema = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(100, 'Máximo 100 caracteres'),
  descripcion: z.string().max(255, 'Máximo 255 caracteres').optional().or(z.literal('')),
  estado: z.boolean().optional(),
});

export type CategoriaFormValues = z.infer<typeof categoriaSchema>;
