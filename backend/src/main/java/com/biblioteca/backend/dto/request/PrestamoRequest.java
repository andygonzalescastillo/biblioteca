package com.biblioteca.backend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record PrestamoRequest(
    @NotNull(message = "El ID de usuario es obligatorio.")
    Long usuarioId,

    @NotEmpty(message = "Debe haber al menos un libro en el préstamo.")
    List<@Valid DetalleRequest> detalles,

    @NotNull(message = "El número de días límite es obligatorio.")
    @Min(value = 1, message = "El préstamo debe ser de al menos 1 día.")
    Integer diasLimite
) {
    public record DetalleRequest(
        @NotNull(message = "El ID del libro es obligatorio.")
        Long libroId,

        @NotNull(message = "La cantidad es obligatoria.")
        @Min(value = 1, message = "La cantidad mínima es 1.")
        Integer cantidad
    ) {}
}
