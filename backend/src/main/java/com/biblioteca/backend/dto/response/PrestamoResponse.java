package com.biblioteca.backend.dto.response;

import com.biblioteca.backend.entity.EstadoPrestamo;
import java.time.Instant;
import java.util.List;

public record PrestamoResponse(
    Long id,
    UsuarioResponse usuario,
    Instant fechaPrestamo,
    Instant fechaDevolucionLimite,
    Instant fechaDevolucionReal,
    EstadoPrestamo estado,
    List<DetallePrestamoResponse> detalles
) {}
