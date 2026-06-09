package com.biblioteca.backend.service.portal;

import com.biblioteca.backend.entity.Prestamo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LectorPrestamoService {
    Prestamo registrarPrestamoDesdeCarrito(Long lectorId, List<Long> librosIds, Integer diasPrestamo);
    Page<Prestamo> obtenerPrestamosDelLector(Long lectorId, Pageable pageable);
}
