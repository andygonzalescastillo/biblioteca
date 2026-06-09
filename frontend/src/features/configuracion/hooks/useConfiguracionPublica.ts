import { useQuery } from '@tanstack/react-query';
import { configuracionService } from '../services/configuracionService';

const TIEMPO_CACHE_CONFIGURACION = 1000 * 60 * 10;

export const useConfiguracionPublica = () => {
  return useQuery({
    queryKey: ['configuracion-publica'],
    queryFn: configuracionService.obtenerPublica,
    staleTime: TIEMPO_CACHE_CONFIGURACION,
  });
};
