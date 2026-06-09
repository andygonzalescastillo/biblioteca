package com.biblioteca.backend.dto.response;

public record DashboardLibroInventarioResponse(
    Long id,
    String titulo,
    Integer stock,
    String categoriaNombre
) {}
