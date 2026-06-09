package com.biblioteca.backend.dto.request;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record UsuarioRequest(
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar los 150 caracteres.")
    String nombre,

    @NotBlank(message = "El email es obligatorio.")
    @Email(message = "El formato del email es inválido.")
    @Size(max = 150, message = "El email no puede superar los 150 caracteres.")
    String email,

    @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres.")
    String telefono,

    @Size(max = 255, message = "La dirección no puede superar los 255 caracteres.")
    String direccion,

    UUID fotoId,

    Boolean estado
) {}
