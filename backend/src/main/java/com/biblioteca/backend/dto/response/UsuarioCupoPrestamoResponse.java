package com.biblioteca.backend.dto.response;

import java.util.List;

public record UsuarioCupoPrestamoResponse(
        Long usuarioId,
        long maximoPermitido,
        long librosEnPosesion,
        long cupoDisponible,
        List<Long> librosPrestadosIds
) {}
