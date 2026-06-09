package com.biblioteca.backend.dto.response;

public record DetallePrestamoResponse(
    Long id,
    LibroResponse libro,
    Integer cantidad
) {}
