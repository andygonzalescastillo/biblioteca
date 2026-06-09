package com.biblioteca.backend.service.impl;

import com.biblioteca.backend.config.AppProperties;
import com.biblioteca.backend.entity.*;
import com.biblioteca.backend.exception.BusinessException;
import com.biblioteca.backend.repository.PrestamoRepository;
import com.biblioteca.backend.repository.UsuarioRepository;
import com.biblioteca.backend.service.LibroService;
import com.biblioteca.backend.service.PrestamoService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PrestamoServiceImpl implements PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroService libroService;
    private final AppProperties appProperties;

    @Override
    @Transactional(readOnly = true)
    public Page<Prestamo> obtenerTodos(Long usuarioId, EstadoPrestamo estado, Pageable pageable) {
        Page<Long> idsPage = prestamoRepository.findIdsByFilters(usuarioId, estado, pageable);
        return cargarPrestamosPaginados(idsPage, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Prestamo obtenerPorId(Long id) {
        return prestamoRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new EntityNotFoundException("Préstamo no encontrado con el ID: " + id));
    }


    @Override
    @Transactional
    public Prestamo registrarPrestamo(PrestamoRequest request) {
        Long usuarioId = request.usuarioId();
        int diasLimite = request.diasLimite();
        List<DetalleRequest> detallesRequest = request.detalles();

        if (detallesRequest == null || detallesRequest.isEmpty()) {
            throw BusinessException.badRequest("EMPTY_LOAN_DETAILS", "El préstamo debe contener al menos un libro.");
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con el ID: " + usuarioId));

        if (!usuario.isEstado()) {
            throw BusinessException.conflict("USER_INACTIVE", "El usuario está inactivo. No se pueden realizar préstamos.");
        }

        if (diasLimite < appProperties.prestamo().diasMinimo() || diasLimite > appProperties.prestamo().diasMaximo()) {
            throw BusinessException.badRequest("INVALID_LOAN_DAYS", "La duración del préstamo debe estar entre "
                    + appProperties.prestamo().diasMinimo() + " y "
                    + appProperties.prestamo().diasMaximo() + " días.");
        }

        if (prestamoRepository.tienePrestamosVencidos(usuarioId, Instant.now())) {
            throw BusinessException.conflict("USER_HAS_OVERDUE_LOANS", "No se pueden realizar nuevos préstamos porque el usuario tiene entregas atrasadas pendientes.");
        }

        long totalCantidadSolicitada = detallesRequest.stream().mapToLong(DetalleRequest::cantidad).sum();
        long librosEnPoseccion = prestamoRepository.countTotalLibrosEnPoseccion(usuarioId);
        if (librosEnPoseccion + totalCantidadSolicitada > appProperties.prestamo().maxLibrosPrestadosConcurrentes()) {
            throw BusinessException.conflict("MAX_CONCURRENT_LOANS_EXCEEDED", "El usuario ha alcanzado el límite máximo de libros prestados simultáneamente. "
                    + "Actualmente posee " + librosEnPoseccion + " libro(s) y está solicitando " + totalCantidadSolicitada + " más. "
                    + "El límite total es " + appProperties.prestamo().maxLibrosPrestadosConcurrentes() + ".");
        }

        Instant ahora = Instant.now();
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuario)
                .fechaPrestamo(ahora)
                .fechaDevolucionLimite(ahora.plus(diasLimite, ChronoUnit.DAYS))
                .estado(EstadoPrestamo.ACTIVO)
                .detalles(new ArrayList<>())
                .build();

        for (DetalleRequest req : detallesRequest) {
            if (req.cantidad() <= 0) {
                throw BusinessException.badRequest("INVALID_QUANTITY", "La cantidad prestada debe ser mayor a 0.");
            }

            if (req.cantidad() > appProperties.prestamo().cantidadReservaMaxima()) {
                throw BusinessException.badRequest("INVALID_QUANTITY", "No se permite solicitar más de " 
                        + appProperties.prestamo().cantidadReservaMaxima() + " unidad(es) de un mismo libro por préstamo.");
            }

            Libro libro = libroService.obtenerPorId(req.libroId());

            if (!libro.isEstado()) {
                throw BusinessException.conflict("BOOK_INACTIVE", "El libro \"" + libro.getTitulo() + "\" no está disponible para préstamos (inactivo).");
            }

            if (prestamoRepository.tieneLibroPrestadoActivo(usuarioId, libro.getId())) {
                throw BusinessException.conflict("BOOK_ALREADY_LOANED_BY_USER", "El lector ya lo posee: \"" + libro.getTitulo() + "\".");
            }

            libroService.actualizarStock(libro.getId(), -req.cantidad());

            DetallePrestamo detalle = DetallePrestamo.builder()
                    .prestamo(prestamo)
                    .libro(libro)
                    .cantidad(req.cantidad())
                    .build();

            prestamo.getDetalles().add(detalle);
        }

        return prestamoRepository.save(prestamo);
    }

    @Override
    @Transactional
    public Prestamo devolverPrestamo(Long id) {
        Prestamo prestamo = obtenerPorId(id);

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw BusinessException.conflict("LOAN_ALREADY_RETURNED", "Este préstamo ya fue devuelto anteriormente.");
        }

        prestamo.setFechaDevolucionReal(Instant.now());
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);

        prestamo.getDetalles().forEach(detalle ->
                libroService.actualizarStock(detalle.getLibro().getId(), detalle.getCantidad()));

        return prestamoRepository.save(prestamo);
    }

    private Page<Prestamo> cargarPrestamosPaginados(Page<Long> idsPage, Pageable pageable) {
        if (idsPage.isEmpty()) {
            return Page.empty(pageable);
        }

        Map<Long, Prestamo> prestamosPorId = prestamoRepository.findAllByIdInWithRelations(idsPage.getContent())
                .stream()
                .collect(Collectors.toMap(Prestamo::getId, Function.identity()));

        List<Prestamo> prestamosOrdenados = idsPage.getContent().stream()
                .map(prestamosPorId::get)
                .toList();

        return new PageImpl<>(prestamosOrdenados, pageable, idsPage.getTotalElements());
    }
}
