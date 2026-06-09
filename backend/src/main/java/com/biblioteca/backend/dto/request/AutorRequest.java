package com.biblioteca.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record AutorRequest(
    @NotBlank(message = "El nombre del autor es obligatorio.")
    @Size(max = 150, message = "El nombre no puede exceder los 150 caracteres.")
    String nombre,

    String biografia,

    LocalDate fechaNacimiento,

    UUID fotoId,

    Boolean estado
) {}
