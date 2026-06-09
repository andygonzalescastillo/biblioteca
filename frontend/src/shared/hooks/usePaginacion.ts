import { useCallback, useState, type Dispatch, type SetStateAction } from 'react';
import { TAMANIO_PAGINA_POR_DEFECTO } from '@/shared/constants/paginacion';

export const usePaginacion = (tamanioInicial = TAMANIO_PAGINA_POR_DEFECTO) => {
  const [pagina, setPagina] = useState(0);
  const [tamanioPagina, setTamanioPagina] = useState(tamanioInicial);

  const reiniciarPagina = useCallback(() => setPagina(0), []);

  const manejarCambioFiltro = useCallback(
    <T,>(setter: Dispatch<SetStateAction<T>>) =>
      (valor: T) => {
        setter(valor);
        setPagina(0);
      },
    [],
  );

  return {
    pagina,
    setPagina,
    tamanioPagina,
    setTamanioPagina,
    reiniciarPagina,
    manejarCambioFiltro,
  };
}
