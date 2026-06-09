package com.biblioteca.backend.service.portal.impl;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.entity.Prestamo;
import com.biblioteca.backend.service.PrestamoService;
import com.biblioteca.backend.service.portal.LectorCarritoService;
import com.biblioteca.backend.service.portal.LectorPrestamoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LectorPrestamoServiceImpl implements LectorPrestamoService {

    private final PrestamoService prestamoService;
    private final LectorCarritoService lectorCarritoService;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public Prestamo registrarPrestamoDesdeCarrito(Long lectorId, List<Long> librosIds, Integer diasPrestamo) {
        if (librosIds == null || librosIds.isEmpty()) {
            throw BusinessException.badRequest("EMPTY_CART", "El carrito está vacío.");
        }

        int dias = normalizarDiasPrestamo(diasPrestamo);

        var cantidadesPorLibro = librosIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        List<PrestamoService.DetalleRequest> detalles = lectorCarritoService.obtenerLibrosCarrito(librosIds)
                .stream()
                .map(libro -> new PrestamoService.DetalleRequest(
                        libro.getId(),
                        Math.toIntExact(cantidadesPorLibro.getOrDefault(libro.getId(), 0L))
                ))
                .distinct()
                .toList();

        return prestamoService.registrarPrestamo(new PrestamoService.PrestamoRequest(lectorId, dias, detalles));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Prestamo> obtenerPrestamosDelLector(Long lectorId, Pageable pageable) {
        return prestamoService.obtenerTodos(lectorId, null, pageable);
    }

    private int normalizarDiasPrestamo(Integer diasPrestamo) {
        int dias = Optional.ofNullable(diasPrestamo).orElse(appProperties.prestamo().diasDefault());
        if (dias < appProperties.prestamo().diasMinimo() || dias > appProperties.prestamo().diasMaximo()) {
            throw BusinessException.badRequest("INVALID_LOAN_DAYS", "La duración del préstamo debe estar entre "
                    + appProperties.prestamo().diasMinimo() + " y "
                    + appProperties.prestamo().diasMaximo() + " días.");
        }
        return dias;
    }
}
