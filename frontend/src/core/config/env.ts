import { z } from 'zod';

const envSchema = z.object({
  VITE_API_URL: z.string().default('http://localhost:8080/api'),
});

export const env = envSchema.parse(import.meta.env);
