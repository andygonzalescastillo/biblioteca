package com.biblioteca.backend.dto.response;

import java.util.Set;

public record LibroResponse(
    Long id,
    String titulo,
    String isbn,
    Integer stock,
    CategoriaResponse categoria,
    ImagenResponse portada,
    Set<AutorResponse> autores,
    Boolean estado
) {}
