package com.biblioteca.backend.service.portal.impl;

import com.biblioteca.backend.entity.Libro;
import com.biblioteca.backend.entity.Usuario;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.service.portal.LectorAuthService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorCatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LectorCarritoServiceImpl implements LectorCarritoService {

    private final LectorCatalogoService lectorCatalogoService;
    private final LectorAuthService lectorAuthService;
    private final PrestamoRepository prestamoRepository;

    @Override
    @Transactional(readOnly = true)
    public Libro obtenerLibroAgregable(Long libroId, Long lectorId) {
        Libro libro = lectorCatalogoService.obtenerLibroActivo(libroId);

        if (libro.getStock() == null || libro.getStock() <= 0) {
            throw BusinessException.conflict("INSUFFICIENT_STOCK", "El libro \"" + libro.getTitulo() + "\" no tiene stock disponible.");
        }

        if (lectorId != null) {
            Usuario lector = lectorAuthService.obtenerLector(lectorId);
            if (!lector.isEstado()) {
                throw BusinessException.conflict("USER_INACTIVE", "Tu cuenta está inactiva. No puedes reservar libros por el momento.");
            }

            if (prestamoRepository.tienePrestamosVencidos(lectorId, Instant.now())) {
                throw BusinessException.conflict("USER_HAS_OVERDUE_LOANS", "No puedes agregar libros al carrito porque tienes entregas atrasadas pendientes.");
            }

            if (prestamoRepository.tieneLibroPrestadoActivo(lectorId, libroId)) {
                throw BusinessException.conflict("BOOK_ALREADY_LOANED_BY_USER", "Ya lo posees: \"" + libro.getTitulo() + "\".");
            }
        }

        return libro;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Libro> obtenerLibrosCarrito(List<Long> librosIds) {
        if (librosIds == null || librosIds.isEmpty()) {
            return List.of();
        }
        return librosIds.stream()
                .distinct()
                .map(lectorCatalogoService::obtenerLibroActivo)
                .toList();
    }
}
