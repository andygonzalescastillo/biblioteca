import * as z from 'zod';

export const usuarioSchema = z.object({
  nombre: z.string().min(3, 'Mínimo 3 caracteres').max(150, 'Máximo 150 caracteres'),
  email: z.string().email('Email inválido').max(150, 'Máximo 150 caracteres'),
  telefono: z.string().max(20, 'Máximo 20 caracteres').optional().or(z.literal('')),
  direccion: z.string().max(255, 'Máximo 255 caracteres').optional().or(z.literal('')),
  fotoId: z.string().optional().nullable(),
  estado: z.boolean().optional(),
});

export type UsuarioFormValues = z.infer<typeof usuarioSchema>;
