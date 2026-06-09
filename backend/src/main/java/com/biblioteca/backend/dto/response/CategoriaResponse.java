package com.biblioteca.backend.dto.response;

public record CategoriaResponse(
    Long id,
    String nombre,
    String descripcion,
    Boolean estado
) {}
