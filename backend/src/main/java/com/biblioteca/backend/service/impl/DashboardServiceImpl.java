package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.dto.response.DashboardResumenResponse;
import com.biblioteca.backend.dto.response.PrestamoResponse;
import com.biblioteca.backend.entity.EstadoPrestamo;
import com.biblioteca.backend.entity.Prestamo;
import com.biblioteca.backend.mapper.PrestamoMapper;
import com.biblioteca.backend.repository.AutorRepository;
import com.biblioteca.backend.repository.CategoriaRepository;
import com.biblioteca.backend.repository.DetallePrestamoRepository;
import com.biblioteca.backend.repository.LibroRepository;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.repository.UsuarioRepository;
import com.biblioteca.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int STOCK_CRITICO = 5;
    private static final int LIMITE_PRESTAMOS_RECIENTES = 5;
    private static final int LIMITE_ALERTAS_INVENTARIO = 5;
    private static final int LIMITE_ALERTAS_PRESTAMOS = 5;
    private static final int LIMITE_RANKINGS = 5;
    private static final int DIAS_PROXIMOS_A_VENCER = 3;

    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final AutorRepository autorRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrestamoRepository prestamoRepository;
    private final DetallePrestamoRepository detallePrestamoRepository;
    private final PrestamoMapper prestamoMapper;

    @Override
    @Transactional(readOnly = true)
    public DashboardResumenResponse obtenerResumen() {
        var prestamosRecientes = cargarPrestamos(
                prestamoRepository.findIdsByFilters(
                        null,
                        null,
                        PageRequest.of(0, LIMITE_PRESTAMOS_RECIENTES, Sort.by(Sort.Direction.DESC, "id"))
                )
        );

        var alertasInventario = libroRepository.findAlertasInventario(
                STOCK_CRITICO,
                PageRequest.of(0, LIMITE_ALERTAS_INVENTARIO)
        );

        Instant ahora = Instant.now();
        Instant limiteProximosAVencer = ahora.plus(Duration.ofDays(DIAS_PROXIMOS_A_VENCER));
        Instant inicioMes = LocalDate.now(ZoneOffset.UTC)
                .withDayOfMonth(1)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC);

        var prestamosVencidos = cargarPrestamos(
                prestamoRepository.findIdsPrestamosActivosVencidos(
                        ahora,
                        PageRequest.of(0, LIMITE_ALERTAS_PRESTAMOS)
                )
        );

        var prestamosProximosAVencer = cargarPrestamos(
                prestamoRepository.findIdsPrestamosActivosPorVencer(
                        ahora,
                        limiteProximosAVencer,
                        PageRequest.of(0, LIMITE_ALERTAS_PRESTAMOS)
                )
        );

        return new DashboardResumenResponse(
                libroRepository.count(),
                libroRepository.countByEstadoTrue(),
                libroRepository.sumStockActivo(),
                usuarioRepository.count(),
                usuarioRepository.countByEstadoTrue(),
                autorRepository.count(),
                categoriaRepository.count(),
                prestamoRepository.countByEstado(EstadoPrestamo.ACTIVO),
                prestamoRepository.countPrestamosActivosVencidos(ahora),
                prestamoRepository.countPrestamosActivosPorVencer(ahora, limiteProximosAVencer),
                prestamoRepository.countByEstadoAndFechaDevolucionRealBetween(
                        EstadoPrestamo.DEVUELTO,
                        inicioMes,
                        ahora
                ),
                libroRepository.countByEstadoTrueAndStock(0),
                prestamosRecientes,
                prestamosVencidos,
                prestamosProximosAVencer,
                detallePrestamoRepository.findLibrosMasPrestados(PageRequest.of(0, LIMITE_RANKINGS)),
                detallePrestamoRepository.findLectoresMasActivos(PageRequest.of(0, LIMITE_RANKINGS)),
                libroRepository.findCategoriasConMasLibros(PageRequest.of(0, LIMITE_RANKINGS)),
                libroRepository.findAutoresConMasLibros(PageRequest.of(0, LIMITE_RANKINGS)),
                alertasInventario
        );
    }

    private List<PrestamoResponse> cargarPrestamos(Page<Long> idsPage) {
        if (idsPage.isEmpty()) {
            return List.of();
        }

        Map<Long, Prestamo> prestamosPorId = prestamoRepository.findAllByIdInWithRelations(idsPage.getContent())
                .stream()
                .collect(Collectors.toMap(Prestamo::getId, Function.identity()));

        return idsPage.getContent().stream()
                .map(prestamosPorId::get)
                .map(prestamoMapper::toResponse)
                .toList();
    }
}
