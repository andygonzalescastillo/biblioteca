package com.biblioteca.backend.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ImagenResponse(
    UUID id,
    String nombreArchivo,
    String urlAlmacenamiento,
    Instant fechaCreacion
) {}
