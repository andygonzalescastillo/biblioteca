import * as z from 'zod';

export const autorSchema = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(150, 'Máximo 150 caracteres'),
  biografia: z.string().optional().or(z.literal('')),
  fechaNacimiento: z.string().optional().or(z.literal('')),
  fotoId: z.string().optional().nullable(),
  estado: z.boolean().optional(),
});

export type AutorFormValues = z.infer<typeof autorSchema>;
