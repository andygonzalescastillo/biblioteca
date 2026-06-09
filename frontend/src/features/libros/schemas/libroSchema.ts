import * as z from 'zod';

export const libroSchema = z.object({
  titulo: z.string().min(1, 'El título es obligatorio').max(255, 'Máximo 255 caracteres'),
  isbn: z.string()
    .min(10, 'Mínimo 10 caracteres')
    .max(20, 'Máximo 20 caracteres'),
  stock: z.number({ error: 'El stock es obligatorio' })
    .int('Debe ser un número entero')
    .nonnegative('El stock no puede ser negativo'),
  categoriaId: z.number({ error: 'La categoría es obligatoria' })
    .positive('Selecciona una categoría válida'),
  autoresIds: z.array(z.number()).min(1, 'Selecciona al menos un autor'),
  portadaId: z.string().optional().nullable(),
  estado: z.boolean().optional(),
});

export type LibroFormValues = z.infer<typeof libroSchema>;
