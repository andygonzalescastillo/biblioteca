package com.biblioteca.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String uploadDir,
    Cors cors,
    Prestamo prestamo
) {
    public record Cors(
        List<String> allowedOrigins
    ) {}

    public record Prestamo(
        int diasDefault,
        int diasMinimo,
        int diasMaximo,
        int cantidadReservaMaxima,
        int maxLibrosPrestadosConcurrentes
    ) {}
}
