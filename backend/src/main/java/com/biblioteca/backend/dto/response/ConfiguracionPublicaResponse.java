package com.biblioteca.backend.dto.response;

public record ConfiguracionPublicaResponse(
        Prestamo prestamo
) {
    public record Prestamo(
            int diasDefault,
            int diasMinimo,
            int diasMaximo,
            int cantidadReservaMaxima,
            int maxLibrosPrestadosConcurrentes
    ) {}
}
