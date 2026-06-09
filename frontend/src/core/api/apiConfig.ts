import { env } from '@/core/config/env';

export const API_BASE_URL = env.VITE_API_URL;

export const API_ORIGIN = API_BASE_URL.replace(/\/api\/?$/, '');
