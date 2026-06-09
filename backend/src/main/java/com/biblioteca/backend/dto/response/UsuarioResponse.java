package com.biblioteca.backend.dto.response;

import java.time.Instant;

public record UsuarioResponse(
    Long id,
    String nombre,
    String email,
    String telefono,
    String direccion,
    Instant fechaRegistro,
    ImagenResponse foto,
    Boolean estado
) {}
