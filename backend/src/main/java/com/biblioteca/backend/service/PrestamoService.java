package com.biblioteca.backend.service;

import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PrestamoService {
    record DetalleRequest(Long libroId, Integer cantidad) {}
    record PrestamoRequest(Long usuarioId, int diasLimite, List<DetalleRequest> detalles) {}

    Page<Prestamo> obtenerTodos(Long usuarioId, EstadoPrestamo estado, Pageable pageable);
    Prestamo obtenerPorId(Long id);
    Prestamo registrarPrestamo(PrestamoRequest request);
    Prestamo devolverPrestamo(Long id);
}
