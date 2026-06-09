package com.biblioteca.backend.dto.request;

import jakarta.validation.constraints.*;
import java.util.Set;
import java.util.UUID;

public record LibroRequest(
    @NotBlank(message = "El título es obligatorio.")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres.")
    String titulo,

    @NotBlank(message = "El ISBN es obligatorio.")
    @Size(max = 20, message = "El ISBN no puede superar los 20 caracteres.")
    String isbn,

    @NotNull(message = "El stock es obligatorio.")
    @Min(value = 0, message = "El stock no puede ser negativo.")
    Integer stock,

    @NotNull(message = "La categoría es obligatoria.")
    Long categoriaId,

    @NotEmpty(message = "Debe especificar al menos un autor.")
    Set<Long> autoresIds,

    UUID portadaId,

    Boolean estado
) {}
