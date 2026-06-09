import * as z from 'zod';

export const prestamoFormSchema = z.object({
  usuarioId: z.number({ error: 'Selecciona un lector' })
    .positive('Selecciona un lector'),
  diasLimite: z.number({ error: 'La duración es obligatoria' })
    .int()
    .positive('La duración debe ser mayor a 0'),
});

export type PrestamoFormValues = z.infer<typeof prestamoFormSchema>;
