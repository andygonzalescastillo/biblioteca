package com.biblioteca.backend.dto.response;

import java.time.LocalDate;

public record AutorResponse(
    Long id,
    String nombre,
    String biografia,
    LocalDate fechaNacimiento,
    ImagenResponse foto,
    Boolean estado
) {}
