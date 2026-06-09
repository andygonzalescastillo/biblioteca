export interface ConfiguracionPrestamo {
  diasDefault: number;
  diasMinimo: number;
  diasMaximo: number;
  cantidadReservaMaxima: number;
  maxLibrosPrestadosConcurrentes: number;
}

export interface ConfiguracionPublicaResponse {
  prestamo: ConfiguracionPrestamo;
}
